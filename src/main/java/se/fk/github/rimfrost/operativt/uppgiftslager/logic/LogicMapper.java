package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.ZoneId;
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
      var planeradTill = uppgift.planeradTill();

      var data = new OperativtUppgiftslagerStatusMessage();
      data.setHandlaggningId(uppgift.handlaggningId().toString());
      data.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      data.setUppgiftId(uppgift.uppgiftId().toString());
      var processInfo = new ProcessInfo();
      processInfo.setCloudeventAttributes(uppgift.cloudeventAttributes());
      processInfo.setReplyTopic(uppgift.replyTopic());
      data.setProcessInfo(processInfo);
      data.setPlaneradTill(
            planeradTill != null ? planeradTill.atStartOfDay().atZone(ZoneId.systemDefault()).toOffsetDateTime() : null);

      var handlaggarId = uppgift.handlaggarId();
      if (handlaggarId != null)
      {
         var utforarId = new Idtyp();
         utforarId.setTypId(handlaggarId.typId());
         utforarId.setVarde(handlaggarId.varde());
         data.setUtforarId(utforarId);
      }

      return data;
   }

   public UppgiftDto toUppgiftDto(UppgiftEntity uppgift)
   {
      return ImmutableUppgiftDto.builder()
            .handlaggningId(uppgift.handlaggningId())
            .uppgiftId(uppgift.uppgiftId())
            .handlaggarId(uppgift.handlaggarId())
            .skapad(uppgift.skapad())
            .planeradTill(uppgift.planeradTill())
            .utford(uppgift.utford())
            .status(uppgift.status())
            .regel(uppgift.regel())
            .individer(uppgift.individer())
            .beskrivning(uppgift.beskrivning())
            .verksamhetslogik(uppgift.verksamhetslogik())
            .roll(uppgift.roll())
            .url(uppgift.url())
            .subTopic(uppgift.subTopic())
            .replyTopic(uppgift.replyTopic())
            .cloudeventAttributes(uppgift.cloudeventAttributes())
            .erbjudande(uppgift.erbjudande())
            .build();
   }

}
