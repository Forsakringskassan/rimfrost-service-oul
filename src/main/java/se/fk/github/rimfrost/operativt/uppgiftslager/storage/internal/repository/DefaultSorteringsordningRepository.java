package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.DefaultSorteringsordningEntity;
import java.util.UUID;

/**
 * Panache repository for {@link DefaultSorteringsordningEntity}, keyed by the singleton
 * boolean primary key {@code lock = TRUE}.
 * The table holds at most one row, enforced by the primary key constraint.
 */
@ApplicationScoped
public class DefaultSorteringsordningRepository
      implements PanacheRepositoryBase<DefaultSorteringsordningEntity, Boolean>
{
   /**
    * Atomically sets the default sorteringsordning to {@code sorteringsordningId} if no default
    * exists yet. Concurrent calls are safe: the PRIMARY KEY on {@code lock = TRUE} ensures only
    * one row can ever be inserted, and {@code ON CONFLICT DO NOTHING} makes the second writer
    * silently succeed rather than throw a constraint violation.
    * <p>
    * A check-then-insert in application code would be vulnerable to a race condition where two
    * concurrent {@code POST /sorteringsordning} requests both observe an empty table and both
    * attempt an insert — the second would fail with a PK violation and roll back its transaction.
    */
   public void insertIfAbsent(UUID sorteringsordningId)
   {
      getEntityManager()
            .createNativeQuery(
                  "INSERT INTO {h-schema}default_sorteringsordning (lock, sorteringsordning_id) VALUES (TRUE, :id) ON CONFLICT DO NOTHING")
            .setParameter("id", sorteringsordningId)
            .executeUpdate();
   }
}
