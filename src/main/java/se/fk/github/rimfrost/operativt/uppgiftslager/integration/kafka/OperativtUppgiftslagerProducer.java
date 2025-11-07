package se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.OperativtUppgiftslagerResponseMessagePayload;

@ApplicationScoped
public class OperativtUppgiftslagerProducer
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerProducer.class);

   @Channel("operativt-uppgiftslager-responses")
   Emitter<OperativtUppgiftslagerResponseMessagePayload> emitter;

   public void publishTaskResponse(OperativtUppgiftslagerResponseMessagePayload response)
   {
      log.info("Publishing task response for operativt uppgiftslager: {}", response);
      emitter.send(response);
      log.info("Published task response for operativt uppgiftslager: {}", response);
   }
}
