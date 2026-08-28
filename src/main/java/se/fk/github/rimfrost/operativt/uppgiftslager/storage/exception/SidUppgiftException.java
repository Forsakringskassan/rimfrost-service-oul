package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

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
