package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

public class UppgiftNotAssignedException extends RuntimeException
{
   public UppgiftNotAssignedException(UUID uppgiftId)
   {
      super("Uppgift " + uppgiftId + " is not assigned to handläggare");
   }
}
