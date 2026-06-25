package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.GetUppgifterHandlaggareResponse;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgiftHandlaggareResponse;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgifterHandlaggareResponse;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UpdateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningPage;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.StorageTestCleaner;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;
import java.util.UUID;
import static io.restassured.RestAssured.given;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

public abstract class OulTestBase
{
   protected OulKafkaConnector oulKafkaConnector;

   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   @Inject
   ObjectMapper objectMapper;

   @Inject
   StorageTestCleaner storageTestCleaner;

   @BeforeEach
   public void resetOul()
   {
      RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
            ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((cls, charset) -> objectMapper));
      storageTestCleaner.clearAll();

      if (oulKafkaConnector == null)
      {
         oulKafkaConnector = new OulKafkaConnector(inMemoryConnector);
      }

      oulKafkaConnector.clear();
   }

   public UppgiftResponse sendCreateUppgiftRequest(CreateUppgiftRequest createUppgiftRequest)
   {
      return given().contentType(ContentType.JSON).body(createUppgiftRequest)
            .when().post("/uppgifter")
            .then().statusCode(201)
            .extract().as(UppgiftResponse.class);
   }

   public void sendCreateUppgiftRequest(CreateUppgiftRequest createUppgiftRequest, int expectedStatusCode)
   {
      given().contentType(ContentType.JSON).body(createUppgiftRequest)
            .when().post("/uppgifter")
            .then().statusCode(expectedStatusCode);
   }

   public static UppgiftResponse sendEndUppgiftRequest(UUID uppgiftId, EndUppgiftRequest endUppgiftRequest)
   {
      return given().contentType(ContentType.JSON).body(endUppgiftRequest)
            .when().post("/uppgifter/" + uppgiftId + "/end")
            .then().statusCode(200)
            .extract().as(UppgiftResponse.class);
   }

   public static void sendEndUppgiftRequest(UUID uppgiftId, EndUppgiftRequest endUppgiftRequest, int expectedResponseStatusCode)
   {
      given().contentType(ContentType.JSON).body(endUppgiftRequest)
            .when().post("/uppgifter/" + uppgiftId + "/end").then().statusCode(expectedResponseStatusCode);
   }

   public static UppgiftPage getUppgifter(int limit)
   {
      return given().contentType(ContentType.JSON)
            .queryParam("limit", limit)
            .when().get("/uppgifter")
            .then().statusCode(200)
            .extract().as(UppgiftPage.class);
   }

   public static UppgiftPage getUppgifter(int limit, int offset, UUID sorteringsordningId)
   {
      var req = given().contentType(ContentType.JSON)
            .queryParam("limit", limit)
            .queryParam("offset", offset);
      if (sorteringsordningId != null)
      {
         req = req.queryParam("sorteringsordningId", sorteringsordningId);
      }
      return req.when().get("/uppgifter")
            .then().statusCode(200)
            .extract().as(UppgiftPage.class);
   }

   public static void getUppgifter(int limit, UUID sorteringsordningId, int expectedStatus)
   {
      given().contentType(ContentType.JSON)
            .queryParam("limit", limit)
            .queryParam("sorteringsordningId", sorteringsordningId)
            .when().get("/uppgifter")
            .then().statusCode(expectedStatus);
   }

   /**
    * Returns a bearer token encoding the given handläggare identity using the standard test typId.
    *
    * @param handlaggarId the handläggare varde
    * @return Authorization header value
    */
   public static String bearerToken(UUID handlaggarId)
   {
      return "Bearer " + oulHandlaggareTypId + ":" + handlaggarId;
   }

   /**
    * Assigns a new task to the given handläggare via POST /uppgifter/handlaggare.
    *
    * @param handlaggarId the handläggare varde
    * @return the assigned task
    */
   public static PostUppgifterHandlaggareResponse assignTaskToHandlaggare(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON)
            .header("Authorization", bearerToken(handlaggarId))
            .when().post("/uppgifter/handlaggare")
            .then().statusCode(200).extract().as(PostUppgifterHandlaggareResponse.class);
   }

   public static OperativUppgift unassignTask(UUID uppgiftId)
   {
      return given().contentType(ContentType.JSON).when().post("/uppgifter/" + uppgiftId + "/unassign").then().statusCode(200)
            .extract().as(OperativUppgift.class);
   }

   public static void unassignTask(UUID uppgiftId, int expectedStatus)
   {
      given().contentType(ContentType.JSON).when().post("/uppgifter/" + uppgiftId + "/unassign").then()
            .statusCode(expectedStatus);
   }

   public static OperativUppgift updateTask(UUID uppgiftId, UpdateUppgiftRequest updateUppgiftRequest)
   {
      return given().contentType(ContentType.JSON).body(updateUppgiftRequest)
            .when().patch("/uppgifter/{uppgiftId}", uppgiftId).then().statusCode(200).extract().as(OperativUppgift.class);
   }

   public static void updateTask(UUID uppgiftId, UpdateUppgiftRequest updateUppgiftRequest, int expectedStatus)
   {
      given().contentType(ContentType.JSON).body(updateUppgiftRequest)
            .when().patch("/uppgifter/{uppgiftId}", uppgiftId).then().statusCode(expectedStatus);
   }

   /**
    * Returns tasks assigned to the given handläggare via GET /uppgifter/handlaggare.
    *
    * @param handlaggarId the handläggare varde
    * @return assigned tasks
    */
   public static GetUppgifterHandlaggareResponse getAssignedTasks(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON)
            .header("Authorization", bearerToken(handlaggarId))
            .when().get("/uppgifter/handlaggare")
            .then().statusCode(200).extract().as(GetUppgifterHandlaggareResponse.class);
   }

   /**
    * Returns all tasks assigned to any team member of the given handläggare via GET /uppgifter/team.
    *
    * @param handlaggarId the caller's handläggare varde
    * @return team tasks
    */
   public static GetUppgifterHandlaggareResponse getTeamTasks(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON)
            .header("Authorization", bearerToken(handlaggarId))
            .when().get("/uppgifter/team")
            .then().statusCode(200).extract().as(GetUppgifterHandlaggareResponse.class);
   }

   /**
    * Reassigns the given uppgift to the calling handläggare via POST /uppgifter/{uppgift_id}/handlaggare.
    *
    * @param uppgiftId        the ID of the uppgift to reassign
    * @param callerHandlaggarId the caller's handläggare varde
    * @return the updated uppgift
    */
   public static PostUppgiftHandlaggareResponse reassignTask(UUID uppgiftId, UUID callerHandlaggarId)
   {
      return given().contentType(ContentType.JSON)
            .header("Authorization", bearerToken(callerHandlaggarId))
            .when().post("/uppgifter/" + uppgiftId + "/handlaggare")
            .then().statusCode(200).extract().as(PostUppgiftHandlaggareResponse.class);
   }

   /**
    * Attempts to reassign the given uppgift, expecting the given HTTP status code.
    *
    * @param uppgiftId          the ID of the uppgift to reassign
    * @param callerHandlaggarId the caller's handläggare varde
    * @param expectedStatus     expected HTTP response status
    */
   public static void reassignTask(UUID uppgiftId, UUID callerHandlaggarId, int expectedStatus)
   {
      given().contentType(ContentType.JSON)
            .header("Authorization", bearerToken(callerHandlaggarId))
            .when().post("/uppgifter/" + uppgiftId + "/handlaggare")
            .then().statusCode(expectedStatus);
   }

   public static SorteringsordningResponse sendCreateSorteringsordningRequest(SorteringsordningSpec spec)
   {
      return given().contentType(ContentType.JSON).body(spec)
            .when().post("/sorteringsordning")
            .then().statusCode(201)
            .extract().as(SorteringsordningResponse.class);
   }

   public static void sendCreateSorteringsordningRequest(SorteringsordningSpec spec, int expectedStatus)
   {
      given().contentType(ContentType.JSON).body(spec)
            .when().post("/sorteringsordning")
            .then().statusCode(expectedStatus);
   }

   public static SorteringsordningResponse getDefaultSorteringsordning()
   {
      return given().contentType(ContentType.JSON)
            .when().get("/sorteringsordning/default")
            .then().statusCode(200)
            .extract().as(SorteringsordningResponse.class);
   }

   public static void getDefaultSorteringsordning(int expectedStatus)
   {
      given().contentType(ContentType.JSON)
            .when().get("/sorteringsordning/default")
            .then().statusCode(expectedStatus);
   }

   public static SorteringsordningPage getSorteringsordningar(int limit)
   {
      return given().contentType(ContentType.JSON)
            .queryParam("limit", limit)
            .when().get("/sorteringsordning")
            .then().statusCode(200)
            .extract().as(SorteringsordningPage.class);
   }

   public static SorteringsordningPage getSorteringsordningar(int limit, int offset)
   {
      return given().contentType(ContentType.JSON)
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .when().get("/sorteringsordning")
            .then().statusCode(200)
            .extract().as(SorteringsordningPage.class);
   }

   public static void getSorteringsordningarWithExpectedStatus(Integer limit, int expectedStatus)
   {
      var req = given().contentType(ContentType.JSON);
      if (limit != null)
      {
         req = req.queryParam("limit", limit);
      }
      req.when().get("/sorteringsordning").then().statusCode(expectedStatus);
   }

   public static SorteringsordningResponse getSorteringsordning(UUID id)
   {
      return given().contentType(ContentType.JSON)
            .when().get("/sorteringsordning/{id}", id)
            .then().statusCode(200)
            .extract().as(SorteringsordningResponse.class);
   }

   public static void getSorteringsordning(UUID id, int expectedStatus)
   {
      given().contentType(ContentType.JSON)
            .when().get("/sorteringsordning/{id}", id)
            .then().statusCode(expectedStatus);
   }

   public static void deleteSorteringsordning(UUID id)
   {
      given().contentType(ContentType.JSON)
            .when().delete("/sorteringsordning/{id}", id)
            .then().statusCode(204);
   }

   public static void deleteSorteringsordning(UUID id, int expectedStatus)
   {
      given().contentType(ContentType.JSON)
            .when().delete("/sorteringsordning/{id}", id)
            .then().statusCode(expectedStatus);
   }

   public static void setDefaultSorteringsordning(UUID id)
   {
      given().contentType(ContentType.JSON)
            .when().put("/sorteringsordning/{id}/default", id)
            .then().statusCode(204);
   }

   public static void setDefaultSorteringsordning(UUID id, int expectedStatus)
   {
      given().contentType(ContentType.JSON)
            .when().put("/sorteringsordning/{id}/default", id)
            .then().statusCode(expectedStatus);
   }

   public static UppgiftPage sendPreviewRequest(SorteringsordningSpec spec, int limit, Integer offset)
   {
      var req = given().contentType(ContentType.JSON).body(spec)
            .queryParam("limit", limit);
      if (offset != null)
      {
         req = req.queryParam("offset", offset);
      }
      return req.when().post("/sorteringsordning/preview")
            .then().statusCode(200)
            .extract().as(UppgiftPage.class);
   }
}
