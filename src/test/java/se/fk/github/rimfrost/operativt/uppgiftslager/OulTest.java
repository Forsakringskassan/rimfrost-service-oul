package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;
import se.fk.rimfrost.Status;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.*;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
public class OulTest
{
   private static final String oulStatusNotificationChannel = "operativt-uppgiftslager-status-notification";

   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   private List<? extends Message<?>> waitForMessages(String channel)
   {
      await().atMost(5, TimeUnit.SECONDS).until(() -> !inMemoryConnector.sink(channel).received().isEmpty());
      return inMemoryConnector.sink(channel).received();
   }

   private UppgiftResponse createUppgift(UUID handlaggningId)
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
      request.setReplyTopic("test");
      request.setCloudeventAttributes(Map.of("kogitoprocinstanceid", "test-proc-instance-id"));

      return given().contentType(ContentType.JSON).body(request)
            .when().post("/uppgifter")
            .then().statusCode(200)
            .extract().as(UppgiftResponse.class);
   }

   private PostUppgifterHandlaggareResponse assignTaskToHandlaggare(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON).when()
            .post("/uppgifter/handlaggare/116759e4-18fd-4209-849c-90abbd257d22/{handlaggarId}", handlaggarId).then()
            .statusCode(200).extract().as(PostUppgifterHandlaggareResponse.class);
   }

   private GetUppgifterHandlaggareResponse getAssignedTasks(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON).when()
            .get("/uppgifter/handlaggare/116759e4-18fd-4209-849c-90abbd257d22/{handlaggarId}", handlaggarId).then()
            .statusCode(200).extract().as(GetUppgifterHandlaggareResponse.class);
   }

   private UppgiftResponse endUppgift(UUID uppgiftId, String reason)
   {
      var request = new EndUppgiftRequest();
      request.setReason(reason);

      return given().contentType(ContentType.JSON).body(request)
            .when().post("/uppgifter/{uppgiftId}/end", uppgiftId)
            .then().statusCode(200)
            .extract().as(UppgiftResponse.class);
   }

   @BeforeEach
   public void setup()
   {
      inMemoryConnector.sink(oulStatusNotificationChannel).clear();
   }

   @Test
   public void testHealthEndpoint()
   {
      when()
            .get("/q/health/live")
            .then()
            .statusCode(200)
            .body("status", is("UP"));
   }

   @Test
   public void testOulSmoke()
   {
      var handlaggningId = UUID.randomUUID();

      //
      // Create uppgift via REST
      //
      var createResponse = createUppgift(handlaggningId);
      assertNotNull(createResponse.getUppgiftId());
      assertEquals(handlaggningId, createResponse.getHandlaggningId());
      var uppgiftId = createResponse.getUppgiftId().toString();

      //
      // Assign new task to handlaggare
      //
      var handlaggarId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggarId);

      var expectedIndivid = new Idtyp();
      expectedIndivid.setTypId("d8bc00b6-445e-4085-ac31-d743cfb5f303");
      expectedIndivid.setVarde("19900101-1234");

      var expectedAssignedHandlaggare = new Idtyp();
      expectedAssignedHandlaggare.setTypId("116759e4-18fd-4209-849c-90abbd257d22");
      expectedAssignedHandlaggare.setVarde(handlaggarId.toString());

      assertNotNull(assignResponse.getOperativUppgift());
      assertEquals(createResponse.getUppgiftId(), assignResponse.getOperativUppgift().getUppgiftId());
      assertEquals("Test Regel", assignResponse.getOperativUppgift().getRegel());
      assertEquals("Test Roll", assignResponse.getOperativUppgift().getRoll());
      assertEquals("Test Beskrivning", assignResponse.getOperativUppgift().getBeskrivning());
      assertEquals("Test Verksamhetslogik", assignResponse.getOperativUppgift().getVerksamhetslogik());
      assertEquals("/test/url/", assignResponse.getOperativUppgift().getUrl());
      assertEquals(expectedAssignedHandlaggare, assignResponse.getOperativUppgift().getHandlaggarId());
      assertEquals(List.of(expectedIndivid), assignResponse.getOperativUppgift().getIndivider());
      assertEquals(OperativUppgift.StatusEnum.TILLDELAD, assignResponse.getOperativUppgift().getStatus());
      assertNotNull(assignResponse.getOperativUppgift().getSkapad());

      //
      // Verify OUL status notification is produced
      //
      var messages = waitForMessages(oulStatusNotificationChannel);
      assertEquals(1, messages.size());

      var message = messages.getFirst().getPayload();
      assertInstanceOf(OperativtUppgiftslagerStatusMessage.class, message);

      var expectedUtforare = new se.fk.rimfrost.Idtyp();
      expectedUtforare.setTypId("116759e4-18fd-4209-849c-90abbd257d22");
      expectedUtforare.setVarde(handlaggarId.toString());

      var oulStatusMessage = (OperativtUppgiftslagerStatusMessage) message;
      assertEquals(handlaggningId.toString(), oulStatusMessage.getHandlaggningId());
      assertEquals(uppgiftId, oulStatusMessage.getUppgiftId());
      assertEquals(expectedUtforare, oulStatusMessage.getUtforarId());
      assertEquals(Status.TILLDELAD, oulStatusMessage.getStatus());

      inMemoryConnector.sink(oulStatusNotificationChannel).clear();

      //
      // Verify assigned task is included in GET response
      //
      var assignedTasks = getAssignedTasks(handlaggarId);

      assertNotNull(assignedTasks);
      assertNotNull(assignedTasks.getOperativaUppgifter());
      assertEquals(1, assignedTasks.getOperativaUppgifter().size());

      var assignedTask = assignedTasks.getOperativaUppgifter().getFirst();
      assertEquals(createResponse.getUppgiftId(), assignedTask.getUppgiftId());

      //
      // End uppgift via REST
      //
      endUppgift(UUID.fromString(uppgiftId), "Test reason");

      //
      // Verify OUL status notification is produced
      //
      messages = waitForMessages(oulStatusNotificationChannel);
      assertEquals(1, messages.size());

      message = messages.getFirst().getPayload();
      assertInstanceOf(OperativtUppgiftslagerStatusMessage.class, message);

      oulStatusMessage = (OperativtUppgiftslagerStatusMessage) message;
      assertEquals(handlaggningId.toString(), oulStatusMessage.getHandlaggningId());
      assertEquals(uppgiftId, oulStatusMessage.getUppgiftId());
      assertEquals(expectedUtforare, oulStatusMessage.getUtforarId());
      assertEquals(Status.AVSLUTAD, oulStatusMessage.getStatus());

      inMemoryConnector.sink(oulStatusNotificationChannel).clear();

      //
      // Verify assigned task is not included in GET response
      //
      assignedTasks = getAssignedTasks(handlaggarId);

      assertNotNull(assignedTasks);
      assertNull(assignedTasks.getOperativaUppgifter());
   }

   @ParameterizedTest
   @CsvSource({"false, true", "true, false"})
   public void testUppgiftPresenceAfterStatusUpdate(boolean endTask, boolean expectedPresent)
   {
      var createResponse = createUppgift(UUID.randomUUID());
      var uppgiftId = createResponse.getUppgiftId();

      var handlaggarId = UUID.randomUUID();
      assignTaskToHandlaggare(handlaggarId);
      waitForMessages(oulStatusNotificationChannel);
      inMemoryConnector.sink(oulStatusNotificationChannel).clear();

      if (endTask)
      {
         endUppgift(uppgiftId, "Test reason");
         waitForMessages(oulStatusNotificationChannel);
         inMemoryConnector.sink(oulStatusNotificationChannel).clear();
      }

      var assignedTasks = getAssignedTasks(handlaggarId);
      assertNotNull(assignedTasks);
      if (expectedPresent)
      {
         assertNotNull(assignedTasks.getOperativaUppgifter());
         assertEquals(1, assignedTasks.getOperativaUppgifter().size());
      }
      else
      {
         assertNull(assignedTasks.getOperativaUppgifter());
      }
   }

}
