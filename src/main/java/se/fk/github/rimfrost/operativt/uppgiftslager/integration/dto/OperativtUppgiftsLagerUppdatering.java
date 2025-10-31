package se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftsLagerUppdatering 
{
    UUID processId();

    // TODO: Räcker processId för att koppla till rätt uppgift?
    String personNummer();

    // TODO: Tänk igenom om denna verkligen behövs, eller om status räcker
    String uppgift();

    String status();
}
