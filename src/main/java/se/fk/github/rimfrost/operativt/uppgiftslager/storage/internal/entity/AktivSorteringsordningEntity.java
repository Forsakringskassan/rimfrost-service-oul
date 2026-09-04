package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity for the {@code aktiv_sorteringsordning} table.
 * <p>
 * The table intentionally holds at most one row: the primary key column {@code lock} is
 * constrained to {@code TRUE}, so only a single record can ever be inserted.
 * This design avoids a separate sequence or application-level singleton guard.
 */
@Entity
@Table(name = "aktiv_sorteringsordning")
public class AktivSorteringsordningEntity
{
   @Id
   @Column(nullable = false)
   private boolean lock = true;

   @Column(name = "sorteringsordning_id", nullable = false)
   private UUID sorteringsordningId;

   /**
    * @return always {@code true}; the singleton primary key
    */
   public boolean isLock()
   {
      return lock;
   }

   /**
    * @return the UUID of the currently aktiv sorteringsordning
    */
   public UUID getSorteringsordningId()
   {
      return sorteringsordningId;
   }

   /**
    * @param sorteringsordningId the UUID of the sorteringsordning to designate as aktiv
    */
   public void setSorteringsordningId(UUID sorteringsordningId)
   {
      this.sorteringsordningId = sorteringsordningId;
   }
}
