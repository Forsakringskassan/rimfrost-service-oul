package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface Idtyp
{
   String typId();

   String varde();
}
