package se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.MutinyEmitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OperativtUppgiftslagerProducerTest
{
   private OperativtUppgiftslagerProducer producer;

   @SuppressWarnings("unchecked")
   @BeforeEach
   void setUp()
   {
      producer = new OperativtUppgiftslagerProducer();
      producer.oulStatusNotificationTopicBase = "operativt-uppgiftslager-status-notification.";
      producer.statusUpdateEmitter = mock(MutinyEmitter.class);
      producer.publishTimeout = Duration.ofMillis(200);
   }

   @Test
   @DisplayName("OUL-FR-06.5: Statusnotifiering som Kafka nekar ger exception")
   void should_throw_when_kafka_rejects_status_notification()
   {
      when(producer.statusUpdateEmitter.sendMessage(any()))
            .thenReturn(Uni.createFrom().failure(new RuntimeException("nack")));

      assertThrows(StatusNotificationException.class,
            () -> producer.publishTaskStatusUpdate(new OperativtUppgiftslagerStatusMessage(), "test"));
   }

   @Test
   @DisplayName("OUL-FR-06.6: Statusnotifiering som aldrig kvitteras ger exception efter konfigurerad tid")
   void should_throw_when_status_notification_is_not_acknowledged_in_time()
   {
      when(producer.statusUpdateEmitter.sendMessage(any())).thenReturn(Uni.createFrom().nothing());

      assertThrows(StatusNotificationException.class,
            () -> producer.publishTaskStatusUpdate(new OperativtUppgiftslagerStatusMessage(), "test"));
   }
}
