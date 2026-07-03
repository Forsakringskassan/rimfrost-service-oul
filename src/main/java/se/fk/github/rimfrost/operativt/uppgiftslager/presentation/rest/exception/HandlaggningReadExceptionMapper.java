package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.HandlaggningReadException;

@Provider
public class HandlaggningReadExceptionMapper implements ExceptionMapper<HandlaggningReadException>
{
   @Override
   public Response toResponse(final HandlaggningReadException exception)
   {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
   }
}
