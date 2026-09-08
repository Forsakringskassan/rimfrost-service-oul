package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import java.io.Serializable;
import java.util.UUID;

public record UppgiftAssignBlocklistId(UUID uppgiftId,String handlaggareIdTypId,String handlaggareIdVarde)implements Serializable{}
