package se.fk.github.rimfrost.operativt.uppgiftslager.logic.team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.HardcodedTeamService.HANDLAGGARE_TYP_ID;
import static se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.HardcodedTeamService.TEAM_MEMBER_1;
import static se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.HardcodedTeamService.TEAM_MEMBER_2;
import static se.fk.github.rimfrost.operativt.uppgiftslager.logic.team.HardcodedTeamService.TEAM_MEMBER_3;

/**
 * Unit tests for {@link HardcodedTeamService}.
 */
class HardcodedTeamServiceTest
{
   private HardcodedTeamService teamService;

   @BeforeEach
   void setUp()
   {
      teamService = new HardcodedTeamService();
   }

   @Test
   @DisplayName("Team member 1 is recognised as a team member")
   void teamMember1_isTeamMember()
   {
      assertTrue(teamService.isTeamMember(idtyp(TEAM_MEMBER_1)));
   }

   @Test
   @DisplayName("Team member 2 is recognised as a team member")
   void teamMember2_isTeamMember()
   {
      assertTrue(teamService.isTeamMember(idtyp(TEAM_MEMBER_2)));
   }

   @Test
   @DisplayName("Team member 3 is recognised as a team member")
   void teamMember3_isTeamMember()
   {
      assertTrue(teamService.isTeamMember(idtyp(TEAM_MEMBER_3)));
   }

   @Test
   @DisplayName("Random UUID with correct typId is not a team member")
   void randomId_isNotTeamMember()
   {
      assertFalse(teamService.isTeamMember(idtyp(UUID.randomUUID())));
   }

   @Test
   @DisplayName("Known typId but unknown varde is not a team member")
   void unknownVarde_isNotTeamMember()
   {
      var unknownMember = ImmutableIdtyp.builder()
            .typId(HANDLAGGARE_TYP_ID)
            .varde("a1a1a1a1-0000-0000-0000-000000000099")
            .build();

      assertFalse(teamService.isTeamMember(unknownMember));
   }

   @Test
   @DisplayName("Known UUID with wrong typId is not a team member")
   void wrongTypId_isNotTeamMember()
   {
      var wrongTypId = ImmutableIdtyp.builder()
            .typId("wrong-typ-id")
            .varde(TEAM_MEMBER_1.toString())
            .build();

      assertFalse(teamService.isTeamMember(wrongTypId));
   }

   @Test
   @DisplayName("teamMembers() returns exactly the three hardcoded members")
   void teamMembers_returnsAllThreeMembers()
   {
      var members = teamService.teamMembers();

      assertEquals(3, members.size());
      assertTrue(members.contains(idtyp(TEAM_MEMBER_1)));
      assertTrue(members.contains(idtyp(TEAM_MEMBER_2)));
      assertTrue(members.contains(idtyp(TEAM_MEMBER_3)));
   }

   private static se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp idtyp(UUID id)
   {
      return ImmutableIdtyp.builder()
            .typId(HANDLAGGARE_TYP_ID)
            .varde(id.toString())
            .build();
   }
}
