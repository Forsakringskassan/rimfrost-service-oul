package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import se.fk.github.rimfrost.operativt.uppgiftslager.storage.exception.HandlaggningReadException;

/**
 * Currently unreachable: {@code assignNewTask}, the only path that can trigger a
 * {@link HandlaggningReadException} today, now catches it itself (FKPOC-938) instead of letting
 * it reach the REST layer. Kept for the planned D1 (ommarkering) and E1 (listning) call sites
 * from sid-behorighet-plan.md, which will reuse the same {@code containsSid} check without that
 * catch and will need this 500 fallback.
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
