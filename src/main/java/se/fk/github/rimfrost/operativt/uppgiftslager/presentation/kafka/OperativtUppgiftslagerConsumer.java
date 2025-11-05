package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableOperativtUppgiftslagerAddRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.dto.OperativtUppgiftslagerRequest;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class OperativtUppgiftslagerConsumer
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerConsumer.class);

   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @Incoming("operativt-uppgiftslager-requests")
   @Blocking
   public void handleIncomingTask(OperativtUppgiftslagerRequest operativtUppgiftslagerRequest)
   {
      log.info("Received task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);

      //FLytta till mappper
      var addRequest = ImmutableOperativtUppgiftslagerAddRequest.builder()
            .personNummer(operativtUppgiftslagerRequest.personNummer())
            .processId(operativtUppgiftslagerRequest.processId())
            .uppgift(operativtUppgiftslagerRequest.uppgift())
            .build();

      operativtUppgiftslagerService.addOperativeTask(addRequest);
      log.info("Processed task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
   }
}
