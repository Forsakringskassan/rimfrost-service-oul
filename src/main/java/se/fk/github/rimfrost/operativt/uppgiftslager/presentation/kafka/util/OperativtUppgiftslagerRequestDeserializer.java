package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.dto.OperativtUppgiftslagerRequest;

public class OperativtUppgiftslagerRequestDeserializer extends ObjectMapperDeserializer<OperativtUppgiftslagerRequest>
{
   public OperativtUppgiftslagerRequestDeserializer()
   {
      super(OperativtUppgiftslagerRequest.class);
   }
}
