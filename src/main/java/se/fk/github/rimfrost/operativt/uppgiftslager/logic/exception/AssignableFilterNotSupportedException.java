package se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception;

/**
 * Thrown when the caller requests a filter combination for {@code POST /uppgifter/individ} that
 * is not yet implemented. The caller should use HTTP 501 Not Implemented.
 *
 * <p>Currently only {@code assignable=false} is supported; omitting {@code assignable} or
 * supplying {@code assignable=true} triggers this exception.
 */
public class AssignableFilterNotSupportedException extends RuntimeException
{
   /**
    * Constructs a new exception for an unsupported assignable filter.
    */
   public AssignableFilterNotSupportedException()
   {
      super("POST /uppgifter/individ stödjer för tillfället endast assignable=false");
   }
}
