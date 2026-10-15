package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.AssignableFilterNotSupportedException;

/**
 * Maps {@link AssignableFilterNotSupportedException} to HTTP 501 Not Implemented.
 */
@Provider
public class AssignableFilterNotSupportedExceptionMapper
      implements ExceptionMapper<AssignableFilterNotSupportedException>
{
   @Override
   public Response toResponse(AssignableFilterNotSupportedException exception)
   {
      return Response.status(501).build(); // 501 Not Implemented — JAX-RS Response.Status has no enum entry for this code
   }
}
