package se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity;

import java.time.LocalDate;
import java.util.UUID;
import org.immutables.value.Value;
import jakarta.annotation.Nullable;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@Value.Immutable
public interface UppgiftEntity
{
   UUID handlaggningId();

   UUID uppgiftId();

   @Nullable
   Idtyp handlaggarId();

   LocalDate skapad();

   @Nullable
   LocalDate planeradTill();

   @Nullable
   LocalDate utford();

   UppgiftStatus status();

   String regel();

   Idtyp[] individer();

   String beskrivning();

   String verksamhetslogik();

   String roll();

   String url();

   String subTopic();
}
