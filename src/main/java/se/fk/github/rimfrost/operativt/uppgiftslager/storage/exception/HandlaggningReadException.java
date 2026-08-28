package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

public class HandlaggningReadException extends RuntimeException
{
   private final UUID uppgiftsId;

   public HandlaggningReadException(UUID uppgiftId, Throwable cause)
   {
      super(cause);

      this.uppgiftsId = uppgiftId;
   }

   public UUID getUppgiftsId()
   {
      return uppgiftsId;
   }
}
