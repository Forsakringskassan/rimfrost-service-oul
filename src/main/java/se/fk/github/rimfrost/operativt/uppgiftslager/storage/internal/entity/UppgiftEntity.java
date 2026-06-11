package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SuppressFBWarnings("EI_EXPOSE_REP")
@Entity
@Table(name = "uppgift")
public class UppgiftEntity
{
   @Id
   @Column(nullable = false)
   private UUID id;

   @Version
   private long version;

   @Column(nullable = false, updatable = false)
   private Instant createdAt;

   @Column(nullable = false)
   private Instant updatedAt;

   @Column(nullable = false)
   private UUID handlaggningId;

   private String handlaggarIdTypId;

   private String handlaggarIdVarde;

   @Column(nullable = false)
   private LocalDate skapad;

   private LocalDate planeradTill;

   private LocalDate utford;

   @Column(nullable = false)
   @Enumerated(EnumType.STRING)
   private UppgiftStatus status;

   @Column(nullable = false)
   private String regel;

   @Column(nullable = false)
   private String beskrivning;

   @Column(nullable = false)
   private String verksamhetslogik;

   @Column(nullable = false)
   private String roll;

   @Column(nullable = false)
   private String url;

   @Column(nullable = false)
   private String subTopic;

   @Column(nullable = false)
   private String replyTopic;

   @Column(nullable = false)
   private String erbjudandeId;

   @Column(nullable = false)
   private String erbjudandeNamn;

   private String reason;

   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   @JoinColumn(name = "uppgift_id", nullable = false, insertable = false, updatable = false)
   private List<UppgiftIndividEntity> individer;

   @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
   @JoinColumn(name = "uppgift_id", nullable = false, insertable = false, updatable = false)
   private List<UppgiftCloudEventAttributeEntity> cloudEventAttributes;

   @PrePersist
   void onCreate()
   {
      createdAt = Instant.now();
      updatedAt = createdAt;
   }

   @PreUpdate
   void onUpdate()
   {
      updatedAt = Instant.now();
   }

   public UUID getId()
   {
      return id;
   }

   public void setId(UUID id)
   {
      this.id = id;
   }

   public UUID getHandlaggningId()
   {
      return handlaggningId;
   }

   public void setHandlaggningId(UUID handlaggningId)
   {
      this.handlaggningId = handlaggningId;
   }

   public String getHandlaggarIdTypId()
   {
      return handlaggarIdTypId;
   }

   public void setHandlaggarIdTypId(String handlaggarIdTypId)
   {
      this.handlaggarIdTypId = handlaggarIdTypId;
   }

   public String getHandlaggarIdVarde()
   {
      return handlaggarIdVarde;
   }

   public void setHandlaggarIdVarde(String handlaggarIdVarde)
   {
      this.handlaggarIdVarde = handlaggarIdVarde;
   }

   public LocalDate getSkapad()
   {
      return skapad;
   }

   public void setSkapad(LocalDate skapad)
   {
      this.skapad = skapad;
   }

   public LocalDate getPlaneradTill()
   {
      return planeradTill;
   }

   public void setPlaneradTill(LocalDate planeradTill)
   {
      this.planeradTill = planeradTill;
   }

   public LocalDate getUtford()
   {
      return utford;
   }

   public void setUtford(LocalDate utford)
   {
      this.utford = utford;
   }

   public UppgiftStatus getStatus()
   {
      return status;
   }

   public void setStatus(UppgiftStatus status)
   {
      this.status = status;
   }

   public String getRegel()
   {
      return regel;
   }

   public void setRegel(String regel)
   {
      this.regel = regel;
   }

   public String getBeskrivning()
   {
      return beskrivning;
   }

   public void setBeskrivning(String beskrivning)
   {
      this.beskrivning = beskrivning;
   }

   public String getVerksamhetslogik()
   {
      return verksamhetslogik;
   }

   public void setVerksamhetslogik(String verksamhetslogik)
   {
      this.verksamhetslogik = verksamhetslogik;
   }

   public String getRoll()
   {
      return roll;
   }

   public void setRoll(String roll)
   {
      this.roll = roll;
   }

   public String getUrl()
   {
      return url;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

   public String getSubTopic()
   {
      return subTopic;
   }

   public void setSubTopic(String subTopic)
   {
      this.subTopic = subTopic;
   }

   /**
    * @return the topic to which status-update events for this uppgift are published
    */
   public String getReplyTopic()
   {
      return replyTopic;
   }

   /**
    * @param replyTopic the topic to which status-update events for this uppgift are published
    */
   public void setReplyTopic(String replyTopic)
   {
      this.replyTopic = replyTopic;
   }

   public String getErbjudandeId()
   {
      return erbjudandeId;
   }

   public void setErbjudandeId(String erbjudandeId)
   {
      this.erbjudandeId = erbjudandeId;
   }

   public String getErbjudandeNamn()
   {
      return erbjudandeNamn;
   }

   public void setErbjudandeNamn(String erbjudandeNamn)
   {
      this.erbjudandeNamn = erbjudandeNamn;
   }

   public String getReason()
   {
      return reason;
   }

   public void setReason(String reason)
   {
      this.reason = reason;
   }

   public List<UppgiftIndividEntity> getIndivider()
   {
      return individer;
   }

   public void setIndivider(List<UppgiftIndividEntity> individer)
   {
      this.individer = individer;
   }

   public List<UppgiftCloudEventAttributeEntity> getCloudEventAttributes()
   {
      return cloudEventAttributes;
   }

   public void setCloudEventAttributes(List<UppgiftCloudEventAttributeEntity> cloudEventAttributes)
   {
      this.cloudEventAttributes = cloudEventAttributes;
   }
}
