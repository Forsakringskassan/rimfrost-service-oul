package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.SidStatusException;

@Provider
public class SidStatusExceptionMapper implements ExceptionMapper<SidStatusException>
{
   @Override
   public Response toResponse(final SidStatusException exception)
   {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
   }
}
