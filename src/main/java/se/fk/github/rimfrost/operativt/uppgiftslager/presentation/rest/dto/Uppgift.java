package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.dto;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonProperty;

@Value.Immutable
public interface Uppgift
{
   @JsonProperty("uppgift_id")
   String uppgiftId();

   @JsonProperty("status")
   String status();

   @JsonProperty("beskrivning")
   String beskrivning();

   @JsonProperty("handlaggar_id")
   String handlaggarId();
}
