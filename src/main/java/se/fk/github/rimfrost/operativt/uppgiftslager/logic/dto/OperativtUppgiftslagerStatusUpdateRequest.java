package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.util.UUID;

import org.immutables.value.Value;

import jakarta.annotation.Nullable;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@Value.Immutable
public interface OperativtUppgiftslagerStatusUpdateRequest
{

   UUID uppgiftId();

   @Nullable
   UUID utforarId();

   UppgiftStatus status();
}
