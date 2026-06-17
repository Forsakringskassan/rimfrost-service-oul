package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SorteringsordningNotFoundException;

/**
 * Maps {@link SorteringsordningNotFoundException} to HTTP 404 Not Found.
 */
@Provider
public class SorteringsordningServiceNotFoundExceptionMapper
      implements ExceptionMapper<SorteringsordningNotFoundException>
{
   /** {@inheritDoc} */
   @Override
   public Response toResponse(SorteringsordningNotFoundException exception)
   {
      return Response.status(Response.Status.NOT_FOUND).build();
   }
}
