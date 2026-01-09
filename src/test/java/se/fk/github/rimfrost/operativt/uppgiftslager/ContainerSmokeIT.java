package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessage;
import se.fk.rimfrost.OperativtUppgiftslagerResponseMessage;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;
import se.fk.rimfrost.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
@Testcontainers
public class ContainerSmokeIT
{

   private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
   private static KafkaContainer kafka;
   private static GenericContainer<?> oul;
   private static GenericContainer<?> wiremock;
   private static final String kafkaImage = TestConfig.get("kafka.image");
   private static final String oulImage = TestConfig.get("oul.image");
   private static final String oulRequestsTopic = TestConfig.get("oul.requests.topic");
   private static final String oulResponsesTopic = TestConfig.get("oul.responses.topic");
   private static final String oulStatusNotificationTopic = TestConfig.get("oul.status-notification.topic");
   private static final String oulStatusControlTopic = TestConfig.get("oul.status-control.topic");
   private static final String networkAlias = TestConfig.get("network.alias");
   private static final String smallryeKafkaBootstrapServers = networkAlias + ":9092";
   private static final Network network = Network.newNetwork();
   private static final String wiremockUrl = "http://wiremock:8080";

   private static final HttpClient httpClient = HttpClient.newHttpClient();

   @BeforeAll
   static void setup()
   {
      setupKafka();
      setupOul();
   }

   static void setupKafka()
   {
      kafka = new KafkaContainer(DockerImageName.parse(kafkaImage)
            .asCompatibleSubstituteFor("apache/kafka"))
            .withNetwork(network)
            .withNetworkAliases(networkAlias);
      kafka.start();
      System.out.println("Kafka host bootstrap servers: " + kafka.getBootstrapServers());
      try
      {
         createTopic(oulRequestsTopic, 1, (short) 1);
         createTopic(oulResponsesTopic, 1, (short) 1);
         createTopic(oulStatusNotificationTopic, 1, (short) 1);
         createTopic(oulStatusControlTopic, 1, (short) 1);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to create Kafka topics", e);
      }
   }

   static KafkaConsumer<String, String> createKafkaConsumer(String topic)
   {
      String bootstrap = kafka.getBootstrapServers().replace("PLAINTEXT://", "");
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
      consumer.subscribe(Collections.singletonList(topic));
      return consumer;
   }

   static void setupOul()
   {
      Properties props = new Properties();
      try (InputStream in = ContainerSmokeIT.class.getResourceAsStream("/test.properties"))
      {
         if (in == null)
         {
            throw new RuntimeException("Could not find /test.properties in classpath");
         }
         props.load(in);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to load test.properties", e);
      }

      String containerConfigPath = "/deployments/test-config.yaml";
      //noinspection resource
      oul = new GenericContainer<>(DockerImageName.parse(oulImage))
            .withNetwork(network)
            .withExposedPorts(8080)
            .withEnv("REGEL_CONFIG_PATH", containerConfigPath)
            .withCopyFileToContainer(
                  MountableFile.forClasspathResource("config-test.yaml"),
                  containerConfigPath)
            .withEnv("MP_MESSAGING_CONNECTOR_SMALLRYE_KAFKA_BOOTSTRAP_SERVERS", smallryeKafkaBootstrapServers)
            .withEnv("QUARKUS_PROFILE", "test") // force test profile
            .withEnv("FOLKBOKFORD_API_BASE_URL", wiremockUrl)
            .withEnv("ARBETSGIVARE_API_BASE_URL", wiremockUrl)
            .withEnv("KUNDBEHOVSFLODE_API_BASE_URL", wiremockUrl);
      oul.start();
   }

   @SuppressWarnings("SameParameterValue")
   static void createTopic(String topicName, int numPartitions, short replicationFactor) throws Exception
   {
      String bootstrap = kafka.getBootstrapServers().replace("PLAINTEXT://", "");
      Properties props = new Properties();
      props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
      try (AdminClient admin = AdminClient.create(props))
      {
         NewTopic topic = new NewTopic(topicName, numPartitions, replicationFactor);
         admin.createTopics(List.of(topic)).all().get();
         System.out.printf("Created topic: %S%n", topicName);
      }
   }

   @AfterAll
   static void tearDown()
   {
      if (oul != null)
         oul.stop();
      if (kafka != null)
         kafka.stop();
      if (wiremock != null)
         wiremock.stop();
   }

   private String readKafkaMessage(String topic)
   {
      String bootstrap = kafka.getBootstrapServers().replace("PLAINTEXT://", "");
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props))
      {
         System.out.printf("New kafka consumer subscribing to topic: %s%n", topic);
         consumer.subscribe(Collections.singletonList(topic));
         ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(120));
         if (records.isEmpty())
         {
            throw new IllegalStateException("No Kafka message received on topic " + topic);
         }
         var kafkaMessage = records.iterator().next().value();
         System.out.printf("Received kafkaMessage on %s: %s%n", topic, kafkaMessage);
         return kafkaMessage;
      }
   }

   private void sendOulRequest(String kundbehovsflodeId,
                               String version,
                               String kundbehov,
                               String regel,
                               String beskrivning,
                               String roll,
                               String url) throws Exception
   {
      OperativtUppgiftslagerRequestMessage message = new OperativtUppgiftslagerRequestMessage();
      message.setVersion(version);
      message.setKundbehovsflodeId(kundbehovsflodeId);
      message.setKundbehov(kundbehov);
      message.setRegel(regel);
      message.setBeskrivning(beskrivning);
      message.setRoll(roll);
      message.setUrl(url);
      // Serialize entire payload to JSON
      String eventJson = mapper.writeValueAsString(message);

      Properties props = new Properties();
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

      try (KafkaProducer<String, String> producer = new KafkaProducer<>(props))
      {
         ProducerRecord<String, String> record = new ProducerRecord<>(
               oulRequestsTopic,
               eventJson);
         System.out.printf("Kafka sending to topic : %s, json: %s%n", oulRequestsTopic, eventJson);
         producer.send(record).get();
      }
   }

   public record OulCorrelation(
         String kundbehovsflodeId,
         String uppgiftId,
         String kafkaKey)
   {
   }

   private KafkaConsumer<String, String> createConsumer()
   {
      Properties props = new Properties();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
      props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
      return new KafkaConsumer<>(props);
   }

   private CompletableFuture<OulCorrelation> startKafkaResponderOul(ExecutorService executor)
   {
      return CompletableFuture.supplyAsync(() -> {
         try (KafkaConsumer<String, String> consumer = createConsumer())
         {

            consumer.subscribe(Collections.singletonList(oulRequestsTopic));

            ConsumerRecord<String, String> record = pollForKafkaMessage(consumer, oulRequestsTopic);

            // Deserialize request
            OperativtUppgiftslagerRequestMessage request = mapper.readValue(record.value(),
                  OperativtUppgiftslagerRequestMessage.class);

            String kundbehovsflodeId = request.getKundbehovsflodeId();
            String uppgiftId = UUID.randomUUID().toString();

            // Build response
            OperativtUppgiftslagerResponseMessage responseMessage = new OperativtUppgiftslagerResponseMessage();
            responseMessage.setKundbehovsflodeId(kundbehovsflodeId);
            responseMessage.setUppgiftId(uppgiftId);

            sendOulResponse(record.key(), request, oulResponsesTopic, responseMessage);

            System.out.printf(
                  "Sent mock Kafka response for kundbehovsflodeId=%s, uppgiftId=%s%n",
                  kundbehovsflodeId, uppgiftId);

            // Return correlation info to the test
            return new OulCorrelation(
                  kundbehovsflodeId,
                  uppgiftId,
                  record.key());

         }
         catch (Exception e)
         {
            throw new RuntimeException("Kafka responder failed", e);
         }
      }, executor);
   }

   @SuppressWarnings("SameParameterValue")
   private ConsumerRecord<String, String> pollForKafkaMessage(
         KafkaConsumer<String, String> consumer,
         String topic)
   {
      long deadline = System.currentTimeMillis() + 30000;
      while (System.currentTimeMillis() < deadline)
      {
         ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
         if (!records.isEmpty())
         {
            return records.iterator().next();
         }
      }
      throw new IllegalStateException("No Kafka message received on " + topic);
   }

   public HttpResponse<String> sendGetUppgifterHandlaggare(HttpClient httpClient, String handlaggareId)
         throws IOException, InterruptedException
   {
      var url = "http://" + oul.getHost() + ":" + oul.getMappedPort(8080) + "/uppgifter/handlaggare/"
            + handlaggareId;
      System.out.printf("Sending GET uppgifter handlaggare to: %s%n", url);
      HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      assertEquals(200, response.statusCode());
      return response;
   }

    public HttpResponse<String> sendPostUppgifterHandlaggare(HttpClient httpClient, String handlaggareId)
            throws IOException, InterruptedException
    {
        var url = "http://" + oul.getHost() + ":" + oul.getMappedPort(8080) + "/uppgifter/handlaggare/"
                + handlaggareId;
        System.out.printf("Sending POST uppgifter handlaggare to: %s%n", url);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return response;
    }

   @ParameterizedTest
   @CsvSource(
   {
         "5367f6b8-cc4a-11f0-8de9-199901011234"
   })
   void TestRtfManuellSmoke(String kundbehovsflodeId) throws Exception
   {
      System.out.printf("Starting TestRtfManuellSmoke. %S%n", kundbehovsflodeId);
      // Send Rtf manuell request to start workflow
      sendOulRequest(kundbehovsflodeId, version, kundbehov, regel, beskrivning, roll, url);
      // Start background Kafka responder handling request to Operativt uppgiftslager
      ExecutorService executorOul = Executors.newSingleThreadExecutor();
      CompletableFuture<OulCorrelation> responderOul = startKafkaResponderOul(executorOul);
      OulCorrelation oulCorrelation = responderOul.join();
      String uppgiftId = oulCorrelation.uppgiftId();
      //
      // Verify GET kundbehovsflöde requested
      //
      List<LoggedRequest> kundbehovsflodeRequests = waitForWireMockRequest(wiremockClient,
            kundbehovsflodeEndpoint + kundbehovsflodeId, 3);
      var getRequests = kundbehovsflodeRequests.stream().filter(p -> p.getMethod().equals(RequestMethod.GET)).toList();
      assertFalse(getRequests.isEmpty());
      //
      // Verify oul message produced
      //
      String kafkaMessage = readKafkaMessage(oulRequestsTopic);
      OperativtUppgiftslagerRequestMessage oulRequestMessage = mapper.readValue(kafkaMessage,
            OperativtUppgiftslagerRequestMessage.class);
      assertEquals(kundbehovsflodeId, oulRequestMessage.getKundbehovsflodeId());
      assertEquals("TestUppgiftBeskrivning", oulRequestMessage.getBeskrivning());
      assertEquals("TestUppgiftNamn", oulRequestMessage.getRegel());
      assertEquals("TestUppgiftVerksamhetslogik", oulRequestMessage.getVerksamhetslogik());
      assertEquals("TestUppgiftRoll", oulRequestMessage.getRoll());
      assertTrue(oulRequestMessage.getUrl().contains("/regel/rtf-manuell"));

      //
      // Verify PUT kundbehovsflöde requested
      //
      kundbehovsflodeRequests = waitForWireMockRequest(wiremockClient, kundbehovsflodeEndpoint + kundbehovsflodeId, 3);
      var putRequests = kundbehovsflodeRequests.stream().filter(p -> p.getMethod().equals(RequestMethod.PUT)).toList();
      assertEquals(1, putRequests.size());
      var sentJson = putRequests.getLast().getBodyAsString();
      var sentPutKundbehovsflodeRequest = mapper.readValue(sentJson, PutKundbehovsflodeRequest.class);
      assertEquals(kundbehovsflodeId, sentPutKundbehovsflodeRequest.getUppgift().getKundbehovsflode().getId().toString());
      assertEquals(UppgiftStatus.AVSLUTAD, sentPutKundbehovsflodeRequest.getUppgift().getUppgiftStatus()); // TODO borde ej vara AVSLUTAD nu ??
      // TODO Add more checks above
      //
      // mock status update from OUL
      //
      OperativtUppgiftslagerStatusMessage statusMessage = new OperativtUppgiftslagerStatusMessage();
      statusMessage.setStatus(Status.NY);
      statusMessage.setUppgiftId(oulCorrelation.uppgiftId);
      statusMessage.setKundbehovsflodeId(oulCorrelation.kundbehovsflodeId);
      sendOulStatus(oulCorrelation.kafkaKey, oulStatusNotificationTopic, statusMessage);
      //
      // verify expected actions from rtf manual as result of new status reported
      //
      kundbehovsflodeRequests = waitForWireMockRequest(wiremockClient, kundbehovsflodeEndpoint + kundbehovsflodeId, 5);
      putRequests = kundbehovsflodeRequests.stream().filter(p -> p.getMethod().equals(RequestMethod.PUT)).toList();
      assertEquals(2, putRequests.size());
      sentJson = putRequests.getLast().getBodyAsString();
      sentPutKundbehovsflodeRequest = mapper.readValue(sentJson, PutKundbehovsflodeRequest.class);
      assertEquals(kundbehovsflodeId, sentPutKundbehovsflodeRequest.getUppgift().getKundbehovsflode().getId().toString());
      assertEquals(UppgiftStatus.AVSLUTAD, sentPutKundbehovsflodeRequest.getUppgift().getUppgiftStatus());
      //
      // mock GET operation requested from portal FE
      //
      var httpResponse = sendGetRtfManuell(httpClient, kundbehovsflodeId);
      //
      // Verify GET operation response
      //
      var getDataResponse = mapper.readValue(httpResponse.body(), GetDataResponse.class);
      assertEquals(kundbehovsflodeId, getDataResponse.getKundbehovsflodeId().toString());
      // TODO more assertions of content above
      //
      // verify that rule performed requests to kundbehovsflode
      //
      kundbehovsflodeRequests = waitForWireMockRequest(wiremockClient, kundbehovsflodeEndpoint + kundbehovsflodeId, 8);
      putRequests = kundbehovsflodeRequests.stream().filter(p -> p.getMethod().equals(RequestMethod.PUT)).toList();
      assertEquals(3, putRequests.size());
      sentJson = putRequests.getLast().getBodyAsString();
      sentPutKundbehovsflodeRequest = mapper.readValue(sentJson, PutKundbehovsflodeRequest.class);
      assertEquals(kundbehovsflodeId, sentPutKundbehovsflodeRequest.getUppgift().getKundbehovsflode().getId().toString());
      assertEquals(UppgiftStatus.AVSLUTAD, sentPutKundbehovsflodeRequest.getUppgift().getUppgiftStatus());
      //
      // mock PATCH operation from portal FE
      //
      PatchDataRequest patchDataRequest = new PatchDataRequest();
      patchDataRequest.setErsattningId(UUID.fromString("67c5ded8-7697-41fd-b943-c58a1be15c93"));
      patchDataRequest.setAvslagsanledning("");
      patchDataRequest.setSignera(true);
      patchDataRequest.setBeslutsutfall(Beslutsutfall.JA);
      httpResponse = sendPatchRtfManuell(httpClient, kundbehovsflodeId, patchDataRequest);
      //
      // verify that rule performed requests to kundbehovsflode
      //
      kundbehovsflodeRequests = waitForWireMockRequest(wiremockClient, kundbehovsflodeEndpoint + kundbehovsflodeId, 10);
      putRequests = kundbehovsflodeRequests.stream().filter(p -> p.getMethod().equals(RequestMethod.PUT)).toList();
      assertEquals(4, putRequests.size());
      //
      // verify kafka status message sent to oul
      //
      var kafkaOulStatusMessage = readKafkaMessage(oulStatusControlTopic);
      OperativtUppgiftslagerStatusMessage oulStatusMessage = mapper.readValue(kafkaOulStatusMessage,
            OperativtUppgiftslagerStatusMessage.class);
      assertEquals(uppgiftId, oulStatusMessage.getUppgiftId());
      assertEquals(Status.AVSLUTAD, oulStatusMessage.getStatus());
      //
      // verify kafka manuell response message sent to VAH
      //
      var kafkaRtfManuellResponseMessage = readKafkaMessage(rtfManuellResponsesTopic);
      RtfManuellResponseMessagePayload rtfManuellResponseMessagePayload = mapper.readValue(kafkaRtfManuellResponseMessage,
            RtfManuellResponseMessagePayload.class);
      assertEquals(kundbehovsflodeId, rtfManuellResponseMessagePayload.getData().getKundbehovsflodeId());
      assertEquals(RattTillForsakring.JA, rtfManuellResponseMessagePayload.getData().getRattTillForsakring());
   }
}
