package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util.PresentationKafkaMapper;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessage;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;

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
   public CompletionStage<Void> onOperativtUppgiftsLagerRequest(Message<OperativtUppgiftslagerRequestMessage> operativtUppgiftslagerRequest)
   {
      log.info("Received task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
      IncomingKafkaRecordMetadata<String, String> metadata = operativtUppgiftslagerRequest
            .getMetadata(IncomingKafkaRecordMetadata.class)
            .orElseThrow(() -> new IllegalStateException("Missing replyTo header"));

      String replyTo = metadata.getHeaders().lastHeader("replyTo") != null
            ? new String(metadata.getHeaders().lastHeader("replyTo").value(), StandardCharsets.UTF_8)
            : null;

      if (replyTo == null)
      {
         throw new IllegalStateException("Missing replyTo header");
      }

      var oulAddRequest = mapper.toAddRequest(operativtUppgiftslagerRequest.getPayload(), replyTo);

      operativtUppgiftslagerService.addOperativeTask(oulAddRequest);
      log.info("Processed task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
      return operativtUppgiftslagerRequest.ack();
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
