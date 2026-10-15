package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

/**
 * Thrown when the caller's Authorization header contains invalid credentials.
 * The caller should use HTTP 401 Unauthorized.
 */
public class IdentityInvalidAuthCredentialsException extends RuntimeException
{
   public IdentityInvalidAuthCredentialsException()
   {
      super();
   }
}
