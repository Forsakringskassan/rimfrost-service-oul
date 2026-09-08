package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;

public record UppgiftAssignBlocklistId(@SuppressFBWarnings("EI_EXPOSE_REP")UppgiftEntity uppgift,String handlaggareIdTypId,String handlaggareIdVarde)implements Serializable{}
