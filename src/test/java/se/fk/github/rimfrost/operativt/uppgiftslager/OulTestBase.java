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
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgifterHandlaggareResponse;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UpdateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;

import java.util.List;
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

   public static PostUppgifterHandlaggareResponse assignTaskToHandlaggare(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON).when()
            .post("/uppgifter/handlaggare/" + oulHandlaggareTypId + "/{handlaggarId}", handlaggarId).then()
            .statusCode(200).extract().as(PostUppgifterHandlaggareResponse.class);
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

   public static GetUppgifterHandlaggareResponse getAssignedTasks(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON).when()
            .get("/uppgifter/handlaggare/" + oulHandlaggareTypId + "/{handlaggarId}", handlaggarId).then()
            .statusCode(200).extract().as(GetUppgifterHandlaggareResponse.class);
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

   public static List<SorteringsordningResponse> getSorteringsordningar()
   {
      return given().contentType(ContentType.JSON)
            .when().get("/sorteringsordning")
            .then().statusCode(200)
            .extract().jsonPath().getList(".", SorteringsordningResponse.class);
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
