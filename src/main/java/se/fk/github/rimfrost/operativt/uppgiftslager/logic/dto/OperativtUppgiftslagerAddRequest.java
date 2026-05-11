package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.util.UUID;
import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerAddRequest
{

   String version();

   Idtyp[] individer();

   UUID handlaggningId();

   String regel();

   String beskrivning();

   String verksamhetslogik();

   String roll();

   String url();

}
