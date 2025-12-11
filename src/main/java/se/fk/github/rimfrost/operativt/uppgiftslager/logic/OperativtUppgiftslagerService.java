package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka.OperativtUppgiftslagerProducer;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableUppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerStatusUpdateRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.Status;

@ApplicationScoped
public class OperativtUppgiftslagerService
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerService.class);

   @Inject
   LogicMapper logicMapper;

   @Inject
   OperativtUppgiftslagerProducer producer;

   private final ConcurrentHashMap<UUID, UppgiftEntity> taskMap = new ConcurrentHashMap<>();

   public void addOperativeTask(OperativtUppgiftslagerAddRequest addRequest)
   {
      log.info("Adding new task");
      var uppgift = ImmutableUppgiftEntity.builder()
            .uppgiftId(UUID.randomUUID())
            .kundbehovsflodeId(addRequest.kundbehovsflodeId())
            .skapad(LocalDate.now())
            .status(UppgiftStatus.NY)
            .regelTyp(addRequest.regeltyp())
            .build();

      taskMap.put(uppgift.uppgiftId(), uppgift);
      producer.publishTaskResponse(uppgift.kundbehovsflodeId(), uppgift.uppgiftId());
   }

   public void onTaskStatusUpdated(OperativtUppgiftslagerStatusUpdateRequest statusUpdateRequest)
   {
      log.info("StatusUpdating task {} with status {}", statusUpdateRequest.uppgiftId(), statusUpdateRequest.status());
      var task = taskMap.get(statusUpdateRequest.uppgiftId());
      var updatedTask = ImmutableUppgiftEntity.builder()
            .from(task)
            .status(statusUpdateRequest.status())
            .build();
      taskMap.put(task.uppgiftId(), updatedTask);

      if (updatedTask.status() == UppgiftStatus.AVSLUTAD)
      {
         taskMap.remove(updatedTask.uppgiftId());
      }

      notifyStatusUpdate(updatedTask);
      log.info("Task StatusUpdate finished on {}", updatedTask.uppgiftId());
   }

   public Collection<UppgiftDto> getUppgifterHandlaggare(UUID handlaggarId)
   {
      log.info("Getting all tasks for handlaggarId: " + handlaggarId);
      var uppgifter = taskMap.values();
      var handlaggarTasks = new ArrayList<UppgiftDto>();
      for (UppgiftEntity uppgift : uppgifter)
      {
         if (Objects.equals(uppgift.handlaggarId(), handlaggarId))
         {
            handlaggarTasks.add(logicMapper.toUppgiftDto(uppgift));
         }
      }
      return handlaggarTasks;
   }

   public UppgiftDto assignNewTask(UUID handlaggarId)
   {
      log.info("Assigning new task to handlaggarId: {}", handlaggarId);
      var tasks = taskMap.values();
      for (UppgiftEntity task : tasks)
      {
         if (task.handlaggarId() == null)
         {
            var updatedTask = ImmutableUppgiftEntity.builder()
                  .from(task)
                  .status(UppgiftStatus.TILLDELAD)
                  .handlaggarId(handlaggarId)
                  .build();
            taskMap.put(task.uppgiftId(), updatedTask);
            notifyStatusUpdate(updatedTask);
            log.info("Assigned task {} to handlaggarId: {}", updatedTask.uppgiftId(), handlaggarId);
            return logicMapper.toUppgiftDto(updatedTask);
         }
      }
      log.info("Failed to assign new task to handlaggarId: {}", handlaggarId);
      return null;
   }

   private void notifyStatusUpdate(UppgiftEntity uppgift)
   {
      var statusMessage = logicMapper.toStatusMessage(uppgift);
      producer.publishTaskStatusUpdate(statusMessage);
   }
}
