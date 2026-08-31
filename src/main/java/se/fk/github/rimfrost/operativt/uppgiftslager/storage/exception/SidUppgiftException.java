package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

/**                                                                                                                                                                                                                                                                                                          
   * Thrown when an uppgift is SID-marked and the handläggare lacks SID-behörighet,
   * allowing the caller to exclude the uppgift and continue assignment (see {@link ExcludableUppgiftException}).                                                                                                                                                                                              
   */    
public class SidUppgiftException extends RuntimeException implements ExcludableUppgiftException
{
   private final UUID uppgiftsId;

   public SidUppgiftException(UUID uppgiftId)
   {
      super();

      this.uppgiftsId = uppgiftId;
   }

   @Override
   public UUID getUppgiftsId()
   {
      return uppgiftsId;
   }
}
