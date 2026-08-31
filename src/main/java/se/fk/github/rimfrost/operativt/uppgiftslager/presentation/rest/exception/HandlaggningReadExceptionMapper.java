package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.HandlaggningReadException;

/**
 * {@code assignNewTask} (FKPOC-938) and {@code reassignUppgift} (FKPOC-939, via
 * {@code resolveContainsSid}) both catch {@link HandlaggningReadException} themselves and never
 * let it reach here. The listing endpoints (FKPOC-940, via {@code isSidBlocked}) deliberately do
 * NOT catch it — a failed SID check means OUL can't confirm an uppgift's SID status, so the
 * whole list call is meant to fail loud instead of guessing — which is what reaches this mapper.
 */
@Provider
public class HandlaggningReadExceptionMapper implements ExceptionMapper<HandlaggningReadException>
{
   @Override
   public Response toResponse(final HandlaggningReadException exception)
   {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
   }
}
