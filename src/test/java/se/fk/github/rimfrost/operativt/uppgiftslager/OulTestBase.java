package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.GetUppgifterHandlaggareResponse;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgifterHandlaggareResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newEndUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

public abstract class OulTestBase
{
   protected OulKafkaConnector oulKafkaConnector;

   @Inject
   @Connector("smallrye-in-memory")
   InMemoryConnector inMemoryConnector;

   @Inject
   StorageTestCleaner storageTestCleaner;

   @BeforeEach
   public void resetOul()
   {
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
            .then().statusCode(200)
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

   public static List<OperativUppgift> getUppgifter()
   {
      return given().contentType(ContentType.JSON).when().get("/uppgifter").then().statusCode(200).extract().jsonPath()
            .getList(".", OperativUppgift.class);
   }

   public static PostUppgifterHandlaggareResponse assignTaskToHandlaggare(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON).when()
            .post("/uppgifter/handlaggare/" + oulHandlaggareTypId + "/{handlaggarId}", handlaggarId).then()
            .statusCode(200).extract().as(PostUppgifterHandlaggareResponse.class);
   }

   public static GetUppgifterHandlaggareResponse getAssignedTasks(UUID handlaggarId)
   {
      return given().contentType(ContentType.JSON).when()
            .get("/uppgifter/handlaggare/" + oulHandlaggareTypId + "/{handlaggarId}", handlaggarId).then()
            .statusCode(200).extract().as(GetUppgifterHandlaggareResponse.class);
   }
}
