package se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerResponse
{
   UUID processId();

   String personNummer();

   // TODO: Tänk ut hur denna ska fungera, måste vara dynamisk på något sätt
   String resultat();

   // TODO: Onekligen onödig men hänger med från början
   String status();
}
