package se.fk.github.rimfrost.operativt.uppgiftslager;

import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Erbjudande;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Idtyp;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;

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

      var erbjudande = new Erbjudande();
      erbjudande.setId("55f84389-8e66-4122-8f07-c157141d5b1d");
      erbjudande.setNamn("Erbjudande namn");

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
      request.setErbjudande(erbjudande);

      return request;
   }

   public static EndUppgiftRequest newEndUppgiftRequest(String reason)
   {
      var endUppgiftRequest = new EndUppgiftRequest();
      endUppgiftRequest.setReason(reason);
      return endUppgiftRequest;
   }

   public static SorteringsordningSpec newSorteringsordningSpec()
   {
      var entry = new SorteringsordningEntry();
      var spec = new SorteringsordningSpec();
      spec.setEntries(List.of(entry));
      return spec;
   }
}
