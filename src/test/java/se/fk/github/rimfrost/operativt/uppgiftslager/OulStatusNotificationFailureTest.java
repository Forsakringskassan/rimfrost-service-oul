package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.InjectMock;
import io.restassured.http.ContentType;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka.OperativtUppgiftslagerProducer;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka.StatusNotificationException;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.OperativUppgift;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;

@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class OulStatusNotificationFailureTest extends OulTestBase
{
   @InjectMock
   OperativtUppgiftslagerProducer producer;

   @Test
   @DisplayName("OUL-FR-04.9, OUL-FR-06.5: Hämta ny uppgift — misslyckad statusnotifiering ger fel och uppgiften kan hämtas igen")
   public void should_hand_back_task_when_status_notification_fails()
   {
      var handlaggareId = UUID.randomUUID();
      var createResponse = sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));

      Mockito.doThrow(new StatusNotificationException("Kafka unavailable", new RuntimeException("nack")))
            .when(producer).publishTaskStatusUpdate(Mockito.any(), Mockito.any());
      given().contentType(ContentType.JSON)
            .header("Authorization", bearerToken(handlaggareId))
            .when().post("/uppgifter/handlaggare")
            .then().statusCode(500).body(emptyString());

      Mockito.reset(producer);
      var assignResponse = assignTaskToHandlaggare(handlaggareId);

      assertEquals(createResponse.getUppgiftId(), assignResponse.getOperativUppgift().getUppgiftId());
      assertEquals(OperativUppgift.StatusEnum.TILLDELAD, assignResponse.getOperativUppgift().getStatus());
   }
}
