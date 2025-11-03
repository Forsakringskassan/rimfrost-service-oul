package se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.dto.OperativtUppgiftslagerRequest;

public class OperativtUppgiftslagerRequestDeserializer extends ObjectMapperDeserializer<OperativtUppgiftslagerRequest>
{
   public OperativtUppgiftslagerRequestDeserializer()
   {
      super(OperativtUppgiftslagerRequest.class);
   }
}
