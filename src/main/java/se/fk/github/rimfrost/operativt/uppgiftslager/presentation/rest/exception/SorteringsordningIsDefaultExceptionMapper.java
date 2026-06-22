package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.SorteringsordningIsDefaultException;

/**
 * Maps {@link SorteringsordningIsDefaultException} to HTTP 409 Conflict.
 */
@Provider
public class SorteringsordningIsDefaultExceptionMapper implements ExceptionMapper<SorteringsordningIsDefaultException>
{
   @Override
   public Response toResponse(SorteringsordningIsDefaultException exception)
   {
      return Response.status(Response.Status.CONFLICT).build();
   }
}
