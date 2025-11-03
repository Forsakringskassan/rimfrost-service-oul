package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.OperativtUpggiftslagerProducer;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@ApplicationScoped
public class OperativtUppgiftslagerService
{
   @Inject
   LogicMapper logicMapper;

   @Inject
   OperativtUpggiftslagerProducer producer;

   private final ConcurrentHashMap<UUID, UppgiftEntity> tasks = new ConcurrentHashMap<>();   

   public void addOperativeTask(OperativtUppgiftslagerAddRequest addRequest)
   {
      //Kanske vara i amppern?
      var uppgift = ImmutableUppgiftEntity.builder()
         .personnummer(addRequest.personNummer())
         .processId(addRequest.processId())
         .beskrivning(addRequest.uppgift())
         .status(UppgiftStatus.NY)
         .build();

      tasks.put(addRequest.processId(), uppgift);
   }

   public Collection<UppgiftEntity> getUppgifter()
   {
      return tasks.values();
   }

   public UppgiftEntity getUppgift(UUID id)
   {
      return tasks.get(id);
   }

   public OperativtUppgiftslagerUpdateResponse updateOperativeTask(UUID id, UppgiftStatus newStatus)
   {
      var uppgift = tasks.get(id);

      if (uppgift == null) return null;

      //FIXA MED ToBuilder eller något
      //uppgift.status = newStatus;
     // uppgift.version++;
      //uppgift.andrad = OffsetDateTime.now();

      if(newStatus == UppgiftStatus.AVSLUTAD)
      {
         notifyTaskCompleted(uppgift);
      }

      return logicMapper.toOperativtUppgiftslagerUpdateResponse(uppgift);
   }

   public void notifyTaskCompleted(UppgiftEntity uppgift)
   {
      producer.publishTaskResponse(logicMapper.toOperativtUppgiftslagerNotification(uppgift));
   }
}
