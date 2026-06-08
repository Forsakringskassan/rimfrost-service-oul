package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.SortOrderApplier;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.SortedUppgiftPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka.OperativtUppgiftslagerProducer;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.OulDataStorage;

@ApplicationScoped
public class OperativtUppgiftslagerService
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerService.class);

   @Inject
   LogicMapper logicMapper;

   @Inject
   OperativtUppgiftslagerProducer producer;

   @Inject
   SortOrderApplier sortOrderApplier;

   @Inject
   OulDataStorage storage;

   public UppgiftDto addOperativeTask(OperativtUppgiftslagerAddRequest addRequest, String notificationTopic,
         Map<String, String> cloudeventAttributes)
   {
      log.info("Adding new task");
      var uppgift = ImmutableUppgiftEntity.builder()
            .uppgiftId(UUID.randomUUID())
            .handlaggningId(addRequest.handlaggningId())
            .skapad(LocalDate.now())
            .status(UppgiftStatus.NY)
            .regel(addRequest.regel())
            .individer(addRequest.individer())
            .beskrivning(addRequest.beskrivning())
            .verksamhetslogik(addRequest.verksamhetslogik())
            .roll(addRequest.roll())
            .url(addRequest.url())
            .subTopic(notificationTopic)
            .cloudeventAttributes(cloudeventAttributes)
            .erbjudande(addRequest.erbjudande())
            .build();

      storage.createUppgift(uppgift);
      return logicMapper.toUppgiftDto(uppgift);
   }

   public UppgiftDto endTask(UUID uppgiftId, String reason)
   {
      log.info("Ending task {} with reason: {}", uppgiftId, reason);
      var task = storage.findUppgiftById(uppgiftId);

      if (task == null)
      {
         return null;
      }

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

   public List<UppgiftDto> getTasks()
   {
      return storage.findAllUppgifter().stream().map(logicMapper::toUppgiftDto).toList();
   }

   public SortedUppgiftPage getUppgifterPage(int limit, int offset, UUID sorteringsordningId)
   {
      var uppgifter = getTasks();

      SorteringsordningEntity sorteringsordning;
      if (sorteringsordningId != null)
      {
         sorteringsordning = storage.getSorteringsordningById(sorteringsordningId).orElse(null);
         if (sorteringsordning == null)
         {
            return null;
         }
      }
      else
      {
         sorteringsordning = storage.getDefaultSorteringsordning()
               .orElse(new SorteringsordningEntity(null, null, List.of()));
      }

      return sortOrderApplier.apply(uppgifter, sorteringsordning, limit, offset);
   }

   public Collection<UppgiftDto> getUppgifterHandlaggare(String idTyp, String handlaggarId)
   {
      log.info("Getting all tasks for handlaggarId: {}", handlaggarId);
      var handlaggare = ImmutableIdtyp.builder()
            .typId(idTyp)
            .varde(handlaggarId)
            .build();
      var uppgifter = storage.findAllUppgifterByHandlaggarId(handlaggare);
      return uppgifter.stream().map(logicMapper::toUppgiftDto).toList();
   }

   public UppgiftDto assignNewTask(String idTyp, String handlaggarId)
   {
      log.info("Assigning new task to handlaggarId: {} with type: {}", handlaggarId, idTyp);
      var handlaggare = ImmutableIdtyp.builder()
            .typId(idTyp)
            .varde(handlaggarId)
            .build();
      var uppgift = storage.assignNewUppgift(handlaggare);

      if (uppgift == null)
      {
         log.info("Failed to assign new task to handlaggarId: {}", handlaggarId);
         return null;
      }

      notifyStatusUpdate(uppgift);
      log.info("Assigned task {} to handlaggarId: {}", uppgift.uppgiftId(), handlaggarId);
      return logicMapper.toUppgiftDto(uppgift);
   }

   public UppgiftDto unassignTask(UUID uppgiftId)
   {
      var uppgift = storage.unassignUppgift(uppgiftId);

      if (uppgift == null)
      {
         return null;
      }

      notifyStatusUpdate(uppgift);
      return logicMapper.toUppgiftDto(uppgift);
   }

   public UppgiftDto updateTask(UUID uppgiftId, Idtyp handlaggarId)
   {
      var uppgift = storage.updateUppgift(uppgiftId, handlaggarId);

      if (uppgift == null)
      {
         return null;
      }

      notifyStatusUpdate(uppgift);
      return logicMapper.toUppgiftDto(uppgift);
   }

   public SortedUppgiftPage previewSorteringsordning(SorteringsordningSpec spec, int limit, int offset)
   {
      var entity = new SorteringsordningEntity(null, null, spec.getEntries());
      return sortOrderApplier.apply(getTasks(), entity, limit, offset);
   }

   public SorteringsordningEntity createSorteringsordning(SorteringsordningSpec spec)
   {
      var entity = new SorteringsordningEntity(UUID.randomUUID(), OffsetDateTime.now(), spec.getEntries());
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

   public List<SorteringsordningEntity> getAllSorteringsordningar()
   {
      return storage.getAllSorteringsordningar();
   }

   private void notifyStatusUpdate(UppgiftEntity uppgift)
   {
      var statusMessage = logicMapper.toStatusMessage(uppgift);
      producer.publishTaskStatusUpdate(statusMessage, uppgift.subTopic());
   }
}
