package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util.PresentationKafkaMapper;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessage;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

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
   public void onOperativtUppgiftsLagerRequest(OperativtUppgiftslagerRequestMessage operativtUppgiftslagerRequest)
   {
      log.info("Received task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);

      var oulAddRequest = mapper.toAddRequest(operativtUppgiftslagerRequest);

      operativtUppgiftslagerService.addOperativeTask(oulAddRequest);
      log.info("Processed task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
   }

   @Incoming("operativt-uppgiftslager-status-control")
   @Blocking
   public void onOperativeUppgiftsLagerStatusUpdate(OperativtUppgiftslagerStatusMessage operativtUppgiftslagerStatusMessage)
   {
      log.info("Received task for StatusUpdate: {}", operativtUppgiftslagerStatusMessage);

      var statusUpdateRequest = mapper.toStatusUpdateRequest(operativtUppgiftslagerStatusMessage);
      operativtUppgiftslagerService.onTaskStatusUpdated(statusUpdateRequest);

      log.info("Processed task for StatusUpdate: {}", statusUpdateRequest);
   }
}
