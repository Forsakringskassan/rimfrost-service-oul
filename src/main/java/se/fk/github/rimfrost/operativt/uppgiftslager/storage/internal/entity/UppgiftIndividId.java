package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import java.io.Serializable;
import java.util.UUID;

public record UppgiftIndividId(UUID uppgiftId,String typId,String varde)implements Serializable{}
