package se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;

public record SorteringsordningEntity(UUID id,OffsetDateTime skapad,List<SorteringsordningEntry>entries){}
