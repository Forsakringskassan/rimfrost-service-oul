package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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

   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "uppgift_id", nullable = false)
   @MapsId("uppgiftId")
   private UppgiftEntity uppgift;

   @SuppressFBWarnings("EI_EXPOSE_REP")
   public UppgiftEntity getUppgift()
   {
      return uppgift;
   }

   @SuppressFBWarnings("EI_EXPOSE_REP2")
   public void setUppgift(UppgiftEntity uppgift)
   {
      this.uppgift = uppgift;
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
