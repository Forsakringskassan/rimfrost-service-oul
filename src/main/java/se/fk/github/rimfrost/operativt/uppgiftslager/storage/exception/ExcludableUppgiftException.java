package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

/**
 * Implemented by exceptions that identify a single uppgift to exclude from further
 * consideration during assignment, so callers can catch several of them together.
 */
public interface ExcludableUppgiftException
{
   UUID getUppgiftsId();
}
