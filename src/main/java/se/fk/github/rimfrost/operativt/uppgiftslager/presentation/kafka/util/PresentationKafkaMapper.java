package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.*;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessage;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

@ApplicationScoped
public class PresentationKafkaMapper
{
   @Inject
   EnumMapper enumMapper;

   public OperativtUppgiftslagerAddRequest toAddRequest(
         OperativtUppgiftslagerRequestMessage operativtUppgiftslagerRequestMessage, String subTopic)
   {
      return ImmutableOperativtUppgiftslagerAddRequest.builder()
            .version(operativtUppgiftslagerRequestMessage.getVersion())
            .kundbehovsflodeId(UUID.fromString(operativtUppgiftslagerRequestMessage.getKundbehovsflodeId()))
            .kundbehov(operativtUppgiftslagerRequestMessage.getKundbehov())
            .regel(operativtUppgiftslagerRequestMessage.getRegel())
            .beskrivning(operativtUppgiftslagerRequestMessage.getBeskrivning())
            .verksamhetslogik(operativtUppgiftslagerRequestMessage.getVerksamhetslogik())
            .roll(operativtUppgiftslagerRequestMessage.getRoll())
            .url(operativtUppgiftslagerRequestMessage.getUrl())
            .subTopic(subTopic)
            .build();
   }

   public OperativtUppgiftslagerStatusUpdateRequest toStatusUpdateRequest(
         OperativtUppgiftslagerStatusMessage operativtUppgiftslagerStatusMessage)
   {
      return ImmutableOperativtUppgiftslagerStatusUpdateRequest.builder()
            .uppgiftId(UUID.fromString(operativtUppgiftslagerStatusMessage.getUppgiftId()))
            .status(enumMapper.mapStatusToUppgiftStatus(operativtUppgiftslagerStatusMessage.getStatus()))
            .build();
   }

}
