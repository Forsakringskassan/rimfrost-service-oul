package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UpdateUppgiftRequest;

import java.util.UUID;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newEndUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

@QuarkusTest
public class OulManagementTest extends OulTestBase
{
   @Test
   @DisplayName("OUL-FR-01.1, OUL-FR-01.2, OUL-FR-01.3, OUL-FR-01.6, OUL-FR-01.7: Skapa uppgift — status NY, uppgift_id genereras, CloudEvent-attribut bevaras")
   public void should_create_uppgift()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      var createResponse = sendCreateUppgiftRequest(createUppgiftRequest);

      assertNotNull(createResponse);
      assertEquals(handlaggningId, createResponse.getHandlaggningId());
      assertNotNull(createResponse.getUppgiftId());
      assertEquals("NY", createResponse.getStatus());
      assertEquals(createUppgiftRequest.getProcessInfo().getCloudeventAttributes(),
            createResponse.getProcessInfo().getCloudeventAttributes());
      assertEquals(createUppgiftRequest.getProcessInfo().getReplyTopic(), createResponse.getProcessInfo().getReplyTopic());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "AVSLUTAD",
         "AVBRUTEN"
   })
   @DisplayName("OUL-FR-02.1, OUL-FR-02.3, OUL-FR-01.6: Avsluta uppgift — AVSLUTAD och AVBRUTEN är giltiga skäl, status och CloudEvent-attribut returneras")
   public void should_end_uppgift(String reason)
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      var createResponse = sendCreateUppgiftRequest(createUppgiftRequest);
      var endResponse = sendEndUppgiftRequest(createResponse.getUppgiftId(), newEndUppgiftRequest(reason));

      assertNotNull(endResponse);
      assertEquals(createResponse.getUppgiftId(), endResponse.getUppgiftId());
      assertEquals(handlaggningId, endResponse.getHandlaggningId());
      assertEquals("AVSLUTAD", endResponse.getStatus());
      assertEquals(createUppgiftRequest.getProcessInfo().getCloudeventAttributes(),
            endResponse.getProcessInfo().getCloudeventAttributes());
      assertEquals(createUppgiftRequest.getProcessInfo().getReplyTopic(), endResponse.getProcessInfo().getReplyTopic());
   }

   @Test
   @DisplayName("GET /uppgifter returns 404 when the requested sorteringsordningId does not exist")
   public void should_return_404_on_get_uppgifter_when_sorteringsordning_not_found()
   {
      getUppgifter(50, UUID.randomUUID(), 404);
   }

   @Test
   @DisplayName("OUL-FR-02.4: Avsluta uppgift — HTTP 404 returneras när uppgifts-ID inte finns")
   public void should_return_404_on_end_when_uppgift_not_found()
   {
      var uppgiftId = UUID.randomUUID();
      sendEndUppgiftRequest(uppgiftId, newEndUppgiftRequest("AVSLUTAD"), 404);
   }

   @Test
   @DisplayName("OUL-FR-07.2: Uppdatera uppgift — HTTP 404 returneras när uppgifts-ID inte finns")
   public void should_return_404_on_update_when_uppgift_not_found()
   {
      updateTask(UUID.randomUUID(), new UpdateUppgiftRequest(), 404);
   }

   @ParameterizedTest
   @CsvSource(
   {
         "true", "false"
   })
   @DisplayName("OUL-FR-03.1, OUL-FR-03.3, OUL-FR-01.4, OUL-FR-01.5, OUL-FR-03.4, OUL-FR-03.5: Lista alla uppgifter — NY och TILLDELAD returneras med fullständiga fält (Tier 2)")
   public void should_list_available_uppgifter(boolean assignedTask)
   {
      var handlaggningId = UUID.randomUUID();
      UUID handlaggareId = null;

      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      var createResponse = sendCreateUppgiftRequest(createUppgiftRequest);

      if (assignedTask)
      {
         handlaggareId = UUID.randomUUID();
         assignTaskToHandlaggare(handlaggareId);
      }

      var page = getUppgifter(50);
      sendEndUppgiftRequest(createResponse.getUppgiftId(), newEndUppgiftRequest("reason"));

      assertNotNull(page);
      assertEquals(1, page.getTotal());
      assertEquals(1, page.getItems().size());

      var uppgift = page.getItems().getFirst();

      assertEquals(createResponse.getUppgiftId(), uppgift.getUppgiftId());
      assertEquals(handlaggningId, uppgift.getHandlaggningId());
      assertNotNull(uppgift.getSkapad());
      assertEquals(assignedTask ? "TILLDELAD" : "NY", uppgift.getStatus());
      assertEquals(createUppgiftRequest.getIndivider().stream().map(this::toIdtyp).toList(), uppgift.getIndivider());
      assertEquals(createUppgiftRequest.getRegel(), uppgift.getRegel());
      assertEquals(createUppgiftRequest.getBeskrivning(), uppgift.getBeskrivning());
      assertEquals(createUppgiftRequest.getVerksamhetslogik(), uppgift.getVerksamhetslogik());
      assertEquals(createUppgiftRequest.getRoll(), uppgift.getRoll());
      assertEquals(createUppgiftRequest.getUrl(), uppgift.getUrl());
      assertNull(uppgift.getUtford());
      assertNull(uppgift.getPlaneradTill());
      assertEquals(toErbjudande(createUppgiftRequest.getErbjudande()), uppgift.getErbjudande());

      if (assignedTask)
      {
         assertEquals(handlaggareId.toString(), uppgift.getHandlaggarId().getVarde());
      }
      else
      {
         assertNull(uppgift.getHandlaggarId());
      }
   }

   @Test
   @DisplayName("OUL-FR-08.1, OUL-FR-08.2: Avdela uppgift — tilldelning tas bort och uppgiften återgår till status NY")
   public void should_unassign_task_from_handlaggare()
   {
      var handlaggningId = UUID.randomUUID();

      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      sendCreateUppgiftRequest(createUppgiftRequest);

      var handlaggareId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggareId);

      assertNotNull(assignResponse);
      assertNotNull(assignResponse.getOperativUppgift());

      var unassignResponse = unassignTask(assignResponse.getOperativUppgift().getUppgiftId());
      assertNotNull(unassignResponse);
      assertNull(unassignResponse.getHandlaggarId());
      assertEquals("NY", unassignResponse.getStatus());

      var assignedTasks = getAssignedTasks(handlaggareId);
      var assignedTask = assignedTasks.getOperativaUppgifter().stream()
            .filter(u -> u.getUppgiftId().equals(assignResponse.getOperativUppgift().getUppgiftId())).findFirst();
      assertTrue(assignedTask.isEmpty());
   }

   @Test
   @DisplayName("OUL-FR-08.3: Avdela uppgift — HTTP 404 returneras när uppgifts-ID inte finns")
   public void should_return_404_on_unassign_when_uppgift_not_found()
   {
      unassignTask(UUID.randomUUID(), 404);
   }

   @Test
   public void should_update_task_assignment()
   {
      var handlaggningId = UUID.randomUUID();

      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      sendCreateUppgiftRequest(createUppgiftRequest);

      var handlaggareId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggareId);

      assertNotNull(assignResponse);
      assertNotNull(assignResponse.getOperativUppgift());

      Idtyp newHandlaggare = new Idtyp();
      newHandlaggare.setTypId(oulHandlaggareTypId);
      newHandlaggare.setVarde(UUID.randomUUID().toString());

      UpdateUppgiftRequest updateUppgiftRequest = new UpdateUppgiftRequest();
      updateUppgiftRequest.setHandlaggarId(newHandlaggare);

      var updateResponse = updateTask(assignResponse.getOperativUppgift().getUppgiftId(), updateUppgiftRequest);

      assertNotNull(updateResponse);
      assertEquals(newHandlaggare, updateResponse.getHandlaggarId());

      var assignedTasks = getAssignedTasks(UUID.fromString(newHandlaggare.getVarde()));
      var assignedTask = assignedTasks.getOperativaUppgifter().stream()
            .filter(u -> u.getUppgiftId().equals(assignResponse.getOperativUppgift().getUppgiftId())).findFirst();
      assertTrue(assignedTask.isPresent());

      assignedTasks = getAssignedTasks(handlaggareId);
      assignedTask = assignedTasks.getOperativaUppgifter().stream()
            .filter(u -> u.getUppgiftId().equals(assignResponse.getOperativUppgift().getUppgiftId())).findFirst();
      assertTrue(assignedTask.isEmpty());
   }

   @Test
   public void should_return_unchanged_task_on_update_request_with_no_parameters_set()
   {
      var handlaggningId = UUID.randomUUID();

      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      sendCreateUppgiftRequest(createUppgiftRequest);

      var handlaggareId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggareId);

      assertNotNull(assignResponse);
      assertNotNull(assignResponse.getOperativUppgift());

      var assignedUppgift = getUppgifter(2).getItems().stream()
            .filter(u -> u.getUppgiftId().equals(assignResponse.getOperativUppgift().getUppgiftId())).findFirst().orElseThrow();

      UpdateUppgiftRequest updateUppgiftRequest = new UpdateUppgiftRequest();
      var updateResponse = updateTask(assignResponse.getOperativUppgift().getUppgiftId(), updateUppgiftRequest);

      assertNotNull(updateResponse);
      assertEquals(assignedUppgift, updateResponse);
   }

   private se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Idtyp toIdtyp(
         se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Idtyp id)
   {
      if (id == null)
      {
         return null;
      }

      var idtyp = new Idtyp();
      idtyp.setTypId(id.getTypId());
      idtyp.setVarde(id.getVarde());
      return idtyp;
   }

   private se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande toErbjudande(
         se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Erbjudande e)
   {
      var erbjudande = new se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande();
      erbjudande.setId(e.getId());
      erbjudande.setNamn(e.getNamn());
      return erbjudande;
   }
}
