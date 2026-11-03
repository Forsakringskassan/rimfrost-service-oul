package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.IdentityInvalidAuthCredentialsException;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.IdentityStatusException;
import se.fk.rimfrost.adapter.identity.adapter.IdentityAdapter;
import se.fk.rimfrost.adapter.identity.exception.IdentityException;

@ApplicationScoped
public class IdentityUtil
{
   private static final Logger LOGGER = LoggerFactory.getLogger(IdentityUtil.class);

   @Inject
   IdentityAdapter identityAdapter;

   public Idtyp getIdentity(String headerValue)
   {
      try
      {
         var identity = identityAdapter.getIdentity(headerValue);
         return ImmutableIdtyp.builder().typId(identity.typId()).varde(identity.varde()).build();
      }
      catch (IdentityException e)
      {
         if (e.getErrorType() == IdentityException.ErrorType.UNAUTHORIZED)
         {
            throw new IdentityInvalidAuthCredentialsException();
         }

         LOGGER.error("Unexpected error while attempting to resolve identity from authorization header", e);
         throw new IdentityStatusException();
      }
   }
}
