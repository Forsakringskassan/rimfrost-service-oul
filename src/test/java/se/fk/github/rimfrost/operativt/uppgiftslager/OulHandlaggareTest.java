package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.OperativUppgift;

import java.util.UUID;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newEndUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

@QuarkusTest
public class OulHandlaggareTest extends OulTestBase
{
   @Test
   @DisplayName("FR-04.1, FR-04.2, FR-04.3, FR-04.5, FR-06.1, FR-06.2, FR-06.3: Hämta ny uppgift — tilldelas handläggare med status TILLDELAD och Kafka-notis publiceras med korrekt innehåll")
   public void should_assign_task_to_handlaggare()
   {
      var handlaggareId = UUID.randomUUID();

      var createUppgiftRequest = newCreateUppgiftRequest(UUID.randomUUID());
      var createResponse = sendCreateUppgiftRequest(createUppgiftRequest);
      var assignResponse = assignTaskToHandlaggare(handlaggareId);

      assertNotNull(assignResponse);
      assertNotNull(assignResponse.getOperativUppgift());
      assertEquals(createResponse.getUppgiftId(), assignResponse.getOperativUppgift().getUppgiftId());
      assertEquals(createResponse.getHandlaggningId(), assignResponse.getOperativUppgift().getHandlaggningId());
      assertNotNull(assignResponse.getOperativUppgift().getSkapad());
      assertEquals(createHandlaggningIdtyp(handlaggareId), assignResponse.getOperativUppgift().getHandlaggarId());
      assertEquals(OperativUppgift.StatusEnum.TILLDELAD, assignResponse.getOperativUppgift().getStatus());
      assertEquals(createUppgiftRequest.getIndivider().stream().map(this::toHandlaggningIdtyp).toList(),
            assignResponse.getOperativUppgift().getIndivider());
      assertEquals(createUppgiftRequest.getRegel(), assignResponse.getOperativUppgift().getRegel());
      assertEquals(createUppgiftRequest.getBeskrivning(), assignResponse.getOperativUppgift().getBeskrivning());
      assertEquals(createUppgiftRequest.getVerksamhetslogik(), assignResponse.getOperativUppgift().getVerksamhetslogik());
      assertEquals(createUppgiftRequest.getRoll(), assignResponse.getOperativUppgift().getRoll());
      assertEquals(createUppgiftRequest.getUrl(), assignResponse.getOperativUppgift().getUrl());

      var statusMessage = oulKafkaConnector.waitForOulStatusMessage();

      assertNotNull(statusMessage);
      assertEquals(createResponse.getUppgiftId().toString(), statusMessage.getUppgiftId());
      assertEquals(createUppgiftRequest.getHandlaggningId().toString(), statusMessage.getHandlaggningId());
      assertEquals("TILLDELAD", statusMessage.getStatus());
      assertEquals(createKafkaIdTyp(handlaggareId), statusMessage.getUtforarId());
      assertEquals(createUppgiftRequest.getCloudeventAttributes(), statusMessage.getCloudeventAttributes());
   }

   @Test
   @DisplayName("FR-05.1, FR-05.2: Lista tilldelade uppgifter — filtreras på handläggarens identitet, endast egna uppgifter returneras")
   public void should_return_task_assigned_to_handlaggare()
   {
      var handlaggareId = UUID.randomUUID();

      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var assignResponse = assignTaskToHandlaggare(handlaggareId);
      var assignedTasks = getAssignedTasks(handlaggareId);

      assertNotNull(assignedTasks);
      assertNotNull(assignedTasks.getOperativaUppgifter());
      assertEquals(1, assignedTasks.getOperativaUppgifter().size());

      var assignedTask = assignedTasks.getOperativaUppgifter().getFirst();

      assertEquals(assignResponse.getOperativUppgift().getUppgiftId(), assignedTask.getUppgiftId());
      assertEquals(assignResponse.getOperativUppgift().getHandlaggningId(), assignedTask.getHandlaggningId());
      assertEquals(assignResponse.getOperativUppgift().getSkapad(), assignedTask.getSkapad());
      assertEquals(assignResponse.getOperativUppgift().getHandlaggarId(), assignedTask.getHandlaggarId());
      assertEquals(handlaggareId.toString(), assignedTask.getHandlaggarId().getVarde());
      assertEquals(OperativUppgift.StatusEnum.TILLDELAD, assignResponse.getOperativUppgift().getStatus());
      assertEquals(assignResponse.getOperativUppgift().getIndivider(), assignedTask.getIndivider());
      assertEquals(assignResponse.getOperativUppgift().getRegel(), assignedTask.getRegel());
      assertEquals(assignResponse.getOperativUppgift().getBeskrivning(), assignedTask.getBeskrivning());
      assertEquals(assignResponse.getOperativUppgift().getVerksamhetslogik(), assignedTask.getVerksamhetslogik());
      assertEquals(assignResponse.getOperativUppgift().getRoll(), assignedTask.getRoll());
      assertEquals(assignResponse.getOperativUppgift().getUrl(), assignedTask.getUrl());
   }

   @Test
   @DisplayName("FR-02.2, FR-05.1: Avsluta uppgift — uppgiften tas bort ur aktivt lager och syns inte längre i handläggarens lista")
   public void should_return_empty_task_list_after_assigned_task_end()
   {
      var handlaggareId = UUID.randomUUID();

      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var assignResponse = assignTaskToHandlaggare(handlaggareId);
      sendEndUppgiftRequest(assignResponse.getOperativUppgift().getUppgiftId(), newEndUppgiftRequest("AVSLUTAD"));

      var assignedTasks = getAssignedTasks(handlaggareId);

      assertNotNull(assignedTasks);
      assertNotNull(assignedTasks.getOperativaUppgifter());
      assertEquals(0, assignedTasks.getOperativaUppgifter().size());
   }

   private se.fk.rimfrost.Idtyp createKafkaIdTyp(UUID handlaggareId)
   {
      se.fk.rimfrost.Idtyp idtyp = new se.fk.rimfrost.Idtyp();
      idtyp.setTypId(oulHandlaggareTypId);
      idtyp.setVarde(handlaggareId.toString());

      return idtyp;
   }

   private se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.Idtyp createHandlaggningIdtyp(
         UUID handlaggareId)
   {
      se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.Idtyp idtyp = new se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.Idtyp();
      idtyp.setTypId(oulHandlaggareTypId);
      idtyp.setVarde(handlaggareId.toString());

      return idtyp;
   }

   private se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.Idtyp toHandlaggningIdtyp(
         se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Idtyp managementIdtyp)
   {
      se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.Idtyp idtyp = new se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.Idtyp();
      idtyp.setTypId(managementIdtyp.getTypId());
      idtyp.setVarde(managementIdtyp.getVarde());
      return idtyp;
   }
}
