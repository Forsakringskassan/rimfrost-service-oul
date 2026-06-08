package se.fk.github.rimfrost.operativt.uppgiftslager.storage;

import java.util.UUID;

/**
 * Thrown when a sorteringsordning with a given UUID does not exist in storage.
 * The caller should use HTTP 404 Not Found.
 */
public class SorteringsordningNotFoundException extends RuntimeException
{
   /**
    * @param id the UUID of the sorteringsordning that was not found
    */
   public SorteringsordningNotFoundException(UUID id)
   {
      super("Sorteringsordning not found: " + id);
   }
}
