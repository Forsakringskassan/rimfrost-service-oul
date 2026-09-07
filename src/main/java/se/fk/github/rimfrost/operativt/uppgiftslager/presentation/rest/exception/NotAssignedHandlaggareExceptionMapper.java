package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.NotAssignedHandlaggareException;

/**
 * Maps {@link NotAssignedHandlaggareException} to HTTP 403 Forbidden.
 */
@Provider
public class NotAssignedHandlaggareExceptionMapper implements ExceptionMapper<NotAssignedHandlaggareException>
{
   @Override
   public Response toResponse(NotAssignedHandlaggareException exception)
   {
      return Response.status(Response.Status.FORBIDDEN).build();
   }
}
