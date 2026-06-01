package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@IdClass(UppgiftCloudEventAttributeId.class)
@Table(name = "uppgift_cloud_event_attribute")
public class UppgiftCloudEventAttributeEntity
{
   @Id
   @Column(name = "uppgift_id", nullable = false)
   private UUID uppgiftId;

   @Id
   @Column(name = "cloud_event_attribute_key", nullable = false)
   private String cloudEventAttributeKey;

   @Column(name = "cloud_event_attribute_value")
   private String cloudEventAttributeValue;

   public UUID getUppgiftId()
   {
      return uppgiftId;
   }

   public void setUppgiftId(UUID uppgiftId)
   {
      this.uppgiftId = uppgiftId;
   }

   public String getCloudEventAttributeKey()
   {
      return cloudEventAttributeKey;
   }

   public void setCloudEventAttributeKey(String value)
   {
      this.cloudEventAttributeKey = value;
   }

   public String getCloudEventAttributeValue()
   {
      return cloudEventAttributeValue;
   }

   public void setCloudEventAttributeValue(String value)
   {
      this.cloudEventAttributeValue = value;
   }
}
