package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

 /**                                                                                                                                                                                                                                                                                                          
   * Thrown when the handläggning for an uppgift cannot be read, allowing the caller
   * to exclude the uppgift and continue assignment (see {@link ExcludableUppgiftException}).                                                                                                                                                                                                                  
   */    
public class HandlaggningReadException extends RuntimeException implements ExcludableUppgiftException
{
   private final UUID uppgiftsId;

   public HandlaggningReadException(UUID uppgiftId, Throwable cause)
   {
      super(cause);

      this.uppgiftsId = uppgiftId;
   }

   @Override
   public UUID getUppgiftsId()
   {
      return uppgiftsId;
   }
}
