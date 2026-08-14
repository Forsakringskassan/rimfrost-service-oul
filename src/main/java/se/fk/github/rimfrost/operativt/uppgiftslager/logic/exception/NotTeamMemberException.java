package se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception;

import java.util.UUID;

/**
 * Thrown when a team membership check fails — either because the caller does not
 * belong to any known team (OUL-FR-17.4), or because the current assignee of an
 * uppgift is not a member of the caller's team (OUL-FR-18.3 / AC7).
 */
public class NotTeamMemberException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   /**
    * Used when the caller does not belong to any known team.
    */
   public NotTeamMemberException()
   {
      super("Caller does not belong to any known team");
   }

   /**
    * @param uppgiftId the uppgift whose current assignee is not a team member
    */
   public NotTeamMemberException(UUID uppgiftId)
   {
      super("Current assignee of uppgift " + uppgiftId + " is not a team member");
   }
}
