package se.fk.github.rimfrost.operativt.uppgiftslager.util;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.Status;

@ApplicationScoped
public class EnumMapper
{
   public Status mapUppgiftStatusToStatus(UppgiftStatus status)
   {
      return switch (status)
      {
         case NY -> Status.NY;
         case TILLDELAD -> Status.TILLDELAD;
         case AVSLUTAD -> Status.AVSLUTAD;
         case AVBRUTEN -> Status.AVBRUTEN;
      };
   }

   public UppgiftStatus mapStatusToUppgiftStatus(Status status)
   {
      return switch (status)
      {
         case NY -> UppgiftStatus.NY;
         case TILLDELAD -> UppgiftStatus.TILLDELAD;
         case AVSLUTAD -> UppgiftStatus.AVSLUTAD;
         case AVBRUTEN -> UppgiftStatus.AVBRUTEN;
      };
   }
}
