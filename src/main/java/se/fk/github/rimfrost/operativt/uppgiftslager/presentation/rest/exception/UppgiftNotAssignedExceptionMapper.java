package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.UppgiftNotAssignedException;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.UppgiftNotFoundException;

/**
 * Maps {@link UppgiftNotFoundException} to HTTP 500 Internal Server Error.
 */
public class UppgiftNotAssignedExceptionMapper implements ExceptionMapper<UppgiftNotAssignedException>
{
   /** {@inheritDoc} */
   @Override
   public Response toResponse(UppgiftNotAssignedException exception)
   {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
   }
}
