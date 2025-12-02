package se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka;

import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.OperativtUppgiftslagerResponseMessage;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

@ApplicationScoped
public class OperativtUppgiftslagerProducer
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerProducer.class);

   @Channel("operativt-uppgiftslager-responses")
   Emitter<OperativtUppgiftslagerResponseMessage> emitter;

   public void publishTaskResponse(UUID kundbehovsflodeId, UUID uppgiftId)
   {
      var response = new OperativtUppgiftslagerResponseMessage();
      response.setKundbehovsflodeId(kundbehovsflodeId.toString());
      response.setUppgiftId(uppgiftId.toString());
      log.info("Publishing task response for operativt uppgiftslager: {}", response);
      emitter.send(response);
      log.info("Published task response for operativt uppgiftslager: {}", response);
   }

   @Channel("operativt-uppgiftslager-status-notification")
   Emitter<OperativtUppgiftslagerStatusMessage> statusUpdateEmitter;

   public void publishTaskStatusUpdate(OperativtUppgiftslagerStatusMessage statusMessage)
   {
      log.info("Publishing task StatusUpdate: {}", statusMessage);
      statusUpdateEmitter.send(statusMessage);
      log.info("Published task StatusUpdate: {}", statusMessage);
   }
}
