package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.converter.SorteringsordningEntriesConverter;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningEntry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the {@code sorteringsordning} table.
 * Stores the sort-order configuration as a JSON TEXT column via
 * {@link se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.converter.SorteringsordningEntriesConverter}.
 * Timestamps are managed automatically by JPA lifecycle callbacks.
 */
@Entity
@Table(name = "sorteringsordning")
public class SorteringsordningPersistenceEntity
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

   @Column(nullable = false, columnDefinition = "text")
   @Convert(converter = SorteringsordningEntriesConverter.class)
   private List<SorteringsordningEntry> entries;

   /**
    * Sets {@code createdAt} on first insert (if not already supplied) and always refreshes
    * {@code updatedAt}. Allowing an explicit {@code createdAt} lets the service layer preserve
    * the original creation timestamp when constructing the entity from a domain object.
    */
   @PrePersist
   void onCreate()
   {
      if (createdAt == null)
      {
         createdAt = Instant.now();
      }
      updatedAt = Instant.now();
   }

   /** Refreshes {@code updatedAt} on every UPDATE. */
   @PreUpdate
   void onUpdate()
   {
      updatedAt = Instant.now();
   }

   /**
    * @return the sorteringsordning UUID
    */
   public UUID getId()
   {
      return id;
   }

   /**
    * @param id the sorteringsordning UUID
    */
   public void setId(UUID id)
   {
      this.id = id;
   }

   /**
    * @return the optimistic-lock version counter
    */
   public long getVersion()
   {
      return version;
   }

   /**
    * @param version the optimistic-lock version counter
    */
   public void setVersion(long version)
   {
      this.version = version;
   }

   /**
    * @return the instant this record was first persisted
    */
   public Instant getCreatedAt()
   {
      return createdAt;
   }

   /**
    * Allows the service layer to supply an explicit creation timestamp rather than relying
    * on the {@link PrePersist} default.
    *
    * @param createdAt the creation instant to store
    */
   public void setCreatedAt(Instant createdAt)
   {
      this.createdAt = createdAt;
   }

   /**
    * @return the instant this record was last updated
    */
   public Instant getUpdatedAt()
   {
      return updatedAt;
   }

   /**
    * @return the list of sort-order entries stored in this sorteringsordning
    */
   public List<SorteringsordningEntry> getEntries()
   {
      return entries;
   }

   /**
    * @param entries the list of sort-order entries to store
    */
   public void setEntries(List<SorteringsordningEntry> entries)
   {
      this.entries = entries != null ? List.copyOf(entries) : null;
   }
}
