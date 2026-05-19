package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newEndUppgiftRequest;

@QuarkusTest
public class OulManagementTest extends OulTestBase
{
   @Test
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
}
