package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newEndUppgiftRequest;

@QuarkusTest
public class OulSequenceTest extends OulTestBase
{
   @Test
   public void smoke_test_oul_sequence()
   {
      //
      // Create uppgift via REST
      //
      var createResponse = sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      assertNotNull(createResponse);
      var uppgiftId = createResponse.getUppgiftId().toString();

      //
      // Assign new task to handlaggare
      //
      var handlaggarId = UUID.randomUUID();
      var assignResponse = assignTaskToHandlaggare(handlaggarId);

      assertNotNull(assignResponse.getOperativUppgift());
      assertEquals(createResponse.getUppgiftId(), assignResponse.getOperativUppgift().getUppgiftId());

      //
      // Verify OUL status notification is produced
      //
      var messages = oulKafkaConnector.waitForMessages(OulKafkaConnector.oulStatusNotificationChannel);
      assertEquals(1, messages.size());
      oulKafkaConnector.clear();

      //
      // Verify assigned task is included in GET response
      //
      var assignedTasks = getAssignedTasks(handlaggarId);

      assertNotNull(assignedTasks);
      assertNotNull(assignedTasks.getOperativaUppgifter());
      assertEquals(1, assignedTasks.getOperativaUppgifter().size());

      //
      // End uppgift via REST
      //
      sendEndUppgiftRequest(UUID.fromString(uppgiftId), newEndUppgiftRequest("Test reason"));

      //
      // Verify assigned task is not included in GET response
      //
      assignedTasks = getAssignedTasks(handlaggarId);

      assertNotNull(assignedTasks);
      assertNotNull(assignedTasks.getOperativaUppgifter());
      assertEquals(0, assignedTasks.getOperativaUppgifter().size());
   }
}
