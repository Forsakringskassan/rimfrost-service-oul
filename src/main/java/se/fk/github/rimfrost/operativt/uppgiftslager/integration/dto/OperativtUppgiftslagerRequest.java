package se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerRequest
{
   UUID processId();

   String personNummer();

   // TODO: Enum för uppgiftstyper eller liknande, identitet?
   // Beskriver vad uppgiften går ut på
   String uppgift();

}
