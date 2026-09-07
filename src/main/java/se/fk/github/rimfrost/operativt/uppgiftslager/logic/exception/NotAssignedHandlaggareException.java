package se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception;

import java.util.UUID;

/**
 * Thrown when an ownership check fails for an operation that requires the
 * calling handläggare to be the same as the one assigned to an uppgift.
 */
public class NotAssignedHandlaggareException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   /**
    * @param uppgiftId the uppgift the caller attempted to perform the operation on
    */
   public NotAssignedHandlaggareException(UUID uppgiftId)
   {
      super("Handläggare is not currently assigned to uppgift " + uppgiftId);
   }
}
