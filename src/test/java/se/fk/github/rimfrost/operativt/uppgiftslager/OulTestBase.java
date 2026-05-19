package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.BeforeEach;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.GetUppgifterHandlaggareResponse;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.PostUppgifterHandlaggareResponse;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;

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

   @BeforeEach
   public void resetOul()
   {
      // This reset method attempts to clean up the OUL task queue by assigning
      // any non-assigned tasks to a randomly generated handlaggare id until no
      // more tasks are assigned to the id. Once all available tasks are assigned,
      // end requests are sent to each assigned task to remove them from the queue.
      //
      // This should be replaced with a better solution once permanent storage
      // is added.

      var handlaggarId = UUID.randomUUID();
      PostUppgifterHandlaggareResponse assignResponse;

      do
      {
         assignResponse = assignTaskToHandlaggare(handlaggarId);
      }
      while (assignResponse != null && assignResponse.getOperativUppgift() != null);

      var handlaggareUppgifterResponse = getAssignedTasks(handlaggarId);

      if (handlaggareUppgifterResponse != null && handlaggareUppgifterResponse.getOperativaUppgifter() != null)
      {
         for (var assignedTask : handlaggareUppgifterResponse.getOperativaUppgifter())
         {
            sendEndUppgiftRequest(assignedTask.getUppgiftId(), newEndUppgiftRequest("Cleanup"));
         }
      }

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
