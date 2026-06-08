package se.fk.github.rimfrost.operativt.uppgiftslager.storage;

import java.util.UUID;

/**
 * Thrown when an attempt is made to delete a sorteringsordning that is currently
 * designated as the default. The caller should use HTTP 409 Conflict.
 */
public class SorteringsordningIsDefaultException extends RuntimeException
{
   /**
    * @param id the UUID of the sorteringsordning that is protected as the default
    */
   public SorteringsordningIsDefaultException(UUID id)
   {
      super("Sorteringsordning is the current default and cannot be deleted: " + id);
   }
}
