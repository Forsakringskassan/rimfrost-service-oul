package se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception;

import java.util.UUID;

/**
 * Thrown when a reassignment is attempted on an uppgift whose current assignee
 * is not a member of the caller's team (AC7).
 */
public class NotTeamMemberException extends RuntimeException
{
   /**
    * @param uppgiftId the uppgift whose current assignee is not a team member
    */
   public NotTeamMemberException(UUID uppgiftId)
   {
      super("Current assignee of uppgift " + uppgiftId + " is not a team member");
   }
}
