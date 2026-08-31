package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.SidUppgiftException;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.UppgiftNotFoundException;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka.OperativtUppgiftslagerProducer;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.NotTeamMemberException;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SorteringsordningNotFoundException;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.sid.SidChecker;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.TeamService;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.OulDataStorage;

/**
 * Application service that orchestrates uppgift lifecycle and sorteringsordning management.
 * Delegates all persistence to {@link OulDataStorage} and all event publishing to
 * {@link OperativtUppgiftslagerProducer}.
 */
@ApplicationScoped
public class OperativtUppgiftslagerService
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerService.class);

   @Inject
   LogicMapper logicMapper;

   @Inject
   OperativtUppgiftslagerProducer producer;

   @Inject
   OulDataStorage storage;

   @Inject
   TeamService teamService;

   @Inject
   SidChecker sidChecker;

   public UppgiftDto addOperativeTask(OperativtUppgiftslagerAddRequest addRequest, String notificationTopic,
         String replyTopic, Map<String, String> cloudeventAttributes)
   {
      log.info("Adding new task");
      var uppgift = ImmutableUppgiftEntity.builder()
            .uppgiftId(UUID.randomUUID())
            .handlaggningId(addRequest.handlaggningId())
            .skapad(LocalDate.now())
            .status(UppgiftStatus.NY)
            .regel(addRequest.regel())
            .beskrivning(addRequest.beskrivning())
            .verksamhetslogik(addRequest.verksamhetslogik())
            .roll(addRequest.roll())
            .url(addRequest.url())
            .subTopic(notificationTopic)
            .replyTopic(replyTopic)
            .cloudeventAttributes(cloudeventAttributes)
            .erbjudande(addRequest.erbjudande())
            .build();

      storage.createUppgift(uppgift);
      return logicMapper.toUppgiftDto(uppgift);
   }

   /**
    * Ends the given uppgift with the provided reason.
    * Throws {@link UppgiftNotFoundException} (→ HTTP 404) if the uppgift does not exist.
    *
    * @param uppgiftId the uppgift to end
    * @param reason    the reason for ending the uppgift
    * @return the ended uppgift
    */
   public UppgiftDto endTask(UUID uppgiftId, String reason)
   {
      log.info("Ending task {} with reason: {}", uppgiftId, reason);
      var task = storage.findUppgiftById(uppgiftId);

      var endedTask = ImmutableUppgiftEntity.builder()
            .from(task)
            .status(UppgiftStatus.AVSLUTAD)
            .utford(LocalDate.now())
            .reason(reason)
            .build();
      storage.deleteUppgift(uppgiftId);
      log.info("Task {} ended", uppgiftId);
      return logicMapper.toUppgiftDto(endedTask);
   }

   /**
    * Returns a paginated and sorted page of uppgifter.
    * Sorting and pagination are pushed down to the database via a single SQL query.
    * Throws {@link SorteringsordningNotFoundException} (→ HTTP 404) if a specific
    * sorteringsordningId is given but not found.
    *
    * @param limit               maximum items per page
    * @param offset              zero-based start index
    * @param sorteringsordningId specific sorteringsordning to use, or {@code null} for the default
    * @return the sorted page
    */
   public SortedUppgiftPage getUppgifterPage(int limit, int offset, UUID sorteringsordningId)
   {
      SorteringsordningEntity sorteringsordning;
      if (sorteringsordningId != null)
      {
         sorteringsordning = storage.getSorteringsordningById(sorteringsordningId)
               .orElseThrow(() -> new SorteringsordningNotFoundException(sorteringsordningId));
      }
      else
      {
         sorteringsordning = storage.getDefaultSorteringsordning()
               .orElse(new SorteringsordningEntity(null, null, null, List.of()));
      }

      var page = storage.findUppgifterPage(sorteringsordning, limit, offset);
      var items = page.items().stream().map(logicMapper::toUppgiftDto).toList();
      return new SortedUppgiftPage(page.total(), items);
   }

   /**
    * Returns all uppgifter assigned to the given handläggare, sorted according to the
    * default sorteringsordning. If no sorteringsordning is configured the order is unspecified.
    * Any uppgift that has become SID-märkt since assignment, where the assigned handläggare
    * lacks SID-behörighet, is unassigned back into OUL's pool and excluded (FKPOC-940) — see
    * {@link #filterSidBlocked}.
    *
    * @param idTyp        the handläggare identity type id
    * @param handlaggarId the handläggare identity value
    * @return the uppgifter still visible to the caller, plus how many were removed
    */
   public UppgiftListResult getUppgifterHandlaggare(String idTyp, String handlaggarId)
   {
      log.info("Getting all tasks for handlaggarId: {}", handlaggarId);
      var handlaggare = ImmutableIdtyp.builder()
            .typId(idTyp)
            .varde(handlaggarId)
            .build();
      var sorteringsordning = storage.getDefaultSorteringsordning()
            .orElse(new SorteringsordningEntity(null, null, null, List.of()));
      var uppgifter = storage.findAllUppgifterByHandlaggarId(handlaggare, sorteringsordning);
      return filterSidBlocked(uppgifter);
   }

   /**
    * Returns all uppgifter assigned to any member of the caller's team, sorted according to the
    * default sorteringsordning.
    * Throws {@link NotTeamMemberException} (→ HTTP 403) if the caller belongs to no known team (OUL-FR-17.4).
    * Returns an empty result if the caller's team(s) have no members.
    * Any uppgift that has become SID-märkt since assignment, where its assigned handläggare
    * lacks SID-behörighet, is unassigned back into OUL's pool and excluded (FKPOC-940) — see
    * {@link #filterSidBlocked}.
    *
    * @param callerHandlaggare the calling handläggare's identity (used to determine team)
    * @return the uppgifter still visible to the caller, plus how many were removed
    */
   public UppgiftListResult getUppgifterTeam(Idtyp callerHandlaggare)
   {
      log.info("Getting all team tasks for handlaggarId: {}", callerHandlaggare.varde());
      // throws NotTeamMemberException (→ 403) if the caller belongs to no known team
      var teamMembers = teamService.teamMembers(callerHandlaggare);
      if (teamMembers.isEmpty())
      {
         return new UppgiftListResult(List.of(), 0);
      }
      var sorteringsordning = storage.getDefaultSorteringsordning()
            .orElse(new SorteringsordningEntity(null, null, null, List.of()));
      var uppgifter = storage.findAllUppgifterByTeam(teamMembers, sorteringsordning);
      return filterSidBlocked(uppgifter);
   }

   /**
    * Reassigns the given uppgift to the calling handläggare.
    * Throws {@link UppgiftNotFoundException} (→ HTTP 404) if the uppgift does not exist.
    * Throws {@link NotTeamMemberException} (→ HTTP 403) if the current assignee is not a team member.
    * Publishes a Kafka status-update notification after a successful reassignment.
    *
    * @param uppgiftId         the uppgift to reassign
    * @param callerHandlaggare the new handläggare identity
    * @return the updated uppgift
    */
   public UppgiftDto reassignUppgift(UUID uppgiftId, Idtyp callerHandlaggare)
   {
      log.info("Reassigning uppgift {} to handlaggarId: {}", uppgiftId, callerHandlaggare.varde());
      var current = storage.findUppgiftById(uppgiftId);

      if (current.handlaggarId() == null || !teamService.isSameTeam(callerHandlaggare, current.handlaggarId()))
      {
         throw new NotTeamMemberException(uppgiftId);
      }

      var updated = storage.updateUppgift(uppgiftId, callerHandlaggare);
      notifyStatusUpdate(updated);
      log.info("Reassigned uppgift {} to handlaggarId: {}", uppgiftId, callerHandlaggare.varde());
      return logicMapper.toUppgiftDto(updated);
   }

   /**
    * Assigns the highest-priority unassigned uppgift to the given handläggare according to the
    * default sorteringsordning. Returns {@code null} when no unassigned uppgift is available.
    *
    * @param idTyp        the handläggare identity type id
    * @param handlaggarId the handläggare identity value
    * @return the assigned uppgift, or {@code null} if none is available
    */
   public UppgiftDto assignNewTask(String idTyp, String handlaggarId)
   {
      log.info("Assigning new task to handlaggarId: {} with type: {}", handlaggarId, idTyp);
      var handlaggare = ImmutableIdtyp.builder()
            .typId(idTyp)
            .varde(handlaggarId)
            .build();
      var sorteringsordning = storage.getDefaultSorteringsordning()
            .orElse(new SorteringsordningEntity(null, null, null, List.of()));
      var harSidBehorighet = resolveSidBehorighet(handlaggare);

      UppgiftEntity uppgift;
      List<UUID> excludedUppgiftIds = new ArrayList<>();
      while (true)
      {
         try
         {
            uppgift = storage.assignNewUppgift(handlaggare, sorteringsordning, excludedUppgiftIds, harSidBehorighet);
            break;
         }
         catch (SidUppgiftException e)
         {
            excludedUppgiftIds.add(e.getUppgiftsId());
         }
      }

      if (uppgift == null)
      {
         log.info("Failed to assign new task to handlaggarId: {}", handlaggarId);
         return null;
      }

      notifyStatusUpdate(uppgift);
      log.info("Assigned task {} to handlaggarId: {}", uppgift.uppgiftId(), handlaggarId);
      return logicMapper.toUppgiftDto(uppgift);
   }

   /**
    * Resolves whether {@code handlaggare} has SID-behörighet. Used by {@link #assignNewTask}
    * (once per call rather than once per retry) and {@link #isSidBlocked}. Fails open (treats as
    * {@code false}, i.e. no behörighet / skip SID uppgifter) on any failure beyond a plain "not
    * found" — a Team API outage should degrade to the old unconditional-skip behaviour for SID
    * uppgifter, not turn assignment or listing into a hard failure for every handläggare whose
    * uppgift happens to be SID-marked.
    *
    * @param handlaggare the handläggare identity
    * @return whether the handläggare has SID-behörighet, or {@code false} if that could not
    *         be determined
    */
   private boolean resolveSidBehorighet(Idtyp handlaggare)
   {
      try
      {
         return teamService.harSidBehorighet(handlaggare);
      }
      catch (RuntimeException e)
      {
         log.warn("Failed to resolve SID-behörighet for handlaggarId: {}; treating as no behörighet",
               handlaggare.varde(), e);
         return false;
      }
   }

   /**
    * Filters an already-assigned uppgift list, removing (and unassigning) any uppgift whose
    * current assignee lacks SID-behörighet for a now SID-märkt uppgift (FKPOC-940). Checked per
    * row rather than once, since a team's uppgifter can each have a different assignee.
    *
    * @param uppgifter the already-assigned uppgifter to filter
    * @return the uppgifter still visible to the caller, plus how many were removed
    */
   private UppgiftListResult filterSidBlocked(List<UppgiftEntity> uppgifter)
   {
      var kept = new ArrayList<UppgiftDto>();
      var removed = 0;
      for (var uppgift : uppgifter)
      {
         if (isSidBlocked(uppgift))
         {
            if (unassignIfStillAssignedTo(uppgift.uppgiftId(), uppgift.handlaggarId()))
            {
               removed++;
            }
         }
         else
         {
            kept.add(logicMapper.toUppgiftDto(uppgift));
         }
      }
      return new UppgiftListResult(kept, removed);
   }

   /**
    * Resolves whether {@code uppgift} should be removed from a list: SID-märkt, and its current
    * assignee lacks SID-behörighet. The behörighet check reuses {@link #resolveSidBehorighet}'s
    * fail-open behaviour and runs first, so an authorized assignee's uppgift never pays for the
    * SID-status check at all. Unlike the behörighet check, a failure to determine SID status is
    * NOT swallowed here (review of FKPOC-940 #67): OUL can't guess whether an uppgift is
    * SID-märkt, so {@link SidChecker#containsSid} propagates its exception on failure and the
    * whole list call fails (→ 500) rather than risk returning a list that might wrongly include
    * or exclude a SID-märkt uppgift.
    *
    * @param uppgift the uppgift to check, with its current assignee
    * @return whether the uppgift should be unassigned and excluded from the list
    */
   private boolean isSidBlocked(UppgiftEntity uppgift)
   {
      if (resolveSidBehorighet(uppgift.handlaggarId()))
      {
         return false;
      }

      return sidChecker.containsSid(uppgift.handlaggningId(), uppgift.uppgiftId());
   }

   /**
    * Unassigns an uppgift flagged by {@link #isSidBlocked}, but only if it is still assigned to
    * {@code expectedHandlaggare} — the identity the removal decision in {@link #filterSidBlocked}
    * was based on. Tolerates two races found in review: the uppgift no longer existing (ended/
    * deleted between the list query and this call), and the uppgift having been reassigned to
    * someone else in the meantime. Both are silent no-ops rather than an uncaught exception or a
    * clobbered reassignment — the row has already left {@code expectedHandlaggare}'s list for a
    * reason unrelated to SID-behörighet, so it is correctly excluded from the list either way, just
    * not counted as a behörighet-driven removal.
    *
    * @param uppgiftId          the uppgift to unassign
    * @param expectedHandlaggare the handläggare the removal decision was based on
    * @return whether the uppgift was actually unassigned
    */
   private boolean unassignIfStillAssignedTo(UUID uppgiftId, Idtyp expectedHandlaggare)
   {
      var updated = storage.unassignUppgiftIfAssignedTo(uppgiftId, expectedHandlaggare);

      if (updated == null)
      {
         log.warn("Uppgift {} no longer assigned to handlaggarId: {} when attempting SID-driven unassign; skipping",
               uppgiftId, expectedHandlaggare.varde());
         return false;
      }

      notifyStatusUpdate(updated);
      return true;
   }

   /**
    * Removes the handläggare assignment from the given uppgift.
    * Throws {@link UppgiftNotFoundException} (→ HTTP 404) if the uppgift does not exist.
    *
    * @param uppgiftId the uppgift to unassign
    * @return the updated uppgift
    */
   public UppgiftDto unassignTask(UUID uppgiftId)
   {
      var uppgift = storage.unassignUppgift(uppgiftId);
      notifyStatusUpdate(uppgift);
      return logicMapper.toUppgiftDto(uppgift);
   }

   /**
    * Updates the handläggare assignment on the given uppgift.
    * Throws {@link UppgiftNotFoundException} (→ HTTP 404) if the uppgift does not exist.
    *
    * @param uppgiftId    the uppgift to update
    * @param handlaggarId the new handläggare identity, or {@code null} to clear
    * @return the updated uppgift
    */
   public UppgiftDto updateTask(UUID uppgiftId, Idtyp handlaggarId)
   {
      var uppgift = storage.updateUppgift(uppgiftId, handlaggarId);
      notifyStatusUpdate(uppgift);
      return logicMapper.toUppgiftDto(uppgift);
   }

   /**
    * Previews how the given sorteringsordning spec would sort the current uppgifter.
    * Uses the same DB sort engine as {@link #getUppgifterPage} — no separate in-memory path.
    *
    * @param spec   the sorteringsordning spec to preview
    * @param limit  maximum items per page
    * @param offset zero-based start index
    * @return the sorted page as it would appear if the spec were saved and applied
    */
   public SortedUppgiftPage previewSorteringsordning(SorteringsordningSpec spec, int limit, int offset)
   {
      var entity = new SorteringsordningEntity(null, null, spec.getNamn(), spec.getEntries());
      var page = storage.findUppgifterPage(entity, limit, offset);
      var items = page.items().stream().map(logicMapper::toUppgiftDto).toList();
      return new SortedUppgiftPage(page.total(), items);
   }

   public SorteringsordningEntity createSorteringsordning(SorteringsordningSpec spec)
   {
      var entity = new SorteringsordningEntity(UUID.randomUUID(), OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS),
            spec.getNamn(), spec.getEntries());
      storage.saveSorteringsordning(entity);
      return entity;
   }

   public Optional<SorteringsordningEntity> getDefaultSorteringsordning()
   {
      return storage.getDefaultSorteringsordning();
   }

   public Optional<SorteringsordningEntity> getSorteringsordningById(UUID id)
   {
      return storage.getSorteringsordningById(id);
   }

   /**
    * Returns a paginated page of sorteringsordningar ordered by creation time descending.
    *
    * @param limit  maximum items per page
    * @param offset zero-based start index
    * @return the page slice and total count
    */
   public SorteringsordningEntityPage getSorteringsordningarPage(int limit, int offset)
   {
      return storage.findSorteringsordningarPage(limit, offset);
   }

   /**
    * Deletes the sorteringsordning with the given id.
    *
    * @param id the UUID of the sorteringsordning to delete
    */
   public void deleteSorteringsordning(UUID id)
   {
      storage.deleteSorteringsordning(id);
   }

   /**
    * Promotes the sorteringsordning with the given id to the system default.
    *
    * @param id the UUID of the sorteringsordning to set as default
    */
   public void setDefaultSorteringsordning(UUID id)
   {
      storage.setDefaultSorteringsordning(id);
   }

   private void notifyStatusUpdate(UppgiftEntity uppgift)
   {
      var statusMessage = logicMapper.toStatusMessage(uppgift);
      producer.publishTaskStatusUpdate(statusMessage, uppgift.subTopic());
   }
}
