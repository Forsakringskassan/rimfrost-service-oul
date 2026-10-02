package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IdentityInvalidAuthCredentialsExceptionMapper implements ExceptionMapper<IdentityInvalidAuthCredentialsException>
{
   @Override
   public Response toResponse(IdentityInvalidAuthCredentialsException exception)
   {
      return Response.status(Response.Status.UNAUTHORIZED).build();
   }
}
