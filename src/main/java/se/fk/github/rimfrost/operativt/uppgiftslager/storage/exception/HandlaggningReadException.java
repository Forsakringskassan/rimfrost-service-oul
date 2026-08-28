package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

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
