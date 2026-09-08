package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import java.io.Serializable;
import java.util.UUID;

public record UppgiftCloudEventAttributeId(UUID uppgiftId,String cloudEventAttributeKey)implements Serializable{}
