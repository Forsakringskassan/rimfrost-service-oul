package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import java.util.Collection;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.*;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Uppgift.StatusEnum;

@ApplicationScoped
public class PresentationRestMapper
{
   public GetUppgifterHandlaggareResponse toGetUppgifterHandlaggareResponse(Collection<UppgiftDto> uppgifter)
   {
      GetUppgifterHandlaggareResponse response = new GetUppgifterHandlaggareResponse();
      for (var uppgiftDto : uppgifter)
      {
         response.addUppgifterItem(toUppgift(uppgiftDto));
      }
      return response;
   }

   private Uppgift toUppgift(UppgiftDto uppgiftDto)
   {
      Uppgift uppgift = new Uppgift();
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
         uppgift.setRegeltyp(uppgiftDto.regelTyp());
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
         response.setUppgift(toUppgift(uppgiftEntity));
      }
      return response;
   }
}
