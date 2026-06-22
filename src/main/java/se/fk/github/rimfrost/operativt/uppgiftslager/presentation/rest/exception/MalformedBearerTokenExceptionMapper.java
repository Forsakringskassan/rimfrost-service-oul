package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Maps {@link MalformedBearerTokenException} to HTTP 400 Bad Request.
 */
@Provider
public class MalformedBearerTokenExceptionMapper implements ExceptionMapper<MalformedBearerTokenException>
{
   @Override
   public Response toResponse(MalformedBearerTokenException exception)
   {
      return Response.status(Response.Status.BAD_REQUEST).build();
   }
}
