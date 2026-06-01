package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@IdClass(UppgiftIndividId.class)
@Table(name = "uppgift_individ")
public class UppgiftIndividEntity
{
   @Id
   @Column(name = "uppgift_id", nullable = false)
   private UUID uppgiftId;

   @Id
   @Column(nullable = false)
   private String typId;

   @Id
   @Column(nullable = false)
   private String varde;

   public UUID getUppgiftId()
   {
      return uppgiftId;
   }

   public void setUppgiftId(UUID uppgiftId)
   {
      this.uppgiftId = uppgiftId;
   }

   public String getTypId()
   {
      return typId;
   }

   public void setTypId(String typId)
   {
      this.typId = typId;
   }

   public String getVarde()
   {
      return varde;
   }

   public void setVarde(String varde)
   {
      this.varde = varde;
   }
}
