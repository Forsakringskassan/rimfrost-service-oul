package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerRequestMetadata;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.RequestMetadataEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableRequestMetadataEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@ApplicationScoped
public class OperativtUppgiftslagerService
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerService.class);

   @Inject
   LogicMapper logicMapper;

   private final ConcurrentHashMap<Long, UppgiftEntity> taskMap = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<UUID, RequestMetadataEntity> metadataMap = new ConcurrentHashMap<>();
   private AtomicLong idCounter = new AtomicLong();

   public void addOperativeTask(OperativtUppgiftslagerAddRequest addRequest,
         OperativtUppgiftslagerRequestMetadata requestMetadata)
   {
      log.info("Adding new task");
      var uppgift = ImmutableUppgiftEntity.builder()
            .personnummer(addRequest.personNummer())
            .processId(addRequest.processId())
            .beskrivning(addRequest.uppgift())
            .status(UppgiftStatus.NY)
            .uppgiftId(idCounter.incrementAndGet())
            .handlaggarId("")
            .build();

      var metadata = ImmutableRequestMetadataEntity.builder()
            .specversion(requestMetadata.specversion())
            .id(requestMetadata.id())
            .source(requestMetadata.source())
            .type(requestMetadata.type())
            .time(requestMetadata.time())
            .kogitoparentprociid(requestMetadata.kogitoparentprociid())
            .kogitorootprocid(requestMetadata.kogitorootprocid())
            .kogitoproctype(requestMetadata.kogitoproctype())
            .kogitoprocinstanceid(requestMetadata.kogitoprocinstanceid())
            .kogitoprocist(requestMetadata.kogitoprocist())
            .kogitoprocversion(requestMetadata.kogitoprocversion())
            .kogitorootprociid(requestMetadata.kogitoparentprociid())
            .kogitoprocid(requestMetadata.kogitoprocid())
            .build();

      taskMap.put(uppgift.uppgiftId(), uppgift);
      metadataMap.put(uppgift.processId(), metadata);
      log.info("Added new task");
   }

   public Collection<UppgiftEntity> getUppgifter()
   {
      log.info("Getting all tasks");
      var tasks = taskMap.values();
      for (UppgiftEntity task : tasks)
      {
         log.info("Task ID: {}, Description: {}, Status: {}", task.uppgiftId(), task.beskrivning(), task.status());
      }
      return tasks;
   }

   public Collection<UppgiftEntity> getUppgifterHandlaggare(String handlaggarId)
   {
      log.info("Getting all tasks for handlaggarId: " + handlaggarId);
      var tasks = taskMap.values();
      List<UppgiftEntity> handlaggarTasks = new ArrayList<>();
      for (UppgiftEntity task : tasks)
      {
         if (Objects.equals(task.handlaggarId(), handlaggarId))
         {
            handlaggarTasks.add(task);
         }
      }
      return handlaggarTasks;
   }

   public UppgiftEntity getUppgift(Long id)
   {
      var task = taskMap.get(id);
      if (task == null)
      {
         log.info("Task with ID {} not found", id);
         return null;
      }
      log.info("Task ID: {}, Description: {}, Status: {}", task.uppgiftId(), task.beskrivning(), task.status());
      return task;
   }

   public UppgiftEntity updateOperativeTask(Long uppgiftId, UppgiftStatus newStatus)
   {
      var uppgift = taskMap.get(uppgiftId);

      if (uppgift == null)
         return null;

      var updatedUppgift = ImmutableUppgiftEntity.builder()
            .from(uppgift)
            .status(newStatus)
            .build();

      taskMap.put(uppgiftId, updatedUppgift);

      return updatedUppgift;
   }

   public UppgiftEntity assignNewTask(String handlaggarId)
   {
      var tasks = taskMap.values();
      for (UppgiftEntity task : tasks)
      {
         if (Objects.equals(task.handlaggarId(), ""))
         {
            var updatedTask = ImmutableUppgiftEntity.builder()
                  .from(task)
                  .status(UppgiftStatus.TILLDELAD)
                  .handlaggarId(handlaggarId)
                  .build();
            taskMap.put(task.uppgiftId(), updatedTask);
            return updatedTask;
         }
      }
      return null;
   }
}
