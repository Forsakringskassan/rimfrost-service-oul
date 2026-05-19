package se.fk.github.rimfrost.operativt.uppgiftslager;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.reactive.messaging.Message;
import static org.awaitility.Awaitility.await;

/**
 * Test utility for interacting with Kafka channels backed by {@link InMemoryConnector}.
 *
 * <p>
 *     This helper simplifies sending messages to input channels, waiting for emitted messages on
 * output channels, and clearing collected messages between test cases.</p>
 *
 * <p>Primarily intended for integration and component tests using SmallRye Reactive Messaging with
 * in-memory connectors.</p>
 */
@SuppressWarnings("unused")
public class KafkaConnector
{

   /**
    * In-memory messaging connector used to simulate Kafka channels.
    */
   protected final InMemoryConnector inMemoryConnector;

   /**
    * Creates a new {@code KafkaConnector} using the provided in-memory connector.
    *
    * @param inMemoryConnector connector managing in-memory message sources and sinks
    */
   public KafkaConnector(InMemoryConnector inMemoryConnector)
   {
      if (inMemoryConnector == null)
      {
         throw new IllegalStateException(
               "InMemoryConnector is null. Ensure Quarkus test is fully initialized before creating KafkaConnector.");
      }
      this.inMemoryConnector = inMemoryConnector;
   }

   /**
    * Waits until at least one message has been received on the specified sink channel.
    *
    * <p>The method polls for up to 5 seconds before timing out.</p>
    *
    * @param channel name of the sink channel to inspect
    * @return list of received messages for the channel
    */
   public List<? extends Message<?>> waitForMessages(String channel)
   {
      await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> !inMemoryConnector.sink(channel).received().isEmpty());
      return inMemoryConnector.sink(channel).received();
   }

   /**
    * Clears all received messages for the specified sink channel.
    *
    * <p>Useful for resetting test state between assertions.</p>
    *
    * @param channel name of the sink channel to clear
    */
   public void clearChannel(String channel)
   {
      inMemoryConnector.sink(channel).clear();
   }

   /**
    * Sends a payload to the specified source channel.
    *
    * <p>The payload is forwarded through the in-memory connector as if produced to Kafka.</p>
    *
    * @param channel name of the source channel
    * @param payload message payload to send
    */
   public void sendMessage(String channel, Object payload)
   {
      inMemoryConnector.source(channel).send(payload);
   }
}
