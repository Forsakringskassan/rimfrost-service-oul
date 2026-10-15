package se.fk.github.rimfrost.operativt.uppgiftslager.logic.permissions;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.rimfrost.adapter.permissions.adapter.PermissionsAdapter;
import se.fk.rimfrost.adapter.permissions.adapter.PermissionsException;

/**
 * Resolves SID-behörighet via the permissions adapter, failing open on any error.
 */
@ApplicationScoped
public class SidBehorighetCheckerService implements SidBehorighetChecker
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SidBehorighetCheckerService.class);

   @Inject
   PermissionsAdapter permissionsAdapter;

   /**
    * {@inheritDoc}
    * <p>
    * Returns {@code false} on any failure — a permissions service outage should degrade to
    * the conservative no-behörighet behaviour rather than block assignment or listing.
    */
   @Override
   public boolean hasSidBehorighet(Idtyp handlaggare)
   {
      try
      {
         return permissionsAdapter.hasSidPermission(handlaggare.typId(), handlaggare.varde());
      }
      catch (RuntimeException | PermissionsException e)
      {
         LOGGER.warn("Failed to resolve SID-behörighet for handlaggarId: {}; treating as no behörighet",
               handlaggare.varde(), e);
         return false;
      }
   }
}
