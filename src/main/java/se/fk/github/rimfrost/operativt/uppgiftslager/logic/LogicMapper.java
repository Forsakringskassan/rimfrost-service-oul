package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.ImmutableOperativtUppgiftslagerNotification;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerNotification;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;

@ApplicationScoped
public class LogicMapper
{

   public OperativtUppgiftslagerNotification toOperativtUppgiftslagerNotification(UppgiftEntity uppgift)
   {
      return ImmutableOperativtUppgiftslagerNotification.builder()
            .uppgift(uppgift.beskrivning())
            .status(uppgift.status().name())
            .processId(uppgift.processId())
            .personNummer(uppgift.personnummer())
            .resultat(true)
            .build();
   }

   public OperativtUppgiftslagerUpdateResponse toOperativtUppgiftslagerUpdateResponse(UppgiftEntity uppgift)
   {
      return ImmutableOperativtUppgiftslagerUpdateResponse.builder()
            .beskrivning(uppgift.beskrivning())
            .handlaggarId(uppgift.handlaggarId())
            .status(uppgift.status().name())
            .uppgiftId(uppgift.uppgiftId())
            .build();
   }
}
