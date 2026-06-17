package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

/**
 * Thrown when the {@code Authorization} header is missing or does not match the
 * expected {@code Bearer <typId>:<varde>} format.
 * The caller should use HTTP 400 Bad Request.
 */
public class MalformedBearerTokenException extends RuntimeException
{
   /**
    * @param message a description of the format violation
    */
   public MalformedBearerTokenException(String message)
   {
      super(message);
   }
}
