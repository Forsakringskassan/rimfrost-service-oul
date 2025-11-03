package se.fk.github.rimfrost.operativt.uppgiftslager.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.ImmutableUppgift;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.ImmutableUppgiftGetAllResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.ImmutableUppgiftGetResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.ImmutableUppgiftStatusUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.Uppgift;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftGetAllResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftGetResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftStatusUpdateResponse;

@ApplicationScoped
public class PresentationMapper
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
