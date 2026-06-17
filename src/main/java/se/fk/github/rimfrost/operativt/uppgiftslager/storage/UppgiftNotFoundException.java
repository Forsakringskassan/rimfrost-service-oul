package se.fk.github.rimfrost.operativt.uppgiftslager.storage;

import java.util.UUID;

/**
 * Thrown when an uppgift with a given UUID does not exist in storage.
 * The caller should use HTTP 404 Not Found.
 */
public class UppgiftNotFoundException extends RuntimeException
{
   /**
    * @param id the UUID of the uppgift that was not found
    */
   public UppgiftNotFoundException(UUID id)
   {
      super("Uppgift not found: " + id);
   }
}
