package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newEndUppgiftRequest;

@QuarkusTest
public class OulManagementTest extends OulTestBase
{
   @Test
   @DisplayName("FR-01.1, FR-01.2, FR-01.3, FR-01.6, FR-01.7: Skapa uppgift — status NY, uppgift_id genereras, CloudEvent-attribut bevaras")
   public void should_create_uppgift()
   {
      var handlaggningId = UUID.randomUUID();
      var createUppgiftRequest = newCreateUppgiftRequest(handlaggningId);
      var createResponse = sendCreateUppgiftRequest(createUppgiftRequest);

      assertNotNull(createResponse);
      assertEquals(handlaggningId, createResponse.getHandlaggningId());
      assertNotNull(createResponse.getUppgiftId());
      assertEquals("NY", createResponse.getStatus());
      assertEquals(createUppgiftRequest.getCloudeventAttributes(), createResponse.getCloudeventAttributes());
   }

   @ParameterizedTest
   @CsvSource(
   {
         "AVSLUTAD",
         "AVBRUTEN"
   })
   @DisplayName("FR-02.1, FR-02.3, FR-01.6: Avsluta uppgift — AVSLUTAD och AVBRUTEN är giltiga skäl, status och CloudEvent-attribut returneras")
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
      assertEquals(createUppgiftRequest.getCloudeventAttributes(), endResponse.getCloudeventAttributes());
   }

   @Test
   @DisplayName("FR-02.4: Avsluta uppgift — HTTP 404 returneras när uppgifts-ID inte finns")
   public void should_return_404_on_end_when_uppgift_not_found()
   {
      var uppgiftId = UUID.randomUUID();
      sendEndUppgiftRequest(uppgiftId, newEndUppgiftRequest("AVSLUTAD"), 404);
   }

   @ParameterizedTest
   @CsvSource(
   {
         "true", "false"
   })
   @DisplayName("FR-03.1, FR-03.3, FR-01.4, FR-01.5: Lista alla uppgifter — NY och TILLDELAD returneras med fullständiga fält, handlaggarId endast för tilldelade")
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

      var uppgifter = getUppgifter();
      sendEndUppgiftRequest(createResponse.getUppgiftId(), newEndUppgiftRequest("reason"));

      assertNotNull(uppgifter);
      assertEquals(1, uppgifter.size());

      var uppgift = uppgifter.getFirst();

      assertEquals(createResponse.getUppgiftId(), uppgift.getUppgiftId());
      assertEquals(handlaggningId, uppgift.getHandlaggningId());
      assertNotNull(uppgift.getSkapad());
      assertEquals(assignedTask ? "TILLDELAD" : "NY", uppgift.getStatus());
      assertEquals(createUppgiftRequest.getIndivider(), uppgift.getIndivider());
      assertEquals(createUppgiftRequest.getRegel(), uppgift.getRegel());
      assertEquals(createUppgiftRequest.getBeskrivning(), uppgift.getBeskrivning());
      assertEquals(createUppgiftRequest.getVerksamhetslogik(), uppgift.getVerksamhetslogik());
      assertEquals(createUppgiftRequest.getRoll(), uppgift.getRoll());
      assertEquals(createUppgiftRequest.getUrl(), uppgift.getUrl());
      assertNull(uppgift.getUtford());
      assertNull(uppgift.getPlaneradTill());
      assertEquals(createUppgiftRequest.getErbjudande(), uppgift.getErbjudande());

      if (assignedTask)
      {
         assertEquals(handlaggareId.toString(), uppgift.getHandlaggarId().getVarde());
      }
      else
      {
         assertNull(uppgift.getHandlaggarId());
      }
   }
}
