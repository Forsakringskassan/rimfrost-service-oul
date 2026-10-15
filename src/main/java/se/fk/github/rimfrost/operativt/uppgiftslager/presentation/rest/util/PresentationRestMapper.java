package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.UppgiftListResult;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftMedYrkanderollDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.*;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.OperativUppgift.StatusEnum;
import java.util.List;

@ApplicationScoped
public class PresentationRestMapper
{
   public GetUppgifterHandlaggareResponse toGetUppgifterHandlaggareResponse(UppgiftListResult result)
   {
      GetUppgifterHandlaggareResponse response = new GetUppgifterHandlaggareResponse();
      response.setOperativaUppgifter(result.uppgifter().stream().map(this::toUppgift).toList());
      response.setBorttagnaPgaBehorighet(result.borttagnaPgaBehorighet());
      return response;
   }

   private OperativUppgift toUppgift(UppgiftDto uppgiftDto)
   {
      OperativUppgift uppgift = new OperativUppgift();
      if (uppgiftDto != null)
      {
         uppgift.setHandlaggningId(uppgiftDto.handlaggningId());
         uppgift.setUppgiftId(uppgiftDto.uppgiftId());
         uppgift.setHandlaggarId(toApiIdtyp(uppgiftDto.handlaggarId()));
         uppgift.skapad(uppgiftDto.skapad());
         uppgift.planeradTill(uppgiftDto.planeradTill());
         uppgift.utford(uppgiftDto.utford());
         uppgift.setStatus(mapStatus(uppgiftDto.status()));
         uppgift.setRegel(uppgiftDto.regel());
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

   /**
    * Maps a {@link UppgiftDto} to a {@link PostUppgifterHandlaggareResponse}.
    *
    * @param uppgiftEntity the DTO to map, or {@code null} for an empty response
    * @return the mapped response
    */
   public PostUppgifterHandlaggareResponse toPostUppgifterHandlaggareResponse(UppgiftDto uppgiftEntity)
   {
      PostUppgifterHandlaggareResponse response = new PostUppgifterHandlaggareResponse();
      if (uppgiftEntity != null)
      {
         response.setOperativUppgift(toUppgift(uppgiftEntity));
      }
      return response;
   }

   /**
    * Maps a {@link UppgiftDto} to a {@link PostUppgiftHandlaggareResponse}.
    *
    * @param uppgiftDto the reassigned uppgift DTO
    * @return the mapped response
    */
   public PostUppgiftHandlaggareResponse toPostUppgiftHandlaggareResponse(UppgiftDto uppgiftDto)
   {
      PostUppgiftHandlaggareResponse response = new PostUppgiftHandlaggareResponse();
      response.setOperativUppgift(toUppgift(uppgiftDto));
      return response;
   }

   /**
    * Maps a list of {@link UppgiftMedYrkanderollDto} to a {@link PostUppgifterIndividResponse}.
    *
    * @param uppgifter the matched uppgifter paired with their yrkanderoll
    * @return the mapped response
    */
   public PostUppgifterIndividResponse toPostUppgifterIndividResponse(List<UppgiftMedYrkanderollDto> uppgifter)
   {
      PostUppgifterIndividResponse response = new PostUppgifterIndividResponse();
      response.setOperativaUppgifter(uppgifter.stream().map(this::toUppgiftYrkanderoll).toList());
      return response;
   }

   private OperativUppgiftYrkanderoll toUppgiftYrkanderoll(UppgiftMedYrkanderollDto dto)
   {
      OperativUppgiftYrkanderoll uppgift = new OperativUppgiftYrkanderoll();
      var u = dto.uppgift();
      uppgift.setUppgiftId(u.uppgiftId());
      uppgift.setHandlaggningId(u.handlaggningId());
      uppgift.setSkapad(u.skapad());
      uppgift.setHandlaggarId(toApiIdtyp(u.handlaggarId()));
      uppgift.setPlaneradTill(u.planeradTill());
      uppgift.setUtford(u.utford());
      uppgift.setStatus(mapYrkanderollStatus(u.status()));
      uppgift.setRegel(u.regel());
      uppgift.setBeskrivning(u.beskrivning());
      uppgift.setVerksamhetslogik(u.verksamhetslogik());
      uppgift.setRoll(u.roll());
      uppgift.setUrl(u.url());
      uppgift.setYrkandeRoll(dto.yrkandeRoll());
      return uppgift;
   }

   private OperativUppgiftYrkanderoll.StatusEnum mapYrkanderollStatus(UppgiftStatus status)
   {
      return switch (status)
      {
         case NY -> OperativUppgiftYrkanderoll.StatusEnum.NY;
         case TILLDELAD -> OperativUppgiftYrkanderoll.StatusEnum.TILLDELAD;
         case AVSLUTAD -> OperativUppgiftYrkanderoll.StatusEnum.AVSLUTAD;
         case AVBRUTEN -> OperativUppgiftYrkanderoll.StatusEnum.AVBRUTEN;
      };
   }

   /**
    * Maps a nullable list of {@link PostUppgifterIndividRequest.StatusesEnum} to domain
    * {@link UppgiftStatus} values. A null input is treated as "no filter" and returns an empty list.
    *
    * @param statuses the API status filter, or {@code null}
    * @return the corresponding domain enum values, never null
    */
   public List<UppgiftStatus> toUppgiftStatuses(List<PostUppgifterIndividRequest.StatusesEnum> statuses)
   {
      if (statuses == null)
      {
         return List.of();
      }
      return statuses.stream().map(this::toUppgiftStatus).toList();
   }

   private UppgiftStatus toUppgiftStatus(PostUppgifterIndividRequest.StatusesEnum statusesEnum)
   {
      return switch (statusesEnum)
      {
         case NY -> UppgiftStatus.NY;
         case TILLDELAD -> UppgiftStatus.TILLDELAD;
         case AVSLUTAD -> UppgiftStatus.AVSLUTAD;
         case AVBRUTEN -> UppgiftStatus.AVBRUTEN;
      };
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
