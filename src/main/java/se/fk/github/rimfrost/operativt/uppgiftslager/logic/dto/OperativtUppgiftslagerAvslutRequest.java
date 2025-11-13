package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import org.immutables.value.Value;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@Value.Immutable
public interface OperativtUppgiftslagerAvslutRequest
{

   String uppgiftId();

   String processId();

   String personnummer();

   String resultat();

   UppgiftStatus status();
}
