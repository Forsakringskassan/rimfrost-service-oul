package se.fk.github.rimfrost.operativt.uppgiftslager;

import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OulTestData
{
   public static final String oulHandlaggareTypId = "116759e4-18fd-4209-849c-90abbd257d22";

   public static CreateUppgiftRequest newCreateUppgiftRequest(UUID handlaggningId)
   {
      var individ = new Idtyp();
      individ.setTypId("d8bc00b6-445e-4085-ac31-d743cfb5f303");
      individ.setVarde("19900101-1234");

      var request = new CreateUppgiftRequest();
      request.setVersion("1.0");
      request.setHandlaggningId(handlaggningId);
      request.setIndivider(List.of(individ));
      request.setRegel("Test Regel");
      request.setRoll("Test Roll");
      request.setBeskrivning("Test Beskrivning");
      request.setVerksamhetslogik("Test Verksamhetslogik");
      request.setUrl("/test/url/");
      request.setSubTopic("test");
      request.setCloudeventAttributes(Map.of("kogitoprocinstanceid", "test-proc-instance-id"));

      return request;
   }

   public static EndUppgiftRequest newEndUppgiftRequest(String reason)
   {
      var endUppgiftRequest = new EndUppgiftRequest();
      endUppgiftRequest.setReason(reason);
      return endUppgiftRequest;
   }
}
