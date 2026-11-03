package se.fk.github.rimfrost.operativt.uppgiftslager.logic.permissions;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;

/**
 * Checks whether a handläggare has SID-behörighet (authorization to handle uppgifter
 * involving individer with skyddad identitet).
 */
public interface SidBehorighetChecker
{
   /**
    * Returns whether the given handläggare has SID-behörighet.
    * Implementations should fail open — returning {@code false} on any unresolvable error
    * rather than propagating the failure, since a missing behörighet check should degrade
    * gracefully rather than block assignment or listing operations.
    *
    * @param handlaggare the handläggare identity to check
    * @return {@code true} if the handläggare has SID-behörighet
    */
   boolean hasSidBehorighet(Idtyp handlaggare);
}
