package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import java.util.Collection;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.*;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.OperativUppgift.StatusEnum;

@ApplicationScoped
public class PresentationRestMapper
{
   public GetUppgifterHandlaggareResponse toGetUppgifterHandlaggareResponse(Collection<UppgiftDto> uppgifter)
   {
      GetUppgifterHandlaggareResponse response = new GetUppgifterHandlaggareResponse();
      for (var uppgiftDto : uppgifter)
      {
         response.addOperativaUppgifterItem(toUppgift(uppgiftDto));
      }
      return response;
   }

   private OperativUppgift toUppgift(UppgiftDto uppgiftDto)
   {
      OperativUppgift uppgift = new OperativUppgift();
      if (uppgiftDto != null)
      {
         System.out.printf("toUppgift uppgiftDto is not null: %s%n", uppgiftDto);
         uppgift.setKundbehovsflodeId(uppgiftDto.kundbehovsflodeId());
         uppgift.setUppgiftId(uppgiftDto.uppgiftId());
         uppgift.setHandlaggarId(uppgiftDto.handlaggarId());
         uppgift.skapad(uppgiftDto.skapad());
         uppgift.planeradTill(uppgiftDto.planeradTill());
         uppgift.utford(uppgiftDto.utford());
         uppgift.setStatus(mapStatus(uppgiftDto.status()));
         uppgift.setRegel(uppgiftDto.regel());
         uppgift.setKundbehov(uppgiftDto.kundbehov());
         uppgift.setBeskrivning(uppgiftDto.beskrivning());
         uppgift.setVerksamhetslogik(uppgiftDto.verksamhetslogik());
         uppgift.setRoll(uppgiftDto.roll());
         uppgift.setUrl(uppgiftDto.url());
      }
      return uppgift;
   }

   private StatusEnum mapStatus(UppgiftStatus status)
   {
      switch (status)
      {
         case NY:
            return StatusEnum.NY;
         case TILLDELAD:
            return StatusEnum.TILLDELAD;
         case AVSLUTAD:
         default:
            return StatusEnum.AVSLUTAD;
      }
   }

   public PostUppgifterHandlaggareResponse toPostUppgifterHandlaggareResponse(UppgiftDto uppgiftEntity)
   {
      PostUppgifterHandlaggareResponse response = new PostUppgifterHandlaggareResponse();
      if (uppgiftEntity != null)
      {
         response.setOperativUppgift(toUppgift(uppgiftEntity));
      }
      return response;
   }
}
