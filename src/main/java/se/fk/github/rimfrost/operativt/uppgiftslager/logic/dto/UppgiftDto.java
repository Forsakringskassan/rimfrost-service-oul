package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@Value.Immutable
public interface UppgiftDto
{

   UUID kundbehovsflodeId();

   UUID uppgiftId();

   @Nullable
   UUID handlaggarId();

   LocalDate skapad();

   @Nullable
   LocalDate planeradTill();

   @Nullable
   LocalDate utford();

   UppgiftStatus status();

   String regelTyp();

}
