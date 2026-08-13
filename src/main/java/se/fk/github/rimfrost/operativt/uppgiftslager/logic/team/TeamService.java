package se.fk.github.rimfrost.operativt.uppgiftslager.logic.team;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import java.util.List;

/**
 * Provides team membership information for handläggare.
 *
 * <p>The implementation ({@link TeamApiService}) resolves membership via the team API.
 */
public interface TeamService
{
   /**
    * Returns all members of the given handläggare's team(s).
    *
    * @param caller the calling handläggare's identity
    * @return immutable list of team member identities
    */
   List<Idtyp> teamMembers(Idtyp caller);

   /**
    * Returns whether {@code caller} and {@code other} share at least one team.
    *
    * @param caller the calling handläggare's identity
    * @param other  the other handläggare's identity to compare against
    * @return {@code true} if they belong to at least one common team
    */
   boolean isSameTeam(Idtyp caller, Idtyp other);
}
