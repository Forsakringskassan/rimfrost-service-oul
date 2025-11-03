package se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerNotification
{
   UUID processId();

   String personNummer();

   String uppgift();

   boolean resultat();

   String status();
}
