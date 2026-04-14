package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.Idtyp;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessage;
import se.fk.rimfrost.OperativtUppgiftslagerResponseMessage;
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
   private static final String oulRequestsChannel = "operativt-uppgiftslager-requests";
   private static final String oulResponsesChannel = "operativt-uppgiftslager-responses";
   private static final String oulStatusNotificationChannel = "operativt-uppgiftslager-status-notification";
   private static final String oulStatusControlChannel = "operativt-uppgiftslager-status-control";
   private static final String regelSubTopic = "test";

   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   private List<? extends Message<?>> waitForMessages(String channel)
   {
      await().atMost(5, TimeUnit.SECONDS).until(() -> !inMemoryConnector.sink(channel).received().isEmpty());
      return inMemoryConnector.sink(channel).received();
   }

   private IncomingKafkaRecordMetadata<String, String> createIncomingKafkaRecordMetadata(String channel, RecordHeaders headers)
   {
      // Workaround for IncomingKafkaRecordMetadata not being created on receiving side when in-memory-connector is used
      ConsumerRecord<String, String> record = new ConsumerRecord<>("", 0, 0L, 0L, TimestampType.NO_TIMESTAMP_TYPE, 0, 0, "", "",
            headers, null);
      return new IncomingKafkaRecordMetadata<>(record, channel);
   }

   private void sendOulRequest(String handlaggningId)
   {
      Idtyp individ = new Idtyp();
      individ.setTypId("d8bc00b6-445e-4085-ac31-d743cfb5f303");
      individ.setVarde("19900101-1234");

      OperativtUppgiftslagerRequestMessage message = new OperativtUppgiftslagerRequestMessage();
      message.setVersion("1.0");
      message.setHandlaggningId(handlaggningId);
      message.setIndivider(List.of(individ).toArray(Idtyp[]::new));
      message.setRegel("Test Regel");
      message.setRoll("Test Roll");
      message.setBeskrivning("Test Beskrivning");
      message.setVerksamhetslogik("Test Verksamhetslogik");
      message.setUrl("/test/url/");

      RecordHeaders headers = new RecordHeaders();
      headers.add(new RecordHeader("replyTo", regelSubTopic.getBytes(StandardCharsets.UTF_8)));
      Message<OperativtUppgiftslagerRequestMessage> payload = Message.of(message)
            .addMetadata(createIncomingKafkaRecordMetadata(oulRequestsChannel, headers));

      inMemoryConnector.source(oulRequestsChannel).send(payload);
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

   private void sendStatusUpdateRequest(String uppgiftId, Status status)
   {
      OperativtUppgiftslagerStatusMessage message = new OperativtUppgiftslagerStatusMessage();
      message.setUppgiftId(uppgiftId);
      message.setStatus(status);

      inMemoryConnector.source(oulStatusControlChannel).send(message);
   }

   @BeforeEach
   public void setup()
   {
      inMemoryConnector.sink(oulResponsesChannel).clear();
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
      String handlaggningId = UUID.randomUUID().toString();

      // Send OUL request to start workflow
      sendOulRequest(handlaggningId);

      //
      // Verify OUL response message produced
      //
      var messages = waitForMessages(oulResponsesChannel);
      assertEquals(1, messages.size());

      var message = messages.getFirst().getPayload();
      assertInstanceOf(OperativtUppgiftslagerResponseMessage.class, message);

      var oulResponseMessage = (OperativtUppgiftslagerResponseMessage) message;
      var uppgiftId = oulResponseMessage.getUppgiftId();
      assertEquals(handlaggningId, oulResponseMessage.getHandlaggningId());
      assertNotNull(uppgiftId);

      inMemoryConnector.sink(oulResponsesChannel).clear();

      //
      // Assign new task to handlaggare
      //
      var handlaggarId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggarId);

      var expectedIndivid = new se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Idtyp();
      expectedIndivid.setTypId("d8bc00b6-445e-4085-ac31-d743cfb5f303");
      expectedIndivid.setVarde("19900101-1234");

      var expectedAssignedHandlaggare = new se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Idtyp();
      expectedAssignedHandlaggare.setTypId("116759e4-18fd-4209-849c-90abbd257d22");
      expectedAssignedHandlaggare.setVarde(handlaggarId.toString());

      assertNotNull(assignResponse.getOperativUppgift());
      assertEquals(UUID.fromString(uppgiftId), assignResponse.getOperativUppgift().getUppgiftId());
      assertEquals("Test Regel", assignResponse.getOperativUppgift().getRegel());
      assertEquals("Test Roll", assignResponse.getOperativUppgift().getRoll());
      assertEquals("Test Beskrivning", assignResponse.getOperativUppgift().getBeskrivning());
      assertEquals("Test Verksamhetslogik", assignResponse.getOperativUppgift().getVerksamhetslogik());
      assertEquals("/test/url/", assignResponse.getOperativUppgift().getUrl());
      assertEquals(expectedAssignedHandlaggare, assignResponse.getOperativUppgift().getHandlaggarId());
      assertEquals(List.of(expectedIndivid),
            assignResponse.getOperativUppgift().getIndivider());
      assertEquals(OperativUppgift.StatusEnum.TILLDELAD, assignResponse.getOperativUppgift().getStatus());
      assertNotNull(assignResponse.getOperativUppgift().getSkapad());

      //
      // Verify OUL status notification is produced
      //
      messages = waitForMessages(oulStatusNotificationChannel);
      assertEquals(1, messages.size());

      message = messages.getFirst().getPayload();
      assertInstanceOf(OperativtUppgiftslagerStatusMessage.class, message);

      var expectedUtforare = new se.fk.rimfrost.Idtyp();
      expectedUtforare.setTypId("116759e4-18fd-4209-849c-90abbd257d22");
      expectedUtforare.setVarde(handlaggarId.toString());

      var oulStatusMessage = (OperativtUppgiftslagerStatusMessage) message;
      assertEquals(handlaggningId, oulStatusMessage.getHandlaggningId());
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
      assertEquals(UUID.fromString(uppgiftId), assignedTask.getUppgiftId());

      //
      // Simulate rule being finished by sending avslutad status update
      //
      sendStatusUpdateRequest(uppgiftId, Status.AVSLUTAD);

      //
      // Verify OUL status notification is produced
      //
      messages = waitForMessages(oulStatusNotificationChannel);
      assertEquals(1, messages.size());

      message = messages.getFirst().getPayload();
      assertInstanceOf(OperativtUppgiftslagerStatusMessage.class, message);

      oulStatusMessage = (OperativtUppgiftslagerStatusMessage) message;
      assertEquals(handlaggningId, oulStatusMessage.getHandlaggningId());
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
}
