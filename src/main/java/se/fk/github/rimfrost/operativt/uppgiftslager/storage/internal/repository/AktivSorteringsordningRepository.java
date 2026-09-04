package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.entity.AktivSorteringsordningEntity;
import java.util.Optional;
import java.util.UUID;

/**
 * Panache repository for {@link AktivSorteringsordningEntity}, keyed by the singleton
 * boolean primary key {@code lock = TRUE}.
 * The table holds at most one row, enforced by the primary key constraint.
 */
@ApplicationScoped
public class AktivSorteringsordningRepository
      implements PanacheRepositoryBase<AktivSorteringsordningEntity, Boolean>
{
   /**
    * Returns the current aktiv row with a pessimistic write lock, blocking any concurrent
    * transaction that tries to update or insert the same row until this transaction completes.
    * Use this before checking and deleting a sorteringsordning to prevent a TOCTOU race where
    * a concurrent {@code setAktivSorteringsordning} promotes the target between the check and
    * the delete.
    *
    * @return the locked aktiv row, or empty if no aktiv has been set
    */
   public Optional<AktivSorteringsordningEntity> findForUpdate()
   {
      return findAll().withLock(LockModeType.PESSIMISTIC_WRITE).firstResultOptional();
   }

   /**
    * Atomically sets the aktiv sorteringsordning to {@code sorteringsordningId} if no aktiv
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
                  "INSERT INTO {h-schema}aktiv_sorteringsordning (lock, sorteringsordning_id) VALUES (TRUE, :id) ON CONFLICT DO NOTHING")
            .setParameter("id", sorteringsordningId)
            .executeUpdate();
   }
}
