package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

@Value.Immutable
public interface UppgiftGetResponse
{
   @JsonProperty("uppgift")
   Uppgift uppgift();
}
