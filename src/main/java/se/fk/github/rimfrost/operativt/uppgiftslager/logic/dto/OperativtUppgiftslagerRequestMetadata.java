package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerRequestMetadata
{

   String specversion();

   UUID id();

   String source();

   String type();

   OffsetDateTime time();

   UUID kogitoparentprociid();

   String kogitorootprocid();

   String kogitoproctype();

   UUID kogitoprocinstanceid();

   String kogitoprocist();

   String kogitoprocversion();

   UUID kogitorootprociid();

   String kogitoprocid();

}
