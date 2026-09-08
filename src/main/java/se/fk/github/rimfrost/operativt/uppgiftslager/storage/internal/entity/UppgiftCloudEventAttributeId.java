package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.Objects;

public record UppgiftCloudEventAttributeId(@SuppressFBWarnings("EI_EXPOSE_REP")UppgiftEntity uppgift,String cloudEventAttributeKey)implements Serializable{@Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof UppgiftCloudEventAttributeId(UppgiftEntity otherUppgift,String otherCloudEventAttributeKey)))return false;var uppgiftId=(uppgift)!=null?uppgift.getId():null;var otherUppgiftId=(otherUppgift)!=null?otherUppgift.getId():null;return Objects.equals(uppgiftId,otherUppgiftId)&&Objects.equals(cloudEventAttributeKey,otherCloudEventAttributeKey);}

@Override public int hashCode(){var uppgiftId=(uppgift)!=null?uppgift.getId():null;return Objects.hashCode(uppgiftId)+Objects.hashCode(cloudEventAttributeKey);}}
