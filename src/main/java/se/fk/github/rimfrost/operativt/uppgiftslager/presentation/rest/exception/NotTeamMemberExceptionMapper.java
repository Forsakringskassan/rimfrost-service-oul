package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.NotTeamMemberException;

/**
 * Maps {@link NotTeamMemberException} to HTTP 403 Forbidden (AC7).
 */
@Provider
public class NotTeamMemberExceptionMapper implements ExceptionMapper<NotTeamMemberException>
{
   @Override
   public Response toResponse(NotTeamMemberException exception)
   {
      return Response.status(Response.Status.FORBIDDEN).build();
   }
}
