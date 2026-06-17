package se.fk.github.rimfrost.operativt.uppgiftslager.logic.team;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Hardcoded team service for POC use.
 *
 * <p>Returns a fixed team of three handläggare. These same identities are used as
 * test bearer tokens: {@code Authorization: Bearer <HANDLAGGARE_TYP_ID>:<TEAM_MEMBER_n>}.
 * Replace with a real team API implementation when available.
 */
@ApplicationScoped
public class HardcodedTeamService implements TeamService
{
   /** The handläggare typId used by all hardcoded team members. */
   public static final String HANDLAGGARE_TYP_ID = "card";

   public static final UUID TEAM_MEMBER_1 = UUID.fromString("a1a1a1a1-0000-0000-0000-000000000001");
   public static final UUID TEAM_MEMBER_2 = UUID.fromString("a1a1a1a1-0000-0000-0000-000000000002");
   public static final UUID TEAM_MEMBER_3 = UUID.fromString("a1a1a1a1-0000-0000-0000-000000000003");

   private static final List<Idtyp> TEAM = List.of(
         member(TEAM_MEMBER_1),
         member(TEAM_MEMBER_2),
         member(TEAM_MEMBER_3));

   private static final Set<Idtyp> TEAM_SET = Set.copyOf(TEAM);

   private static Idtyp member(UUID id)
   {
      return ImmutableIdtyp.builder()
            .typId(HANDLAGGARE_TYP_ID)
            .varde(id.toString())
            .build();
   }

   @Override
   public boolean isTeamMember(Idtyp handlaggare)
   {
      return TEAM_SET.contains(handlaggare);
   }

   @Override
   public List<Idtyp> teamMembers()
   {
      return TEAM;
   }
}
