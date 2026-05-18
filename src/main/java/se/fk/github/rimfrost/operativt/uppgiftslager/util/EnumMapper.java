package se.fk.github.rimfrost.operativt.uppgiftslager.util;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@ApplicationScoped
public class EnumMapper
{
   public String mapUppgiftStatusToStatus(UppgiftStatus status)
   {
      return switch (status)
      {
         case NY -> "NY";
         case TILLDELAD -> "TILLDELAD";
         case AVSLUTAD -> "AVSLUTAD";
         case AVBRUTEN -> "AVBRUTEN";
      };
   }

   public UppgiftStatus mapStatusToUppgiftStatus(String status)
   {
      return switch(status){case"NY"->UppgiftStatus.NY;case"TILLDELAD"->UppgiftStatus.TILLDELAD;case"AVSLUTAD"->UppgiftStatus.AVSLUTAD;case"AVBRUTEN"->UppgiftStatus.AVBRUTEN;default->throw new IllegalArgumentException("Unsupported status value: "+status);};
   }
}
