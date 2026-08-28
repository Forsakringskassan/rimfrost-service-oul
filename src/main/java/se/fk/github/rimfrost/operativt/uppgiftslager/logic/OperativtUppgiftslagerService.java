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
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SidNotAuthorizedException;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SorteringsordningNotFoundException;
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
    *
    * @param idTyp        the handläggare identity type id
    * @param handlaggarId the handläggare identity value
    * @return ordered collection of assigned uppgifter
    */
   public Collection<UppgiftDto> getUppgifterHandlaggare(String idTyp, String handlaggarId)
   {
      log.info("Getting all tasks for handlaggarId: {}", handlaggarId);
      var handlaggare = ImmutableIdtyp.builder()
            .typId(idTyp)
            .varde(handlaggarId)
            .build();
      var sorteringsordning = storage.getDefaultSorteringsordning()
            .orElse(new SorteringsordningEntity(null, null, null, List.of()));
      var uppgifter = storage.findAllUppgifterByHandlaggarId(handlaggare, sorteringsordning);
      return uppgifter.stream().map(logicMapper::toUppgiftDto).toList();
   }

   /**
    * Returns all uppgifter assigned to any member of the caller's team, sorted according to the
    * default sorteringsordning.
    * Throws {@link NotTeamMemberException} (→ HTTP 403) if the caller belongs to no known team (OUL-FR-17.4).
    * Returns an empty list if the caller's team(s) have no members.
    *
    * @param callerHandlaggare the calling handläggare's identity (used to determine team)
    * @return ordered collection of team uppgifter
    */
   public Collection<UppgiftDto> getUppgifterTeam(Idtyp callerHandlaggare)
   {
      log.info("Getting all team tasks for handlaggarId: {}", callerHandlaggare.varde());
      // throws NotTeamMemberException (→ 403) if the caller belongs to no known team
      var teamMembers = teamService.teamMembers(callerHandlaggare);
      if (teamMembers.isEmpty())
      {
         return List.of();
      }
      var sorteringsordning = storage.getDefaultSorteringsordning()
            .orElse(new SorteringsordningEntity(null, null, null, List.of()));
      var uppgifter = storage.findAllUppgifterByTeam(teamMembers, sorteringsordning);
      return uppgifter.stream().map(logicMapper::toUppgiftDto).toList();
   }

   /**
    * Reassigns the given uppgift to the calling handläggare.
    * Throws {@link UppgiftNotFoundException} (→ HTTP 404) if the uppgift does not exist.
    * Throws {@link NotTeamMemberException} (→ HTTP 403) if the current assignee is not a team member.
    * Throws {@link SidNotAuthorizedException} (→ HTTP 403) if the uppgift is SID-märkt and the
    * caller lacks SID-behörighet — unlike {@link #assignNewTask}, there is no "next uppgift" to
    * fall back to, so the attempt is rejected outright and the uppgift is left untouched.
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

      // Same SID-authorization rule as PanacheOulDataStorage.assignNewUppgift — kept in sync by
      // hand since that layer can't depend on TeamService; update both on any change.
      if (!resolveSidBehorighet(callerHandlaggare) && resolveContainsSid(current.handlaggningId(), uppgiftId))
      {
         throw new SidNotAuthorizedException(uppgiftId);
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
    * (once per call rather than once per retry) and {@link #reassignUppgift}. Fails open
    * (treats as {@code false}, i.e. no behörighet) on any failure beyond a plain "not found" —
    * a Team API outage should degrade to the old unconditional-skip/reject behaviour for SID
    * uppgifter, not turn assignment or ommarkering into a hard failure whenever the uppgift in
    * question happens to be SID-marked.
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
    * Resolves whether the given uppgift's handläggning is SID-märkt, for {@link #reassignUppgift}.
    * Fails closed (treats as {@code true}, i.e. potentially SID-märkt) on any read failure —
    * unlike {@link #resolveSidBehorighet}, ommarkering has no "next uppgift" to fall back to
    * (FKPOC-939), so when SID status can't be confirmed the safe default is to reject the
    * attempt (→ 403 via {@link SidNotAuthorizedException}) rather than either let the read
    * failure surface as an uncaught 500, or silently let a possibly SID-märkt uppgift through
    * to an unconfirmed handläggare.
    *
    * @param handlaggningId the uppgift's handläggning id
    * @param uppgiftId      the uppgift id, used only for logging context
    * @return whether the handläggning is SID-märkt, or {@code true} if that could not be determined
    */
   private boolean resolveContainsSid(UUID handlaggningId, UUID uppgiftId)
   {
      try
      {
         return storage.containsSid(handlaggningId, uppgiftId);
      }
      catch (RuntimeException e)
      {
         log.warn("Failed to resolve SID status for handlaggningId: {} and uppgiftId: {}; treating as SID-märkt",
               handlaggningId, uppgiftId, e);
         return true;
      }
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
