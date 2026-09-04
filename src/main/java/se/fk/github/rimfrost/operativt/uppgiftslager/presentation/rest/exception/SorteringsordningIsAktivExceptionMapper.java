package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.SorteringsordningIsAktivException;

/**
 * Maps {@link SorteringsordningIsAktivException} to HTTP 409 Conflict.
 */
@Provider
public class SorteringsordningIsAktivExceptionMapper implements ExceptionMapper<SorteringsordningIsAktivException>
{
   @Override
   public Response toResponse(SorteringsordningIsAktivException exception)
   {
      return Response.status(Response.Status.CONFLICT).build();
   }
}
