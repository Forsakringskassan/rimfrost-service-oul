package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import java.util.Arrays;
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
         uppgift.setHandlaggningId(uppgiftDto.handlaggningId());
         uppgift.setUppgiftId(uppgiftDto.uppgiftId());
         uppgift.setHandlaggarId(toApiIdtyp(uppgiftDto.handlaggarId()));
         uppgift.skapad(uppgiftDto.skapad());
         uppgift.planeradTill(uppgiftDto.planeradTill());
         uppgift.utford(uppgiftDto.utford());
         uppgift.setStatus(mapStatus(uppgiftDto.status()));
         uppgift.setRegel(uppgiftDto.regel());
         uppgift.setIndivider(Arrays.stream(uppgiftDto.individer()).map(this::toApiIdtyp).toList());
         uppgift.setBeskrivning(uppgiftDto.beskrivning());
         uppgift.setVerksamhetslogik(uppgiftDto.verksamhetslogik());
         uppgift.setRoll(uppgiftDto.roll());
         uppgift.setUrl(uppgiftDto.url());
      }
      return uppgift;
   }

   private StatusEnum mapStatus(UppgiftStatus status)
   {
      return switch (status)
      {
         case NY -> StatusEnum.NY;
         case TILLDELAD -> StatusEnum.TILLDELAD;
         case AVSLUTAD -> StatusEnum.AVSLUTAD;
         case AVBRUTEN -> StatusEnum.AVBRUTEN;
      };
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

   private Idtyp toApiIdtyp(se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp idTyp)
   {
      if (idTyp == null)
      {
         return null;
      }

      Idtyp apiIdtyp = new Idtyp();
      apiIdtyp.setTypId(idTyp.typId());
      apiIdtyp.setVarde(idTyp.varde());

      return apiIdtyp;
   }
}
