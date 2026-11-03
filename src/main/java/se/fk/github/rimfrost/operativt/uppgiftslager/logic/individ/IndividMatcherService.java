package se.fk.github.rimfrost.operativt.uppgiftslager.logic.individ;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.HandlaggningReadException;
import se.fk.rimfrost.framework.handlaggning.adapter.HandlaggningAdapter;
import se.fk.rimfrost.framework.handlaggning.exception.HandlaggningException;

/**
 * Reads a handläggning and checks whether a given individ appears in its yrkande roles.
 */
@ApplicationScoped
public class IndividMatcherService implements IndividMatcher
{
   private static final Logger LOGGER = LoggerFactory.getLogger(IndividMatcherService.class);

   @Inject
   HandlaggningAdapter handlaggningAdapter;

   /**
    * {@inheritDoc}
    */
   @Override
   public Optional<String> findYrkanderoll(UUID handlaggningId, UUID uppgiftId, Idtyp individ)
   {
      try
      {
         var handlaggning = handlaggningAdapter.readHandlaggning(handlaggningId);
         return handlaggning.yrkande().rollerIYrkande().stream()
               .filter(roll -> individ.typId().equals(roll.individ().typId())
                     && individ.varde().equals(roll.individ().varde()))
               .map(roll -> roll.yrkandeRollId())
               .findFirst();
      }
      catch (HandlaggningException e)
      {
         LOGGER.warn("Failed to read handlaggning {} for individ matching (uppgiftId {})", handlaggningId, uppgiftId, e);
         throw new HandlaggningReadException(uppgiftId, e);
      }
   }
}
