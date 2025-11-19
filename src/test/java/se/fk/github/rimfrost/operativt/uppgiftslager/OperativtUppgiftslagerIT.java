package se.fk.github.rimfrost.operativt.uppgiftslager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import se.fk.rimfrost.KogitoProcType;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessageData;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessagePayload;
import se.fk.rimfrost.SpecVersion;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.GetUppgifterResponse;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Uppgift;

@SuppressWarnings("deprecation")
@Testcontainers
public class OperativtUppgiftslagerIT
{
   private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
   private static KafkaContainer kafka;
   private static GenericContainer<?> oul;
   private static final String KAFKA_IMAGE = TestConfig.get("kafka.image");
   private static final String OUL_IMAGE = TestConfig.get("oul.image");
   private static final int OUL_PORT = TestConfig.getInt("oul.port");
   private static final String OUL_REQUESTS_TOPIC = TestConfig.get("oul.requests.topic");
   private static final int TOPIC_TIMEOUT = TestConfig.getInt("topic.timeout");
   private static final String NETWORK_ALIAS = TestConfig.get("network.alias");
   private static final String SMALLRYE_KAFKA_BOOTSTRAP_SERVERS = NETWORK_ALIAS + ":9092";

   private static Map<Integer, String> payloadIds;
   private static Map<Integer, String> messageData;
   private static List<OperativtUppgiftslagerRequestMessagePayload> sentPayloads;

   @BeforeAll
   static void setupContainers()
   {
      Network network = Network.newNetwork();
      kafka = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE)
            .asCompatibleSubstituteFor("apache/kafka"))
            .withNetwork(network)
            .withNetworkAliases(NETWORK_ALIAS);
      kafka.start();
      try
      {
         createTopic(OUL_REQUESTS_TOPIC, 1, (short) 1);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to create Kafka topics", e);
      }

      oul = new GenericContainer<>(DockerImageName.parse(OUL_IMAGE))
            .withNetwork(network)
            .withEnv("MP_MESSAGING_CONNECTOR_SMALLRYE_KAFKA_BOOTSTRAP_SERVERS", SMALLRYE_KAFKA_BOOTSTRAP_SERVERS)
            .withExposedPorts(OUL_PORT);
      oul.start();
      oul.followOutput(frame -> {
         String logLine = frame.getUtf8String();
         System.out.print(logLine);
      });
   }

   @BeforeAll
   static void initializeOulData()
   {
      payloadIds = Map.of(
            1, "f5543b1c-8edc-4965-8be0-3f431b3d5575",
            2, "a1234567-89ab-cdef-0123-456789abcdef",
            3, "0fedcba9-8765-4321-0fed-cba987654321");

      messageData = Map.of(
            1, "1~Ta reda på rätt till vard av husdjur försärking~19900302-1111~gui:1112",
            2, "2~Annan uppgift~19900302-2222~gui:2223",
            3, "3~Tredje uppgift~19900302-3333~gui:3334");

      List<OperativtUppgiftslagerRequestMessagePayload> payloads = createOulPayloads(payloadIds, messageData);
      sentPayloads = new ArrayList<>();
      
      try
      {
         Thread.sleep(5000); // Vänta på att OUL ska bli redo
         sendOulRequests(payloads, OUL_REQUESTS_TOPIC);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to initialize OUL data", e);
      }
   }

   static void createTopic(String topicName, int numPartitions, short replicationFactor) throws Exception
   {
      String bootstrap = kafka.getBootstrapServers().replace("PLAINTEXT://", "");
      Properties props = new Properties();
      props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);

      try (AdminClient admin = AdminClient.create(props))
      {
         NewTopic topic = new NewTopic(topicName, numPartitions, replicationFactor);
         admin.createTopics(List.of(topic)).all().get();
         System.out.println("Created topic: " + topicName);
      }
   }

   @AfterAll
   static void tearDown()
   {
      if (oul != null)
         oul.stop();
      if (kafka != null)
         kafka.stop();
   }

   // private String readKafkaRequestMessage()
   // {
   //    String bootstrap = kafka.getBootstrapServers().replace("PLAINTEXT://", "");
   //    Properties props = new Properties();
   //    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
   //    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-" + System.currentTimeMillis());
   //    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
   //    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
   //    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

   //    try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props))
   //    {
   //       consumer.subscribe(Collections.singletonList(OUL_REQUESTS_TOPIC));
   //       ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

   //       if (records.isEmpty())
   //       {
   //          throw new IllegalStateException("No Kafka message received on topic " + OUL_REQUESTS_TOPIC);
   //       }
   //       return records.iterator().next().value();
   //    }
   // }

   private static void sendOulRequests(List<OperativtUppgiftslagerRequestMessagePayload> payloads, String topic) throws Exception
   {
      Properties props = new Properties();
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

      for (OperativtUppgiftslagerRequestMessagePayload payload : payloads)
      {
         // Serialize entire payload to JSON
         String eventJson = mapper.writeValueAsString(payload);

         try (KafkaProducer<String, String> producer = new KafkaProducer<>(props))
         {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                  topic,
                  payload.getId(),
                  eventJson);
            System.out.println("Kafka mock sending:\n" + eventJson);
            producer.send(record).get();
            sentPayloads.add(payload);
         }
      }
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

   private static List<OperativtUppgiftslagerRequestMessagePayload> createOulPayloads(Map<Integer, String> payloadIds,
         Map<Integer, String> messageDatas)
   {
      List<OperativtUppgiftslagerRequestMessagePayload> payloads = new ArrayList<>();
      for (int i = 0; i < payloadIds.size(); i++)
      {
         OperativtUppgiftslagerRequestMessagePayload payload = new OperativtUppgiftslagerRequestMessagePayload();
         payload.setSpecversion(SpecVersion.NUMBER_1_DOT_0);
         payload.setId(payloadIds.get(i + 1));
         payload.setSource("oul-test");
         payload.setType("operativt-uppgiftslager-requests");
         payload.setTime(OffsetDateTime.now());
         payload.setKogitoparentprociid(payloadIds.get(i + 1));
         payload.setKogitorootprocid("1");
         payload.setKogitoproctype(KogitoProcType.BPMN);
         payload.setKogitoprocinstanceid(payloadIds.get(i + 1));
         payload.setKogitoprocist("1");
         payload.setKogitoprocversion("1");
         payload.setKogitorootprociid(payloadIds.get(i + 1));
         payload.setKogitoprocid(payloadIds.get(i + 1));
         payload.setKogitoprocrefid(payloadIds.get(i + 1));

         String[] dataParts = messageDatas.get(i + 1).split("~");
         OperativtUppgiftslagerRequestMessageData messageData = new OperativtUppgiftslagerRequestMessageData();
         messageData.setVersion("1.0");
         messageData.setUppgiftspecId(dataParts[0]);
         messageData.setAktivitet(dataParts[1]);
         messageData.setPersonnummer(dataParts[2]);
         messageData.setFrontendGuiUrl(dataParts[3]);
         messageData.setProcessId(payloadIds.get(i + 1));
         payload.setData(messageData);
         payloads.add(payload);
      }

      return payloads;
   }

   private static boolean safeEquals(Object a, Object b)
   {
      return a == null ? b == null : a.equals(b);
   }

   private static void assertUppgiftMatches(
         OperativtUppgiftslagerRequestMessageData expected,
         Uppgift actual)
   {
      assertAll(
            "uppgift for processId=" + expected.getProcessId(),
            () -> assertEquals(expected.getVersion(), actual.getVersion(),"version"),
            () -> assertEquals(expected.getUppgiftspecId(), actual.getUppgiftspecId(), "uppgiftspecId"),
            () -> assertEquals(expected.getAktivitet(), actual.getAktivitet(), "aktivitet"),
            () -> assertEquals(expected.getPersonnummer(), actual.getPersonnummer(), "personnummer"),
            () -> assertEquals(expected.getFrontendGuiUrl(), actual.getFrontendGuiUrl(), "frontendGuiUrl"),

            () -> assertNotNull(actual.getUppgiftId(), "uppgiftId should be set"),
            () -> assertNotNull(actual.getSkapad(), "skapad should be set"),
            () -> assertNotNull(actual.getStatus(), "status should be set"),
            () -> assertNotNull(actual.getSpec(), "spec should be set"),
            () -> assertNotNull(actual.getSpec().getId(), "spec.id should be set"),
            () -> assertNotNull(actual.getSpec().getVersion(), "spec.version should be set"),
            () -> assertNotNull(actual.getSpec().getName(), "spec.name should be set"),
            () -> assertEquals("Ny", actual.getStatus(), "status")
      );
   }

   @Test
   void TestRestGetUppgiftById() throws Exception
   {
      for (int i = 0; i < 10; i++) {
         System.out.println("##################################################################");
      }
      HttpClient client = HttpClient.newHttpClient();
      String oulBaseUrl = "http://" + oul.getHost() + ":" + oul.getMappedPort(OUL_PORT);
      HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(oulBaseUrl + "/uppgifter/" + 1))
            .GET()
            .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      
      System.out.println("REST GET /uppgifter/{uppgift_id} response:\n" + response.body());
   }

   @Test
   void TestRestGetUppgifter() throws Exception
   {
      for (int i = 0; i < 10; i++) {
         System.out.println("##################################################################");
      }
      HttpClient client = HttpClient.newHttpClient();
      String oulBaseUrl = "http://" + oul.getHost() + ":" + oul.getMappedPort(OUL_PORT);
      HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(oulBaseUrl + "/uppgifter"))
            .GET()
            .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      System.out.println("REST GET /uppgifter response:\n" + response.body());

      // Verifiera statuskoden
      assertEquals(200, response.statusCode());

      GetUppgifterResponse parsedResponse = mapper.readValue(response.body(), GetUppgifterResponse.class);

      System.out.println("Parsed response has " + parsedResponse.getUppgifter().size() + " tasks.");
      // Verifiera att antalet uppgifter i svaret matchar antalet skickade payloads
      assertEquals(
            sentPayloads.size(),
            parsedResponse.getUppgifter().size(),
            "Number of tasks in REST response does not match sent payloads");

      // För varje skickade payload, verifiera att motsvarande uppgift fanns i svaret och att fälten matchar
      for (OperativtUppgiftslagerRequestMessagePayload sent : sentPayloads)
      {
         OperativtUppgiftslagerRequestMessageData expected = sent.getData();

         Uppgift actual = parsedResponse.getUppgifter().stream()
               .filter(u -> safeEquals(u.getPersonnummer(), expected.getPersonnummer()) &&
                     safeEquals(u.getUppgiftspecId(), expected.getUppgiftspecId()))
               .findFirst()
               .orElseThrow(() -> new AssertionError(
                     "Uppgift not found for personnummer=" + expected.getPersonnummer() +
                           " uppgiftspecId=" + expected.getUppgiftspecId()));

         assertUppgiftMatches(expected, actual);
      }
   }

}
