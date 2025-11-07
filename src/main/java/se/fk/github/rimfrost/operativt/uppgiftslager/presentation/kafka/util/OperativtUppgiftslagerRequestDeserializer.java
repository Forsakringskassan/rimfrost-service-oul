package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessagePayload;

public class OperativtUppgiftslagerRequestDeserializer
      extends ObjectMapperDeserializer<OperativtUppgiftslagerRequestMessagePayload>
{
   public OperativtUppgiftslagerRequestDeserializer()
   {
      super(OperativtUppgiftslagerRequestMessagePayload.class);
   }
}
