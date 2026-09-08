package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.Objects;

public record UppgiftAssignBlocklistId(@SuppressFBWarnings("EI_EXPOSE_REP")UppgiftEntity uppgift,String handlaggareIdTypId,String handlaggareIdVarde)implements Serializable{@Override public boolean equals(Object o){if(this==o)return true;if(!(o instanceof UppgiftAssignBlocklistId(UppgiftEntity otherUppgift,String otherIdTypId,String otherIdVarde)))return false;var uppgiftId=(uppgift)!=null?uppgift.getId():null;var otherUppgiftId=(otherUppgift)!=null?otherUppgift.getId():null;return Objects.equals(uppgiftId,otherUppgiftId)&&Objects.equals(handlaggareIdTypId,otherIdTypId)&&Objects.equals(handlaggareIdVarde,otherIdVarde);}

@Override public int hashCode(){var uppgiftId=(uppgift)!=null?uppgift.getId():null;return Objects.hashCode(uppgiftId)+Objects.hashCode(handlaggareIdTypId)+Objects.hashCode(handlaggareIdVarde);}}
