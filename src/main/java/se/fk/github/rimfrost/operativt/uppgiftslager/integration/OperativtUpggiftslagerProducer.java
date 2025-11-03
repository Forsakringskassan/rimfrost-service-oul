package se.fk.github.rimfrost.operativt.uppgiftslager.integration;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerNotification;

@ApplicationScoped
public class OperativtUpggiftslagerProducer
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUpggiftslagerProducer.class);

   @Channel("operativt-uppgiftslager-responses")
   Emitter<OperativtUppgiftslagerNotification> emitter;

   public void publishTaskResponse(OperativtUppgiftslagerNotification response)
   {
      log.info("Publishing task response for operativt uppgiftslager: {}", response);
      emitter.send(response);
      log.info("Published task response for operativt uppgiftslager: {}", response);
   }

}
