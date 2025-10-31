package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;

public class UppgiftRestDTO 
{

  @JsonProperty("uppgift_id")
  @NotBlank
  private String uppgiftId; // UUID as string

  @JsonProperty("personnummer")
  @NotBlank
  // Simple Swedish SSN pattern; adjust/replace with your validator if needed
  @Pattern(regexp = "^(\\d{6}|\\d{8})[-+]\\d{4}$")
  private String personnummer;

  @JsonProperty("status")
  @NotBlank
  private String status; // e.g., "Ny", "Tilldelad", "Avslutad"

  @JsonProperty("beskrivning")
  private String beskrivning;

  @JsonProperty("skapad")
  @NotNull
  private Instant skapad;

  @JsonProperty("andrad")
  private Instant andrad;

  @JsonProperty("handlaggar_id")
  private String handlaggarId;

  // Getters/setters
  public String getUppgiftId() { return uppgiftId; }
  public void setUppgiftId(String uppgiftId) { this.uppgiftId = uppgiftId; }

  public String getPersonnummer() { return personnummer; }
  public void setPersonnummer(String personnummer) { this.personnummer = personnummer; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public String getBeskrivning() { return beskrivning; }
  public void setBeskrivning(String beskrivning) { this.beskrivning = beskrivning; }

  public Instant getSkapad() { return skapad; }
  public void setSkapad(Instant skapad) { this.skapad = skapad; }

  public Instant getAndrad() { return andrad; }
  public void setAndrad(Instant andrad) { this.andrad = andrad; }

  public String getHandlaggarId() { return handlaggarId; }
  public void setHandlaggarId(String handlaggarId) { this.handlaggarId = handlaggarId; }
}