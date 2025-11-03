package se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity;

import java.util.UUID;

import org.immutables.value.Value;

import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@Value.Immutable
public interface UppgiftEntity {
    UUID processId();
    String personnummer();
    UppgiftStatus status(); 
    String beskrivning();
}