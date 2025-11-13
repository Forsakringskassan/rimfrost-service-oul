package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.common.NotImplementedYet;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.*;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessageData;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessagePayload;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessagePayload;

@ApplicationScoped
public class PresentationKafkaMapper
{
   @Inject
   EnumMapper enumMapper;

   public OperativtUppgiftslagerAddRequest mapToLogicOulAddRequest(
         OperativtUppgiftslagerRequestMessageData operativtUppgiftslagerRequestData)
   {
      return ImmutableOperativtUppgiftslagerAddRequest.builder()
            .personNummer(operativtUppgiftslagerRequestData.getPersonnummer())
            .processId(UUID.fromString(operativtUppgiftslagerRequestData.getProcessId()))
            .uppgiftSpecId(operativtUppgiftslagerRequestData.getUppgiftspecId())
            .build();
   }

   public OperativtUppgiftslagerAvslutRequest mapToLogicOulStatusMessage(
         OperativtUppgiftslagerStatusMessagePayload operativtUppgiftslagerStatusMessagePayload)
   {
      return ImmutableOperativtUppgiftslagerAvslutRequest.builder()
            .uppgiftId(operativtUppgiftslagerStatusMessagePayload.getUppgiftId())
            .processId(operativtUppgiftslagerStatusMessagePayload.getProcessId())
            .status(enumMapper.mapStatusToUppgiftStatus(operativtUppgiftslagerStatusMessagePayload.getStatus()))
            .personnummer(operativtUppgiftslagerStatusMessagePayload.getPersonnummer())
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
            .time(OffsetDateTime.now())
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
