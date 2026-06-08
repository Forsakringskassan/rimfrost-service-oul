package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * JPA entity for the {@code default_sorteringsordning} table.
 * <p>
 * The table intentionally holds at most one row: the primary key column {@code lock} is
 * constrained to {@code TRUE}, so only a single record can ever be inserted.
 * This design avoids a separate sequence or application-level singleton guard.
 */
@Entity
@Table(name = "default_sorteringsordning")
public class DefaultSorteringsordningEntity
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
    * @param lock must always be {@code true}
    */
   public void setLock(boolean lock)
   {
      this.lock = lock;
   }

   /**
    * @return the UUID of the currently active default sorteringsordning
    */
   public UUID getSorteringsordningId()
   {
      return sorteringsordningId;
   }

   /**
    * @param sorteringsordningId the UUID of the sorteringsordning to designate as default
    */
   public void setSorteringsordningId(UUID sorteringsordningId)
   {
      this.sorteringsordningId = sorteringsordningId;
   }
}
