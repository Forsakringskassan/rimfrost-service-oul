package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.UppgiftNotFoundException;

/**
 * Maps {@link UppgiftNotFoundException} to HTTP 404 Not Found.
 */
@Provider
public class UppgiftNotFoundExceptionMapper implements ExceptionMapper<UppgiftNotFoundException>
{
   /** {@inheritDoc} */
   @Override
   public Response toResponse(UppgiftNotFoundException exception)
   {
      return Response.status(Response.Status.NOT_FOUND).build();
   }
}
