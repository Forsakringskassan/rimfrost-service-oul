package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.kafka.StatusNotificationException;

/**
 * Maps {@link StatusNotificationException} to HTTP 500 Internal Server Error.
 */
@Provider
public class StatusNotificationExceptionMapper implements ExceptionMapper<StatusNotificationException>
{
   @Override
   public Response toResponse(StatusNotificationException exception)
   {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
   }
}
