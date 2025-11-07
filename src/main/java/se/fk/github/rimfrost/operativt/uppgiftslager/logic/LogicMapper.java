package se.fk.github.rimfrost.operativt.uppgiftslager.logic;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.OperativtUppgiftslagerUpdateResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.RequestMetadataEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.BeslutUtfall;
import se.fk.rimfrost.KogitoProcType;
import se.fk.rimfrost.OperativtUppgiftslagerResponseMessageData;
import se.fk.rimfrost.OperativtUppgiftslagerResponseMessagePayload;
import se.fk.rimfrost.SpecVersion;
import se.fk.rimfrost.Status;

@ApplicationScoped
public class LogicMapper
{

   public OperativtUppgiftslagerResponseMessageData toOperativtUppgiftslagerResponseData(UppgiftEntity uppgift)
   {
      var data = new OperativtUppgiftslagerResponseMessageData();
      data.setBeslutUtfall(BeslutUtfall.BEVILJAT); //TODO should be part of uppgift.beslut()
      data.setPersonnummer(uppgift.personnummer());
      data.setProcessId(uppgift.processId().toString());
      data.setStatus(mapStatus(uppgift.status()));
      return data;
   }

   public OperativtUppgiftslagerUpdateResponse toOperativtUppgiftslagerUpdateResponse(UppgiftEntity uppgift)
   {
      return ImmutableOperativtUppgiftslagerUpdateResponse.builder()
            .beskrivning(uppgift.beskrivning())
            .handlaggarId(uppgift.handlaggarId())
            .status(uppgift.status().name())
            .uppgiftId(uppgift.uppgiftId())
            .build();
   }

   public OperativtUppgiftslagerResponseMessagePayload toOperativtUppgiftslagerResponsePayload(RequestMetadataEntity metadata,
         OperativtUppgiftslagerResponseMessageData responseData)
   {
      var payload = new OperativtUppgiftslagerResponseMessagePayload();
      payload.setData(responseData);
      payload.setSpecversion(SpecVersion.NUMBER_1_DOT_0);
      payload.setId(metadata.id().toString());
      payload.setSource(metadata.source());
      payload.setType("operativt-uppgiftslager-responses");
      payload.setKogitoparentprociid(metadata.kogitoparentprociid().toString());
      payload.setKogitoprocid(metadata.kogitoprocid());
      payload.setKogitoprocinstanceid(metadata.kogitoprocinstanceid().toString());
      payload.setKogitoprocist(metadata.kogitoprocist());
      payload.setKogitoprocrefid(metadata.kogitoprocinstanceid().toString());
      payload.setKogitoproctype(KogitoProcType.BPMN);
      payload.setKogitoprocversion(metadata.kogitoprocversion());
      payload.setKogitorootprocid(metadata.kogitorootprocid());
      payload.setKogitorootprociid(metadata.kogitorootprociid().toString());
      return payload;
   }

   public Status mapStatus(UppgiftStatus status)
   {
      switch (status)
      {
         case UppgiftStatus.NY:
            return Status.NY;
         case UppgiftStatus.TILLDELAD:
            return Status.TILLDELAD;
         case UppgiftStatus.AVSLUTAD:
         default:
            return Status.AVSLUTAD;
      }
   }
}
