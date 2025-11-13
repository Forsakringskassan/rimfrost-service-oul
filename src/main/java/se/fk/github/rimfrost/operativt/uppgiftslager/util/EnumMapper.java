package se.fk.github.rimfrost.operativt.uppgiftslager.util;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.rimfrost.Status;

@ApplicationScoped
public class EnumMapper
{
   public Status mapUppgiftStatusToStatus(UppgiftStatus status)
   {
      return switch(status){case UppgiftStatus.NY->Status.NY;case UppgiftStatus.TILLDELAD->Status.TILLDELAD;default->Status.AVSLUTAD;};
   }

   public UppgiftStatus mapStatusToUppgiftStatus(Status status)
    {
        return switch (status){
            case NY -> UppgiftStatus.NY;
            case TILLDELAD -> UppgiftStatus.TILLDELAD;
            default -> UppgiftStatus.AVSLUTAD;
        };
    }
}
