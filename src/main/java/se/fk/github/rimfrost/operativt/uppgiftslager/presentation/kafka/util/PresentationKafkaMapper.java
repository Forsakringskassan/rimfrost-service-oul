package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerRequestMetadata;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerRequestMetadata;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessageData;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessagePayload;

@ApplicationScoped
public class PresentationKafkaMapper
{
   public OperativtUppgiftslagerAddRequest mapToLogicOulAddRequest(
         OperativtUppgiftslagerRequestMessageData operativtUppgiftslagerRequestData)
   {
      return ImmutableOperativtUppgiftslagerAddRequest.builder()
            .personNummer(operativtUppgiftslagerRequestData.getPersonNummer())
            .processId(UUID.fromString(operativtUppgiftslagerRequestData.getProcessId()))
            .uppgift(operativtUppgiftslagerRequestData.getUppgiftsBeskrivning())
            .build();
   }

   public OperativtUppgiftslagerRequestMetadata mapToLogicOulAddRequestMetadata(
         OperativtUppgiftslagerRequestMessagePayload operativtUppgiftslagerRequest)
   {

      return ImmutableOperativtUppgiftslagerRequestMetadata.builder()
            .specversion(operativtUppgiftslagerRequest.getSpecversion().name())
            .id(UUID.fromString(operativtUppgiftslagerRequest.getId()))
            .source(operativtUppgiftslagerRequest.getSource())
            .type(operativtUppgiftslagerRequest.getType())
            .kogitoparentprociid(UUID.fromString(operativtUppgiftslagerRequest.getKogitoparentprociid()))
            .kogitorootprocid(operativtUppgiftslagerRequest.getKogitorootprocid())
            .kogitoproctype(operativtUppgiftslagerRequest.getKogitoproctype().name())
            .kogitoprocinstanceid(UUID.fromString(operativtUppgiftslagerRequest.getKogitoprocinstanceid()))
            .kogitoprocist(operativtUppgiftslagerRequest.getKogitoprocist())
            .kogitoprocversion(operativtUppgiftslagerRequest.getKogitoprocversion())
            .kogitorootprociid(UUID.fromString(operativtUppgiftslagerRequest.getKogitoparentprociid()))
            .kogitoprocid(operativtUppgiftslagerRequest.getKogitoprocid())
            .build();
   }
}
