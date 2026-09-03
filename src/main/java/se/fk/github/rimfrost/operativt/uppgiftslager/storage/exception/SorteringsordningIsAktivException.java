package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

/**
 * Thrown when an attempt is made to delete a sorteringsordning that is currently
 * designated as aktiv. The caller should use HTTP 409 Conflict.
 */
public class SorteringsordningIsAktivException extends RuntimeException
{
   /**
    * @param id the UUID of the sorteringsordning that is protected as aktiv
    */
   public SorteringsordningIsAktivException(UUID id)
   {
      super("Sorteringsordning is currently aktiv and cannot be deleted: " + id);
   }
}
