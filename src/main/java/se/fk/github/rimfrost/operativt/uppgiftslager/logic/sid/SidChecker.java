package se.fk.github.rimfrost.operativt.uppgiftslager.logic.sid;

import java.util.UUID;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.HandlaggningReadException;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SidStatusException;

/**
 * Determines whether the handläggning behind an uppgift is SID-märkt (involves an individ with
 * skyddad identitet). Reads from the handläggning and SID adapters — not storage-specific, so
 * this does not belong in {@link se.fk.github.rimfrost.operativt.uppgiftslager.storage.OulDataStorage}
 * even though it was originally implemented there (the only caller at the time).
 */
public interface SidChecker
{
   /**
    * Returns whether the handläggning behind an uppgift is SID-märkt.
    *
    * @param handlaggningId the id of the handläggning to check — determines the result
    * @param uppgiftId      the id of the uppgift {@code handlaggningId} belongs to; not used to
    *                       compute the result, only to identify the uppgift in logs/exceptions
    *                       if the read fails, so a caller must pass the matching pair
    * @return whether the handläggning is SID-märkt
    * @throws HandlaggningReadException if the handläggning could not be read
    * @throws SidStatusException        if the SID status could not be determined
    */
   boolean containsSid(UUID handlaggningId, UUID uppgiftId);
}
