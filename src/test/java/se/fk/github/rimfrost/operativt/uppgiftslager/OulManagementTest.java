package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
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
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class OulManagementTest extends OulTestBase
{
   private static WireMockServer wireMockServer;

   @BeforeAll
   static void setup()
   {
      wireMockServer = WireMockTestResource.getWireMockServer();
   }

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

   @Test
   @DisplayName("FKPOC-1022: Skapa uppgift är idempotent — samma handlaggningId/regel/erbjudande "
         + "returnerar den befintliga uppgiften i stället för att skapa en dubblett")
   public void should_not_create_duplicate_uppgift_for_same_handlaggning_regel_erbjudande()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);

      var firstResponse = sendCreateUppgiftRequest(createUppgiftRequest);
      var secondResponse = sendCreateUppgiftRequest(createUppgiftRequest);

      assertEquals(firstResponse.getUppgiftId(), secondResponse.getUppgiftId());

      var page = getUppgifter(100);
      var matchingCount = page.getItems().stream()
            .filter(item -> handlaggningId.equals(item.getHandlaggningId()))
            .count();
      assertEquals(1, matchingCount);
   }

   @Test
   public void should_return_400_when_process_info_is_null_during_create_uppgift()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      createUppgiftRequest.setProcessInfo(null);
      sendCreateUppgiftRequest(createUppgiftRequest, 400);
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
      assertEquals(1, endResponse.getProcessInfo().getCloudeventAttributes().size());
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

   @Test
   @DisplayName("OUL-FR-07.2: Uppdatera uppgift med angiven handläggare — HTTP 404 returneras när uppgifts-ID inte finns")
   public void should_return_404_on_update_with_handlaggarId_set_when_uppgift_not_found()
   {
      Idtyp newHandlaggare = new Idtyp();
      newHandlaggare.setTypId(oulHandlaggareTypId);
      newHandlaggare.setVarde(UUID.randomUUID().toString());

      UpdateUppgiftRequest updateUppgiftRequest = new UpdateUppgiftRequest();
      updateUppgiftRequest.setHandlaggarId(newHandlaggare);

      updateTask(UUID.randomUUID(), updateUppgiftRequest, 404);
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
   @DisplayName("OUL-FR-07 (SID-spärr): PATCH /uppgifter/{id} returns 403 and leaves uppgift unchanged when moving a SID-märkt uppgift to a handläggare without SID-behörighet")
   public void should_return_403_and_leave_uppgift_unchanged_when_moving_sid_uppgift_to_unauthorized_handlaggare()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      sendCreateUppgiftRequest(createUppgiftRequest);

      var handlaggareId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggareId);
      var uppgiftId = assignResponse.getOperativUppgift().getUppgiftId();

      // Uppgiften blir sid-märkt efter den ursprungliga (obegränsade) tilldelningen.
      // Den ursprungliga handläggaren har SID-behörighet (till skillnad från målet nedan), så
      // att kontrollen nedan verkligen prövar PATCH-spärren och inte råkar sammanblandas med
      // FKPOC-940:s egna listnings-ombedömning (som annars skulle ta bort uppgiften från
      // handlaggareId:s lista av ett helt annat, redan täckt skäl).
      wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/sid/status"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                  .withBody("{\"sid\":true}")));
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + handlaggareId + "/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                  .withBody("true")));

      var targetHandlaggareId = UUID.randomUUID();
      // targetHandlaggareId har ingen hasSidPermission-stubb → saknar SID-behörighet

      Idtyp newHandlaggare = new Idtyp();
      newHandlaggare.setTypId(oulHandlaggareTypId);
      newHandlaggare.setVarde(targetHandlaggareId.toString());

      UpdateUppgiftRequest updateUppgiftRequest = new UpdateUppgiftRequest();
      updateUppgiftRequest.setHandlaggarId(newHandlaggare);

      updateTask(uppgiftId, updateUppgiftRequest, 403);

      var assignedTasks = getAssignedTasks(handlaggareId);
      var assignedTask = assignedTasks.getOperativaUppgifter().stream()
            .filter(u -> u.getUppgiftId().equals(uppgiftId)).findFirst();
      assertTrue(assignedTask.isPresent());
   }

   @Test
   @DisplayName("OUL-FR-07 (SID-spärr): PATCH /uppgifter/{id} succeeds when moving a SID-märkt uppgift to a handläggare with SID-behörighet")
   public void should_move_sid_uppgift_when_target_has_sid_behorighet()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      sendCreateUppgiftRequest(createUppgiftRequest);

      var handlaggareId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggareId);
      var uppgiftId = assignResponse.getOperativUppgift().getUppgiftId();

      wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/sid/status"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                  .withBody("{\"sid\":true}")));

      var targetHandlaggareId = UUID.randomUUID();
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + targetHandlaggareId + "/hasSidPermission"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                  .withBody("true")));

      Idtyp newHandlaggare = new Idtyp();
      newHandlaggare.setTypId(oulHandlaggareTypId);
      newHandlaggare.setVarde(targetHandlaggareId.toString());

      UpdateUppgiftRequest updateUppgiftRequest = new UpdateUppgiftRequest();
      updateUppgiftRequest.setHandlaggarId(newHandlaggare);

      var updateResponse = updateTask(uppgiftId, updateUppgiftRequest);

      assertNotNull(updateResponse);
      assertEquals(newHandlaggare, updateResponse.getHandlaggarId());
   }

   @Test
   @DisplayName("OUL-FR-07 (SID-spärr): avtilldelning av en SID-märkt uppgift spärras inte, även utan SID-behörighet någonstans")
   public void should_allow_unassign_of_sid_uppgift_regardless_of_sid_behorighet()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      sendCreateUppgiftRequest(createUppgiftRequest);

      var handlaggareId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggareId);
      var uppgiftId = assignResponse.getOperativUppgift().getUppgiftId();

      wireMockServer.stubFor(WireMock.post(WireMock.urlPathEqualTo("/sid/status"))
            .willReturn(WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/json")
                  .withBody("{\"sid\":true}")));

      var unassignResponse = unassignTask(uppgiftId);

      assertNotNull(unassignResponse);
      assertNull(unassignResponse.getHandlaggarId());
      assertEquals("NY", unassignResponse.getStatus());
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

   private se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande toErbjudande(
         se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.Erbjudande e)
   {
      var erbjudande = new se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.Erbjudande();
      erbjudande.setId(e.getId());
      erbjudande.setNamn(e.getNamn());
      return erbjudande;
   }
}
