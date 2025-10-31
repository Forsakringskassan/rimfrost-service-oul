package se.fk.github.rimfrost.operativt.uppgiftslager.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftsLagerUppdatering;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerResponse;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
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
   public void handleIncomingTask(OperativtUppgiftslagerRequest operativtUppgiftslagerRequest) {
      log.info("Received task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
      // Logik för att lägga till en uppgift till uppgiftslagret
      // Skicka även ett slags meddelande när den är ny
      operativtUppgiftslagerService.addOperativeTask();
      log.info("Processed task for operativt uppgiftslager: {}", operativtUppgiftslagerRequest);
   }
}
