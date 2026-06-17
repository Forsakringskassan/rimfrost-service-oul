package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.junit.QuarkusTest;
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
public class OulTeamTest extends OulTestBase
{
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
}
