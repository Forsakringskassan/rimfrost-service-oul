package se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception;

import java.util.UUID;

/**
 * Thrown when ommarkering would give a SID-märkt uppgift to a handläggare without
 * SID-behörighet.
 */
public class SidNotAuthorizedException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   /**
    * @param uppgiftId the SID-märkt uppgift the caller lacks SID-behörighet for
    */
   public SidNotAuthorizedException(UUID uppgiftId)
   {
      super("Handläggare lacks SID-behörighet for uppgift " + uppgiftId);
   }
}
