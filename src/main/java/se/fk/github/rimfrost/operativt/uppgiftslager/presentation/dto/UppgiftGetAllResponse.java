package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto;

import java.util.Collection;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

@Value.Immutable
public interface UppgiftGetAllResponse
{
   @JsonProperty("uppgifter")
   Collection<Uppgift> uppgifter();
}
