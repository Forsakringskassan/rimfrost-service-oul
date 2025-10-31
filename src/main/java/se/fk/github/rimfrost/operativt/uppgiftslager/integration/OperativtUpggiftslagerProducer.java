package se.fk.github.rimfrost.operativt.uppgiftslager.integration;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftsLagerUppdatering;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerResponse;

@ApplicationScoped
public class OperativtUpggiftslagerProducer 
{
    private static final Logger log = LoggerFactory.getLogger(OperativtUpggiftslagerProducer.class);

    @Channel("operativt-uppgiftslager-responses")
    Emitter<OperativtUppgiftslagerResponse> emitter;

    @Channel("operativt-uppgiftslager-status-uppdateringar")
    Emitter<OperativtUppgiftsLagerUppdatering> updateEmitter;

    public void publishTaskResponse(OperativtUppgiftslagerResponse response) {
        log.info("Publishing task response for operativt uppgiftslager: {}", response);
        // Logik för att skicka tillbaka resultatet från en uppgift från uppgiftslagret
        // Även för att uppdatera om statusen på en uppgift ändras, för loggning
        emitter.send(response);
        log.info("Published task response for operativt uppgiftslager: {}", response);
    }

    public void publishTaskUpdate(OperativtUppgiftsLagerUppdatering update) {
        log.info("Publishing task update for operativt uppgiftslager: {}", update);
        // Logik för att skicka en uppdatering för response
        updateEmitter.send(update);
        log.info("Published task update for operativt uppgiftslager: {}", update);
    }

}
