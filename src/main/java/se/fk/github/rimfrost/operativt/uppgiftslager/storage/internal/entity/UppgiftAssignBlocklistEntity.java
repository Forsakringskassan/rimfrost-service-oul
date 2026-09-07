package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@IdClass(UppgiftAssignBlocklistId.class)
@Table(name = "uppgift_assign_blocklist")
public class UppgiftAssignBlocklistEntity
{
   @Id
   @Column(name = "handlaggare_id_typ_id", nullable = false)
   private String handlaggareIdTypId;

   @Id
   @Column(name = "handlaggare_id_varde", nullable = false)
   private String handlaggareIdVarde;

   @Id
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "uppgift_id", nullable = false)
   private UppgiftEntity uppgift;

   public String getHandlaggareIdTypId()
   {
      return handlaggareIdTypId;
   }

   public void setHandlaggareIdTypId(String handlaggareIdTypId)
   {
      this.handlaggareIdTypId = handlaggareIdTypId;
   }

   public String getHandlaggareIdVarde()
   {
      return handlaggareIdVarde;
   }

   public void setHandlaggareIdVarde(String handlaggareIdVarde)
   {
      this.handlaggareIdVarde = handlaggareIdVarde;
   }

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
}
