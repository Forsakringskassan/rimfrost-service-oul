package se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.rimfrost.OperativtUppgiftslagerStatusMessage;

@ApplicationScoped
public class OperativtUppgiftslagerProducer
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerProducer.class);

   @ConfigProperty(name = "kafka.oul_status_notification_topic_base")
   String oulStatusNotificationTopicBase;

   @Channel("operativt-uppgiftslager-status-notification")
   Emitter<OperativtUppgiftslagerStatusMessage> statusUpdateEmitter;

   public void publishTaskStatusUpdate(OperativtUppgiftslagerStatusMessage statusMessage, String subTopic)
   {
      var topic = oulStatusNotificationTopicBase + subTopic;

      var metadata = OutgoingKafkaRecordMetadata.builder()
            .withTopic(topic)
            .build();

      var message = Message.of(statusMessage).addMetadata(metadata);

      log.info("Publishing task StatusUpdate to topic '{}': {}", topic, statusMessage);
      statusUpdateEmitter.send(message);
   }
}
