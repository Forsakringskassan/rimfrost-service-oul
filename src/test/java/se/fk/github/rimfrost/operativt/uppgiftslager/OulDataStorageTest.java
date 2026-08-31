package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.OulDataStorage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.TEAM_MEMBER_1;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.TEAM_MEMBER_2;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.newCreateUppgiftRequest;
import static se.fk.github.rimfrost.operativt.uppgiftslager.OulTestData.oulHandlaggareTypId;

/**
 * Unit tests for {@link OulDataStorage#unassignUppgiftIfAssignedTo}, the compare-and-clear guard
 * added during FKPOC-940 review to fix a stale-read race where an automatic, SID-driven unassign
 * could otherwise clobber a concurrent legitimate reassignment, or throw when the uppgift no
 * longer exists.
 */
@QuarkusTest
@QuarkusTestResource.List(
{
      @QuarkusTestResource(WireMockTestResource.class)
})
class OulDataStorageTest extends OulTestBase
{
   @Inject
   OulDataStorage storage;

   @Test
   @DisplayName("FKPOC-940: unassignUppgiftIfAssignedTo is a no-op when the uppgift was reassigned to someone else in the meantime")
   public void should_not_unassign_when_reassigned_to_someone_else()
   {
      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var assigned = assignTaskToHandlaggare(TEAM_MEMBER_1);
      var uppgiftId = assigned.getOperativUppgift().getUppgiftId();

      reassignTask(uppgiftId, TEAM_MEMBER_2);

      var staleExpected = ImmutableIdtyp.builder()
            .typId(oulHandlaggareTypId)
            .varde(TEAM_MEMBER_1.toString())
            .build();
      var result = storage.unassignUppgiftIfAssignedTo(uppgiftId, staleExpected);

      assertNull(result);
      assertEquals(1, getAssignedTasks(TEAM_MEMBER_2).getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("FKPOC-940: unassignUppgiftIfAssignedTo unassigns when the uppgift is still assigned to the expected handläggare")
   public void should_unassign_when_still_assigned_to_expected_handlaggare()
   {
      var handlaggareId = UUID.randomUUID();

      sendCreateUppgiftRequest(newCreateUppgiftRequest(UUID.randomUUID()));
      var assigned = assignTaskToHandlaggare(handlaggareId);
      var uppgiftId = assigned.getOperativUppgift().getUppgiftId();

      var expected = ImmutableIdtyp.builder()
            .typId(oulHandlaggareTypId)
            .varde(handlaggareId.toString())
            .build();
      var result = storage.unassignUppgiftIfAssignedTo(uppgiftId, expected);

      assertEquals(uppgiftId, result.uppgiftId());
      assertEquals(0, getAssignedTasks(handlaggareId).getOperativaUppgifter().size());
   }

   @Test
   @DisplayName("FKPOC-940: unassignUppgiftIfAssignedTo returns null instead of throwing when the uppgift no longer exists")
   public void should_return_null_when_uppgift_does_not_exist()
   {
      var expected = ImmutableIdtyp.builder()
            .typId(oulHandlaggareTypId)
            .varde(UUID.randomUUID().toString())
            .build();

      var result = storage.unassignUppgiftIfAssignedTo(UUID.randomUUID(), expected);

      assertNull(result);
   }
}
