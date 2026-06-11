package se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.immutables.value.Value;
import jakarta.annotation.Nullable;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Erbjudande;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

@Value.Immutable
public interface UppgiftEntity
{
   UUID handlaggningId();

   UUID uppgiftId();

   @Nullable
   Idtyp handlaggarId();

   LocalDate skapad();

   @Nullable
   LocalDate planeradTill();

   @Nullable
   LocalDate utford();

   UppgiftStatus status();

   String regel();

   Idtyp[] individer();

   String beskrivning();

   String verksamhetslogik();

   String roll();

   String url();

   String subTopic();

   /**
    * The reply topic supplied by the caller at creation time.
    * <p>
    * This value is opaque to the OUL service — it is stored as-is and echoed
    * back in REST responses and Kafka status-update message payloads so that
    * the caller knows which topic to listen on for replies.
    * <p>
    * Distinct from {@link #subTopic()}, which controls Kafka routing within
    * the OUL service (i.e. which topic status events are published to).
    */
   String replyTopic();

   Map<String, String> cloudeventAttributes();

   Erbjudande erbjudande();

   @Nullable
   String reason();
}
