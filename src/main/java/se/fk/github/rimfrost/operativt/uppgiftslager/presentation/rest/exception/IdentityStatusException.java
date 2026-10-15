package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

/**
 * Thrown when identity resolution fails with an unexpected error (not an auth credentials problem).
 * The caller should use HTTP 500 Internal Server Error.
 */
public class IdentityStatusException extends RuntimeException
{
   public IdentityStatusException()
   {
      super();
   }
}
