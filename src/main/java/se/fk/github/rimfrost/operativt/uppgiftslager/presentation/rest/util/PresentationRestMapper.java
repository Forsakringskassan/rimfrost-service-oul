package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import java.util.ArrayList;
import java.util.Collection;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.*;

@ApplicationScoped
public class PresentationRestMapper
{
   public GetUppgiftResponse toGetUppgiftResponse(UppgiftEntity uppgift)
   {
      GetUppgiftResponse response = new GetUppgiftResponse();
      response.setUppgift(toUppgift(uppgift)); // toUppgift returns the DTO `Uppgift` (generated or own)
      return response;
   }

   public GetUppgifterResponse toGetUppgifterResponse(Collection<UppgiftEntity> uppgifter)
   {
      ArrayList<Uppgift> uppgifterDto = new ArrayList<>();
      for (UppgiftEntity uppgiftEntity : uppgifter)
      {
         uppgifterDto.add(toUppgift(uppgiftEntity));
      }
      GetUppgifterResponse response = new GetUppgifterResponse();
      response.setUppgifter(uppgifterDto);
      return response;
   }

   public GetUppgifterHandlaggareResponse toGetUppgifterHandlaggareResponse(Collection<UppgiftEntity> uppgifter)
   {
      ArrayList<Uppgift> uppgifterDto = new ArrayList<>();
      for (UppgiftEntity uppgiftEntity : uppgifter)
      {
         uppgifterDto.add(toUppgift(uppgiftEntity));
      }
      GetUppgifterHandlaggareResponse response = new GetUppgifterHandlaggareResponse();
      response.setUppgifter(uppgifterDto);
      return response;
   }

   public Uppgift toUppgift(UppgiftEntity uppgiftEntity)
   {
      Uppgift uppgift = new Uppgift();
      uppgift.setUppgiftId(uppgiftEntity.uppgiftId().toString());
      uppgift.setStatus(uppgiftEntity.status().toString());
      uppgift.setBeskrivning(uppgiftEntity.beskrivning());
      uppgift.setHandlaggarId(uppgiftEntity.handlaggarId());
      return uppgift;
   }

   public PatchUppgiftResponse toPatchUppgiftResponse(UppgiftEntity uppgiftEntity)
   {
      PatchUppgiftResponse response = new PatchUppgiftResponse();
      response.setUppgift(toUppgift(uppgiftEntity));
      return response;
   }

   public PostUppgifterHandlaggareResponse toPostUppgifterHandlaggareResponse(UppgiftEntity uppgiftEntity)
   {
      PostUppgifterHandlaggareResponse response = new PostUppgifterHandlaggareResponse();
      response.setUppgiftId(uppgiftEntity.uppgiftId().toString());
      response.setUppgiftBeskrivning(uppgiftEntity.beskrivning());
      response.setPersonnummer(uppgiftEntity.personnummer());
      return response;
   }
}
