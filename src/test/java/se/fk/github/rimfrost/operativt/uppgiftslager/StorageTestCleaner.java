package se.fk.github.rimfrost.operativt.uppgiftslager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.internal.PanacheOulDataStorage;

@ApplicationScoped
public class StorageTestCleaner
{
   @Inject
   EntityManager em;

   @Inject
   PanacheOulDataStorage panacheOulDataStorage;

   @Transactional
   public void clearAll()
   {
      em.createQuery("DELETE FROM UppgiftEntity").executeUpdate();
      panacheOulDataStorage.clearSorteringsordning();
   }
}
