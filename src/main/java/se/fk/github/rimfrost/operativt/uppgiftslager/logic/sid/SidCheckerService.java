package se.fk.github.rimfrost.operativt.uppgiftslager.logic.sid;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.HandlaggningReadException;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SidStatusException;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;
import se.fk.rimfrost.framework.sid.adapter.SidAdapter;
import se.fk.rimfrost.framework.sid.exception.SidException;

@ApplicationScoped
public class SidCheckerService implements SidChecker
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SidCheckerService.class);

   @Inject
   HandlaggningAdapter handlaggningAdapter;

   @Inject
   SidAdapter sidAdapter;

   @Override
   public boolean containsSid(UUID handlaggningId, UUID uppgiftId)
   {
      try
      {
         var handlaggning = handlaggningAdapter.readHandlaggning(handlaggningId);
         return sidAdapter.containsSid(handlaggning.yrkande().individYrkandeRoller().stream().map(
               individYrkandeRoll -> (se.fk.rimfrost.framework.sid.model.Idtyp) se.fk.rimfrost.framework.sid.model.ImmutableIdtyp
                     .builder()
                     .typId(individYrkandeRoll.individ().typId())
                     .varde(individYrkandeRoll.individ().varde())
                     .build())
               .toList());
      }
      catch (HandlaggningException e)
      {
         // WARN, not ERROR: the caller now handles this (see assignNewTask), but it stays the
         // one durable signal for a permanently orphaned handläggning — worth alerting on this
         // specific message even though it's below ERROR level.
         LOGGER.warn("Failed to read handlaggning for handlaggning id: {} and uppgift id: {}", handlaggningId,
               uppgiftId, e);

         throw new HandlaggningReadException(uppgiftId, e);
      }
      catch (SidException e)
      {
         LOGGER.error("Failed to read SID status for handlaggning id: {} and uppgift id: {}", handlaggningId,
               uppgiftId, e);

         throw new SidStatusException(e);
      }
   }
}
