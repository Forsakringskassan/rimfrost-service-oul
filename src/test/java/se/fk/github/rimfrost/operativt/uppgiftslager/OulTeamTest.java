package se.fk.github.rimfrost.operativt.uppgiftslager;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.TEAM_MEMBER_1;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.TEAM_MEMBER_2;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.TEAM_MEMBER_3;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

/**
 * Integration tests for team-based task listing (AC1) and reassignment (AC5, AC6, AC7).
 */
@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
public class OulTeamTest extends OulTestBase
{
   private static WireMockServer wireMockServer;

   @BeforeAll
   static void setup()
   {
      wireMockServer = WireMockTestResource.getWireMockServer();
   }

   @Test
   @DisplayName("AC1: GET /uppgifter/team returns all uppgifter assigned to any team member")
   public void getTeamTasks_returnsAllTeamMembersUppgifter()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      assignTaskToHandlaggare(TEAM_MEMBER_1);

      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      assignTaskToHandlaggare(TEAM_MEMBER_2);

      var result = getTeamTasks(TEAM_MEMBER_3);

      assertNotNull(result.getOperativaUppgifter());
      assertEquals(2, result.getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("AC1: GET /uppgifter/team returns empty list when no team members have tasks")
   public void getTeamTasks_returnsEmptyList_whenNoTeamMembersHaveTasks()
   {
      var result = getTeamTasks(TEAM_MEMBER_1);

      assertNotNull(result.getOperativaUppgifter());
      assertEquals(0, result.getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("AC5: POST /uppgifter/{id}/handlaggare reassigns task and returns updated uppgift")
   public void reassignTask_returns200_withUpdatedUppgift()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var original = assignTaskToHandlaggare(TEAM_MEMBER_1);
      var uppgiftId = original.getOperativUppgift().getUppgiftId();

      var result = reassignTask(uppgiftId, TEAM_MEMBER_2);

      assertNotNull(result.getOperativUppgift());
      assertEquals(uppgiftId, result.getOperativUppgift().getUppgiftId());
      assertEquals(oulHandlaggareTypId, result.getOperativUppgift().getHandlaggarId().getTypId());
      assertEquals(TEAM_MEMBER_2.toString(), result.getOperativUppgift().getHandlaggarId().getVarde());
   }

   @Test
   @DisplayName("AC6: After reassignment, task appears in new assignee's list")
   public void reassignTask_taskAppearsInNewAssigneesList()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var original = assignTaskToHandlaggare(TEAM_MEMBER_1);
      var uppgiftId = original.getOperativUppgift().getUppgiftId();

      reassignTask(uppgiftId, TEAM_MEMBER_2);

      var tasks = getAssignedTasks(TEAM_MEMBER_2);
      assertTrue(tasks.getOperativaUppgifter().stream()
            .anyMatch(t -> t.getUppgiftId().equals(uppgiftId)));
   }

   @Test
   @DisplayName("AC6, AC10: After reassignment, task no longer in old assignee's list")
   public void reassignTask_taskNoLongerInOldAssigneesList()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var original = assignTaskToHandlaggare(TEAM_MEMBER_1);
      var uppgiftId = original.getOperativUppgift().getUppgiftId();

      reassignTask(uppgiftId, TEAM_MEMBER_2);

      var tasks = getAssignedTasks(TEAM_MEMBER_1);
      assertTrue(tasks.getOperativaUppgifter().stream()
            .noneMatch(t -> t.getUppgiftId().equals(uppgiftId)));
   }

   @Test
   @DisplayName("OUL-FR-18.5: POST /uppgifter/{id}/handlaggare publishes Kafka status notification after reassignment")
   public void reassignTask_publishesKafkaStatusNotification()
   {
      var createRequest = newCreateUppgiftRequest(UUID.randomUUID());
      sendCreateUppgiftRequest(createRequest);
      var original = assignTaskToHandlaggare(TEAM_MEMBER_1);
      var uppgiftId = original.getOperativUppgift().getUppgiftId();

      oulKafkaConnector.clear();
      reassignTask(uppgiftId, TEAM_MEMBER_2);

      var statusMessage = oulKafkaConnector.waitForOulStatusMessage();

      assertNotNull(statusMessage);
      assertEquals(uppgiftId.toString(), statusMessage.getUppgiftId());
      assertEquals(createRequest.getHandlaggningId().toString(), statusMessage.getHandlaggningId());
      assertEquals("TILLDELAD", statusMessage.getStatus());
      assertEquals(oulHandlaggareTypId, statusMessage.getUtforarId().getTypId());
      assertEquals(TEAM_MEMBER_2.toString(), statusMessage.getUtforarId().getVarde());
   }

   @Test
   @DisplayName("POST /uppgifter/{id}/handlaggare returns 404 when uppgift does not exist")
   public void reassignTask_returns404_whenUppgiftDoesNotExist()
   {
      reassignTask(UUID.randomUUID(), TEAM_MEMBER_1, 404);
   }

   @Test
   @DisplayName("AC7: POST /uppgifter/{id}/handlaggare returns 403 when current assignee is outside team")
   public void reassignTask_returns403_whenCurrentAssigneeIsOutsideTeam()
   {
      var outsider = UUID.randomUUID();

      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      assignTaskToHandlaggare(outsider);
      var uppgiftId = getAssignedTasks(outsider).getOperativaUppgifter().getFirst().getUppgiftId();

      reassignTask(uppgiftId, TEAM_MEMBER_1, 403);
   }

   @Test
   @DisplayName("OUL-FR-17.4: GET /uppgifter/team returns 403 when caller belongs to no known team")
   public void getTeamTasks_returns403_whenCallerHasNoTeam()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      assignTaskToHandlaggare(TEAM_MEMBER_1);

      // Random UUID → catch-all stub returns 200 + empty team list → teamMembers() = [] → 403
      getTeamTasks(UUID.randomUUID(), 403);
   }

   @Test
   @DisplayName("GET /uppgifter/team returns 500 when team API is unavailable")
   public void getTeamTasks_returns500_whenTeamApiUnavailable()
   {
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/individ/.+/team"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      getTeamTasks(UUID.randomUUID(), 500);
   }

   @Test
   @DisplayName("POST /uppgifter/{id}/handlaggare returns 500 when team API is unavailable during isSameTeam check")
   public void reassignTask_returns500_whenTeamApiUnavailable()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var original = assignTaskToHandlaggare(TEAM_MEMBER_1);
      var uppgiftId = original.getOperativUppgift().getUppgiftId();

      wireMockServer.stubFor(WireMock.get(WireMock.urlPathMatching("/individ/.+/team"))
            .willReturn(WireMock.aResponse().withStatus(500)));

      reassignTask(uppgiftId, UUID.randomUUID(), 500);
   }

   @Test
   @DisplayName("POST /uppgifter/{id}/handlaggare returns 403 when team API returns 404 for current assignee (NotFoundException path)")
   public void reassignTask_returns403_whenTeamApiReturns404ForCurrentAssignee()
   {
      var assignee = UUID.fromString("c0ffee00-0000-0000-0000-000000000001");

      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      assignTaskToHandlaggare(assignee);
      var uppgiftId = getAssignedTasks(assignee).getOperativaUppgifter().getFirst().getUppgiftId();

      // Stub assignee's team lookup to 404 → NotFoundException caught → empty set → isSameTeam returns false → 403
      wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo(
            "/individ/" + oulHandlaggareTypId + "/" + assignee + "/team"))
            .willReturn(WireMock.aResponse().withStatus(404)));

      reassignTask(uppgiftId, TEAM_MEMBER_1, 403);
   }
}
