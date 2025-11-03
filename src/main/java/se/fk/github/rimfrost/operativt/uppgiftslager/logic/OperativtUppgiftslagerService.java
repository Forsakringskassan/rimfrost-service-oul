package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.OperativtUpggiftslagerProducer;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@ApplicationScoped
public class OperativtUppgiftslagerService
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerService.class);

   @Inject
   LogicMapper logicMapper;

   @Inject
   OperativtUpggiftslagerProducer producer;

   private final ConcurrentHashMap<Long, UppgiftEntity> taskMap = new ConcurrentHashMap<>();
   private AtomicLong idCounter = new AtomicLong();

   public void addOperativeTask(OperativtUppgiftslagerAddRequest addRequest)
   {
      //Kanske vara i mappern?
      log.info("Adding new task");
      var uppgift = ImmutableUppgiftEntity.builder()
            .personnummer(addRequest.personNummer())
            .processId(addRequest.processId())
            .beskrivning(addRequest.uppgift())
            .status(UppgiftStatus.NY)
            .uppgiftId(idCounter.incrementAndGet())
            .handlaggarId("")
            .build();

      taskMap.put(uppgift.uppgiftId(), uppgift);
      log.info("Added new task");
   }

   public Collection<UppgiftEntity> getUppgifter()
   {
      log.info("Getting all tasks");
      OperativtUppgiftslagerAddRequest dummyRequest = ImmutableOperativtUppgiftslagerAddRequest.builder()
            .processId(UUID.randomUUID())
            .personNummer("1991-01-01-9991")
            .uppgift("Dummy uppgift")
            .build();
      addOperativeTask(dummyRequest);
      var tasks = taskMap.values();
      for (UppgiftEntity task : tasks) {
         log.info("Task ID: {}, Description: {}, Status: {}", task.uppgiftId(), task.beskrivning(), task.status());
      }
      return tasks;
   }

   public UppgiftEntity getUppgift(Long id)
   {
      log.info("Id is {}", id);
      var task = taskMap.get(id);
      if (task == null) {
         log.info("Task with ID {} not found", id);
         return null;
      }
      log.info("Task ID: {}, Description: {}, Status: {}", task.uppgiftId(), task.beskrivning(), task.status());
      return task;
   }

   public OperativtUppgiftslagerUpdateResponse updateOperativeTask(Long uppgiftId, UppgiftStatus newStatus)
   {
      var uppgift = taskMap.get(uppgiftId);

      if (uppgift == null)
         return null;

      var updatedUppgift = ImmutableUppgiftEntity.builder()
            .from(uppgift)
            .status(newStatus)
            .build();

      taskMap.put(uppgiftId, updatedUppgift);

      if (newStatus == UppgiftStatus.AVSLUTAD)
      {
         notifyTaskCompleted(updatedUppgift);
      }

      return logicMapper.toOperativtUppgiftslagerUpdateResponse(updatedUppgift);
   }

   public void notifyTaskCompleted(UppgiftEntity uppgift)
   {
      producer.publishTaskResponse(logicMapper.toOperativtUppgiftslagerResponse(uppgift));
   }
}
