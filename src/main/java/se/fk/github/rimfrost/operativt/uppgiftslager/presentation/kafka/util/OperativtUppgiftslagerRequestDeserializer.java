package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.OperativtUppgiftslagerRequestMessage;

public class OperativtUppgiftslagerRequestDeserializer
      extends ObjectMapperDeserializer<OperativtUppgiftslagerRequestMessage>
{
   public OperativtUppgiftslagerRequestDeserializer()
   {
      super(OperativtUppgiftslagerRequestMessage.class);
   }
}
