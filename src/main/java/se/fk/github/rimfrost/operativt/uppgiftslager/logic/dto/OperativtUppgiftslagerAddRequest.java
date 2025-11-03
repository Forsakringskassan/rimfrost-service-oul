package se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto;

import java.util.UUID;

import org.immutables.value.Value;

@Value.Immutable
public interface OperativtUppgiftslagerAddRequest {
    
   UUID processId();

   String personNummer();

   String uppgift();

}
