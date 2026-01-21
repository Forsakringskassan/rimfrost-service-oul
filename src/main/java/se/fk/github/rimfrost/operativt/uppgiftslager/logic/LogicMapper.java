package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableUppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.UppgiftDto;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.*;

@ApplicationScoped
public class LogicMapper
{
   @Inject
   EnumMapper enumMapper;

   public OperativtUppgiftslagerStatusMessage toStatusMessage(UppgiftEntity uppgift)
   {
      var data = new OperativtUppgiftslagerStatusMessage();
      data.setKundbehovsflodeId(uppgift.kundbehovsflodeId().toString());
      data.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      data.setUppgiftId(uppgift.uppgiftId().toString());
      data.setUtforarId(uppgift.handlaggarId().toString());
      return data;
   }

   public UppgiftDto toUppgiftDto(UppgiftEntity uppgift)
   {
      return ImmutableUppgiftDto.builder()
            .kundbehovsflodeId(uppgift.kundbehovsflodeId())
            .uppgiftId(uppgift.uppgiftId())
            .handlaggarId(uppgift.handlaggarId())
            .skapad(uppgift.skapad())
            .planeradTill(uppgift.planeradTill())
            .utford(uppgift.utford())
            .status(uppgift.status())
            .regel(uppgift.regel())
            .kundbehov(uppgift.kundbehov())
            .beskrivning(uppgift.beskrivning())
            .verksamhetslogik(uppgift.verksamhetslogik())
            .roll(uppgift.roll())
            .url(uppgift.url())
            .build();
   }
}
