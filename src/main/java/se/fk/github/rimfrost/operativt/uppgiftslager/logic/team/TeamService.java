package se.fk.github.rimfrost.operativt.uppgiftslager.logic.team;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import java.util.List;

/**
 * Provides team membership information for handläggare.
 *
 * <p>The current implementation is hardcoded for POC purposes. When the real team API is
 * available, only this interface's implementation changes — all callers remain unchanged.
 */
public interface TeamService
{
   /**
    * Returns whether the given handläggare is a member of the team.
    *
    * @param handlaggare the handläggare identity to check
    * @return {@code true} if the handläggare is a team member
    */
   boolean isTeamMember(Idtyp handlaggare);

   /**
    * Returns all current team members.
    *
    * @return immutable list of team member identities
    */
   List<Idtyp> teamMembers();
}
