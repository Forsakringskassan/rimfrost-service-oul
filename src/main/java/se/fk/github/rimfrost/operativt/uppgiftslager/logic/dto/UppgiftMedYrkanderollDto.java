package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

/**
 * Pairs an uppgift with the yrkanderoll that connects it to a specific individ.
 * Used as the service-layer result of {@code getUppgifterForIndivid} (OUL-FR-21).
 *
 * @param uppgift     the operative task
 * @param yrkandeRoll the ID of the yrkanderoll that matched the searched individ
 */
public record UppgiftMedYrkanderollDto(UppgiftDto uppgift,String yrkandeRoll){}
