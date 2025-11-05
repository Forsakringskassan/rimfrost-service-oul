package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.ImmutableUppgift;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.ImmutableUppgiftGetAllResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.ImmutableUppgiftGetResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.ImmutableUppgiftStatusUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.Uppgift;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.UppgiftGetAllResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.UppgiftGetResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto.UppgiftStatusUpdateResponse;

@ApplicationScoped
public class PresentationRestMapper
{
   public UppgiftGetResponse toUppgiftGetResponse(UppgiftEntity uppgift)
   {
      return ImmutableUppgiftGetResponse.builder()
            .uppgift(toUppgift(uppgift))
            .build();
   }

   public UppgiftGetAllResponse toUppgiftGetAllResponse(Collection<UppgiftEntity> uppgifter)
   {
      Collection<Uppgift> uppgifterDto = new ArrayList<>();
      for (UppgiftEntity uppgiftEntity : uppgifter)
      {
         uppgifterDto.add(toUppgift(uppgiftEntity));
      }

      return ImmutableUppgiftGetAllResponse.builder()
            .uppgifter(uppgifterDto)
            .build();
   }

   public Uppgift toUppgift(UppgiftEntity uppgift)
   {
      return ImmutableUppgift.builder()
            .uppgiftId(uppgift.uppgiftId().toString())
            .status(uppgift.status().name())
            .beskrivning(uppgift.beskrivning())
            .handlaggarId(uppgift.handlaggarId().toString())
            .build();
   }

   public UppgiftStatusUpdateResponse toUppgiftStatusUpdateResponse(
         OperativtUppgiftslagerUpdateResponse response)
   {
      var uppgift = ImmutableUppgift.builder()
            .uppgiftId(response.uppgiftId().toString())
            .status(response.status())
            .beskrivning(response.beskrivning())
            .handlaggarId(response.handlaggarId())
            .build();

      return ImmutableUppgiftStatusUpdateResponse.builder()
            .uppgift(uppgift)
            .build();
   }
}
