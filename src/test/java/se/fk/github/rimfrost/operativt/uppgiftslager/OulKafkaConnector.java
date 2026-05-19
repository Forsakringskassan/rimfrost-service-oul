package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

public class OulKafkaConnector extends KafkaConnector
{
   public static final String oulStatusNotificationChannel = "operativt-uppgiftslager-status-notification";

   public OulKafkaConnector(InMemoryConnector connector)
   {
      super(connector);
   }

   public void clear()
   {
      inMemoryConnector.sink(oulStatusNotificationChannel).clear();
   }

   public OperativtUppgiftslagerStatusMessage waitForOulStatusMessage()
   {
      return (OperativtUppgiftslagerStatusMessage) waitForMessages(oulStatusNotificationChannel).getFirst().getPayload();
   }
}
