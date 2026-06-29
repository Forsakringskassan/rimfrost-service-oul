package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.validation.ValidationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException>
{
   Logger logger = LoggerFactory.getLogger(ValidationExceptionMapper.class);

   @Override
   public Response toResponse(ValidationException exception)
   {
      logger.error("Request terminated with ValidationException", exception);

      return Response.status(Response.Status.BAD_REQUEST).build();
   }
}
