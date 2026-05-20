package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;

@ApplicationScoped
public class ManagementMapper
{
   @Inject
   EnumMapper enumMapper;

   public OperativtUppgiftslagerAddRequest toAddRequest(CreateUppgiftRequest request)
   {
      return ImmutableOperativtUppgiftslagerAddRequest.builder()
            .version(request.getVersion())
            .handlaggningId(request.getHandlaggningId())
            .individer(toIdtyper(request.getIndivider()))
            .regel(request.getRegel())
            .beskrivning(request.getBeskrivning())
            .verksamhetslogik(request.getVerksamhetslogik())
            .roll(request.getRoll())
            .url(request.getUrl())
            .build();
   }

   public List<OperativUppgift> toOperativUppgiftList(List<UppgiftDto> uppgiftDtoList)
   {
      return uppgiftDtoList.stream().map(u -> {
         OperativUppgift uppgift = new OperativUppgift();
         uppgift.setUppgiftId(u.uppgiftId());
         uppgift.setHandlaggningId(u.handlaggningId());
         uppgift.setSkapad(u.skapad());
         uppgift.handlaggarId(toIdtyp(u.handlaggarId()));
         uppgift.planeradTill(u.planeradTill());
         uppgift.utford(u.utford());
         uppgift.status(enumMapper.mapUppgiftStatusToStatus(u.status()));
         uppgift.individer(Arrays.stream(u.individer()).map(this::toIdtyp).toList());
         uppgift.setRegel(u.regel());
         uppgift.beskrivning(u.beskrivning());
         uppgift.verksamhetslogik(u.verksamhetslogik());
         uppgift.roll(u.roll());
         uppgift.url(u.url());
         return uppgift;
      }).toList();
   }

   private Idtyp[] toIdtyper(List<se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp> individer)
   {
      if (individer == null)
      {
         return new Idtyp[0];
      }
      return individer.stream()
            .map(i -> ImmutableIdtyp.builder().typId(i.getTypId()).varde(i.getVarde()).build())
            .toArray(Idtyp[]::new);
   }

   private se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp toIdtyp(Idtyp individ)
   {
      if (individ == null)
      {
         return null;
      }

      se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp idtyp = new se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp();
      idtyp.setTypId(individ.typId());
      idtyp.setVarde(individ.varde());
      return idtyp;
   }
}
