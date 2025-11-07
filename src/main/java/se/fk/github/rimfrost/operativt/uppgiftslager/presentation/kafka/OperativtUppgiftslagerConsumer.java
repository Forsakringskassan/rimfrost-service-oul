package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util.PresentationKafkaMapper;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessagePayload;

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

   @Inject
   PresentationKafkaMapper mapper;

   @Incoming("operativt-uppgiftslager-requests")
   @Blocking
   public void onOperativtUppgiftsLagerRequest(OperativtUppgiftslagerRequestMessagePayload operativtUppgiftslagerRequest)
   {
      log.info("Received task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);

      var oulAddRequest = mapper.mapToLogicOulAddRequest(operativtUppgiftslagerRequest.getData());
      var oulAddRequestMetadata = mapper.mapToLogicOulAddRequestMetadata(operativtUppgiftslagerRequest);

      operativtUppgiftslagerService.addOperativeTask(oulAddRequest, oulAddRequestMetadata);
      log.info("Processed task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
   }
}
