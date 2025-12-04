package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.kafka.util;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

public class OperativtUppgiftslagerStatusControlDeserializer extends ObjectMapperDeserializer<OperativtUppgiftslagerStatusMessage> {
    public OperativtUppgiftslagerStatusControlDeserializer()
    {
        super(OperativtUppgiftslagerStatusMessage.class);
    }
}
