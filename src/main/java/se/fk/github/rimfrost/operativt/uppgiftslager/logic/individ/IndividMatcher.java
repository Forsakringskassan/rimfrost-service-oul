package se.fk.github.rimfrost.operativt.uppgiftslager.logic.individ;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import java.util.Optional;
import java.util.UUID;

/**
 * Checks whether a given individ appears in a handläggning's yrkande roles.
 */
public interface IndividMatcher
{
   /**
    * Returns the {@code yrkandeRollId} of the first roll in the given handläggning's yrkande
    * that matches {@code individ}, or empty if the individ does not appear in any roll.
    * Throws {@link se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.HandlaggningReadException}
    * if the handläggning cannot be read.
    *
    * @param handlaggningId the handläggning to inspect
    * @param uppgiftId      the uppgift the handläggning belongs to (used for exception context)
    * @param individ        the individ identity to search for
    * @return the matched yrkanderoll ID, or empty
    */
   Optional<String> findYrkanderoll(UUID handlaggningId, UUID uppgiftId, Idtyp individ);
}
