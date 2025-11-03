package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.ImmutableOperativtUppgiftslagerResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;

@ApplicationScoped
public class LogicMapper
{

   public OperativtUppgiftslagerResponse toOperativtUppgiftslagerResponse(UppgiftEntity uppgift)
   {
      return ImmutableOperativtUppgiftslagerResponse.builder()
            .processId(uppgift.processId())
            .personNummer(uppgift.personnummer())
            .resultat(true)
            .status("AVSLUTAD")
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
