package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableErbjudande;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.ImmutableUppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.entity.UppgiftEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftCloudEventAttributeEntity;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftIndividEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class OulDataStorageMapper
{
   public se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity toUppgiftEntity(
         UppgiftEntity uppgift)
   {
      var entity = new se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity();
      entity.setId(uppgift.uppgiftId());
      entity.setHandlaggningId(uppgift.handlaggningId());
      entity.setSkapad(uppgift.skapad());
      entity.setPlaneradTill(uppgift.planeradTill());
      entity.setUtford(uppgift.utford());
      entity.setStatus(uppgift.status());
      entity.setRegel(uppgift.regel());
      entity.setBeskrivning(uppgift.beskrivning());
      entity.setVerksamhetslogik(uppgift.verksamhetslogik());
      entity.setRoll(uppgift.roll());
      entity.setUrl(uppgift.url());
      entity.setSubTopic(uppgift.subTopic());
      entity.setErbjudandeId(uppgift.erbjudande().id());
      entity.setErbjudandeNamn(uppgift.erbjudande().namn());
      entity.setReason(uppgift.reason());
      entity.setIndivider(Arrays.stream(uppgift.individer()).map(idtyp -> toIndividEntity(uppgift.uppgiftId(), idtyp)).toList());
      entity.setCloudEventAttributes(uppgift.cloudeventAttributes().entrySet().stream()
            .map(e -> toCloudEventAttributeEntity(uppgift.uppgiftId(), e.getKey(), e.getValue())).toList());

      var handlaggareId = uppgift.handlaggarId();
      if (handlaggareId != null)
      {
         entity.setHandlaggarIdTypId(handlaggareId.typId());
         entity.setHandlaggarIdVarde(handlaggareId.varde());
      }

      return entity;
   }

   @SuppressFBWarnings(value = "NP_NULL_PARAM_DEREF", justification = "False positive for ImmutableIdtyp and handlaggarId values")
   public UppgiftEntity toUppgiftEntity(
         se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.UppgiftEntity entity)
   {
      Idtyp handlaggarId = null;

      var handlaggarIdTypId = entity.getHandlaggarIdTypId();
      var handlaggarIdVarde = entity.getHandlaggarIdVarde();

      if (handlaggarIdTypId != null && handlaggarIdVarde != null)
      {
         handlaggarId = ImmutableIdtyp.builder()
               .typId(handlaggarIdTypId)
               .varde(handlaggarIdVarde)
               .build();
      }

      var erbjudande = ImmutableErbjudande.builder()
            .id(entity.getErbjudandeId())
            .namn(entity.getErbjudandeNamn())
            .build();

      return ImmutableUppgiftEntity.builder()
            .uppgiftId(entity.getId())
            .handlaggningId(entity.getHandlaggningId())
            .handlaggarId(handlaggarId)
            .skapad(entity.getSkapad())
            .planeradTill(entity.getPlaneradTill())
            .utford(entity.getUtford())
            .status(entity.getStatus())
            .regel(entity.getRegel())
            .beskrivning(entity.getBeskrivning())
            .verksamhetslogik(entity.getVerksamhetslogik())
            .roll(entity.getRoll())
            .url(entity.getUrl())
            .subTopic(entity.getSubTopic())
            .erbjudande(erbjudande)
            .reason(entity.getReason())
            .individer(entity.getIndivider().stream().map(this::toIdtyp).toArray(Idtyp[]::new))
            .cloudeventAttributes(toCloudEventAttributesMap(entity.getCloudEventAttributes()))
            .build();
   }

   private UppgiftIndividEntity toIndividEntity(UUID uppgiftId, Idtyp individ)
   {
      var entity = new UppgiftIndividEntity();
      entity.setUppgiftId(uppgiftId);
      entity.setTypId(individ.typId());
      entity.setVarde(individ.varde());
      return entity;
   }

   private Idtyp toIdtyp(UppgiftIndividEntity entity)
   {
      return ImmutableIdtyp.builder()
            .typId(entity.getTypId())
            .varde(entity.getVarde())
            .build();
   }

   private UppgiftCloudEventAttributeEntity toCloudEventAttributeEntity(UUID uppgiftId, String key, String value)
   {
      var entity = new UppgiftCloudEventAttributeEntity();
      entity.setUppgiftId(uppgiftId);
      entity.setCloudEventAttributeKey(key);
      entity.setCloudEventAttributeValue(value);
      return entity;
   }

   private Map<String, String> toCloudEventAttributesMap(List<UppgiftCloudEventAttributeEntity> entities)
   {
      HashMap<String, String> cloudEventAttributes = new HashMap<>();
      for (var entity : entities)
      {
         cloudEventAttributes.put(entity.getCloudEventAttributeKey(), entity.getCloudEventAttributeValue());
      }

      return cloudEventAttributes;
   }
}
