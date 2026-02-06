package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.util.UUID;
import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerAddRequest
{

   String version();

   String kundbehov();

   UUID kundbehovsflodeId();

   String regel();

   String beskrivning();

   String verksamhetslogik();

   String roll();

   String url();

   String subTopic();

}
