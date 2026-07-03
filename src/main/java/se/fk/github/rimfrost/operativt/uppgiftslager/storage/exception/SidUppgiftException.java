package se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception;

import java.util.UUID;

public class SidUppgiftException extends RuntimeException
{
   private final UUID uppgiftsId;

   public SidUppgiftException(UUID uppgiftId)
   {
      super();

      this.uppgiftsId = uppgiftId;
   }

   public UUID getUppgiftsId()
   {
      return uppgiftsId;
   }
}
