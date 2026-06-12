package se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

/**
 * Test-only helper that resets all persistent state between test cases.
 * Lives in the {@code storage.internal} package to retain package-private access
 * to {@link PanacheOulDataStorage#invalidateCountCache()}.
 */
@ApplicationScoped
public class StorageTestCleaner
{
   @Inject
   EntityManager em;

   @Inject
   PanacheOulDataStorage panacheOulDataStorage;

   /**
    * Deletes all uppgifter and sorteringsordningar, and invalidates the count cache.
    */
   @Transactional
   public void clearAll()
   {
      em.createQuery("DELETE FROM UppgiftEntity").executeUpdate();
      em.createNativeQuery("DELETE FROM {h-schema}default_sorteringsordning").executeUpdate();
      em.createNativeQuery("DELETE FROM {h-schema}sorteringsordning").executeUpdate();
      panacheOulDataStorage.invalidateCountCache();
   }

   /**
    * Invalidates the in-process count cache without clearing data.
    * Use this to simulate TTL expiry in cache-behaviour tests.
    */
   public void invalidateCountCache()
   {
      panacheOulDataStorage.invalidateCountCache();
   }
}
