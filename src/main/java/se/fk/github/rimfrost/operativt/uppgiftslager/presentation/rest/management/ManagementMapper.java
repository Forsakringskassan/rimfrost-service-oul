package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Erbjudande;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableErbjudande;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.SortedUppgiftPage;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.SorteringsordningEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;

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
            .erbjudande(toErbjudande(request.getErbjudande()))
            .build();
   }

   public UppgiftPage toUppgiftPage(SortedUppgiftPage page)
   {
      return new UppgiftPage(page.total(), toOperativUppgiftList(page.items()));
   }

   public SorteringsordningResponse toSorteringsordningResponse(SorteringsordningEntity entity)
   {
      return new SorteringsordningResponse(entity.id(), entity.skapad(), entity.entries());
   }

   public List<OperativUppgift> toOperativUppgiftList(List<UppgiftDto> uppgiftDtoList)
   {
      return uppgiftDtoList.stream().map(this::toOperativUppgift).toList();
   }

   public OperativUppgift toOperativUppgift(UppgiftDto uppgiftDto)
   {
      OperativUppgift uppgift = new OperativUppgift();
      uppgift.setUppgiftId(uppgiftDto.uppgiftId());
      uppgift.setHandlaggningId(uppgiftDto.handlaggningId());
      uppgift.setSkapad(uppgiftDto.skapad());
      uppgift.handlaggarId(toIdtyp(uppgiftDto.handlaggarId()));
      uppgift.planeradTill(uppgiftDto.planeradTill());
      uppgift.utford(uppgiftDto.utford());
      uppgift.status(enumMapper.mapUppgiftStatusToStatus(uppgiftDto.status()));
      uppgift.individer(Arrays.stream(uppgiftDto.individer()).map(this::toIdtyp).toList());
      uppgift.setRegel(uppgiftDto.regel());
      uppgift.beskrivning(uppgiftDto.beskrivning());
      uppgift.verksamhetslogik(uppgiftDto.verksamhetslogik());
      uppgift.roll(uppgiftDto.roll());
      uppgift.url(uppgiftDto.url());
      uppgift.erbjudande(toErbjudande(uppgiftDto.erbjudande()));
      return uppgift;
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

   public Idtyp toIdTyp(se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp idtyp)
   {
      return ImmutableIdtyp.builder()
            .typId(idtyp.getTypId())
            .varde(idtyp.getVarde())
            .build();
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

   private Erbjudande toErbjudande(
         se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande erbjudande)
   {
      var e = Objects.requireNonNull(erbjudande);

      return ImmutableErbjudande.builder()
            .id(e.getId())
            .namn(e.getNamn())
            .build();
   }

   private se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande toErbjudande(
         Erbjudande erbjudande)
   {
      var e = Objects.requireNonNull(erbjudande);

      se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande modelErbjudande = new se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande();
      modelErbjudande.setId(e.id());
      modelErbjudande.setNamn(e.namn());
      return modelErbjudande;
   }
}
