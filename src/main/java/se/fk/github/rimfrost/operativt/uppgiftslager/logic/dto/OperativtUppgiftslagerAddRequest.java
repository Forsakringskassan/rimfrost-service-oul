package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.util.UUID;
import org.immutables.value.Value;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface OperativtUppgiftslagerAddRequest
{

   String version();

   UUID handlaggningId();

   String regel();

   String beskrivning();

   String verksamhetslogik();

   String roll();

   String url();

   Erbjudande erbjudande();

   @Nullable
   UUID aktivitetId();

   @Nullable
   String processvyUrl();
}
