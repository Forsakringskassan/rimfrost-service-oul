package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util;

import jakarta.enterprise.context.ApplicationScoped;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.MalformedBearerTokenException;

/**
 * Parses handläggare identity from a bearer token of the form {@code <typId>:<varde>}.
 *
 * <p>This is a POC-only mechanism. Production use will replace this with real JWT parsing.
 */
@ApplicationScoped
public class BearerTokenExtractor
{
   private static final String BEARER_PREFIX = "Bearer ";

   /**
    * Extracts {@link Idtyp} from an {@code Authorization} header value.
    *
    * @param authorizationHeader the full Authorization header value, e.g. {@code Bearer <typId>:<varde>}
    * @return the parsed handläggare identity
    * @throws MalformedBearerTokenException if the header is missing or does not match the expected format
    */
   public Idtyp extract(String authorizationHeader)
   {
      if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX))
      {
         throw new MalformedBearerTokenException("Missing or malformed Authorization header");
      }
      var token = authorizationHeader.substring(BEARER_PREFIX.length());
      var colonIdx = token.indexOf(':');
      if (colonIdx < 0)
      {
         throw new MalformedBearerTokenException("Bearer token must be of the form <typId>:<varde>");
      }
      return ImmutableIdtyp.builder()
            .typId(token.substring(0, colonIdx))
            .varde(token.substring(colonIdx + 1))
            .build();
   }
}
