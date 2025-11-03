package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerUpdateResponse
{
   Long uppgiftId();

   String status();

   String beskrivning();

   String handlaggarId();
}
