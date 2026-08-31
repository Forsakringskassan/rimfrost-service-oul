package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.SidNotAuthorizedException;

/**
 * Maps {@link SidNotAuthorizedException} to HTTP 403 Forbidden.
 */
@Provider
public class SidNotAuthorizedExceptionMapper implements ExceptionMapper<SidNotAuthorizedException>
{
   @Override
   public Response toResponse(SidNotAuthorizedException exception)
   {
      return Response.status(Response.Status.FORBIDDEN).build();
   }
}
