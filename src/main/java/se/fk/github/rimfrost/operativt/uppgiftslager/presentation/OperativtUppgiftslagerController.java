package se.fk.github.rimfrost.operativt.uppgiftslager.presentation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftStatusUpdateRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftStatusUpdateResponse;

@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
@Path("/uppgifter")
public class OperativtUppgiftslagerController
{
   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @PATCH
   @Path("/{uppgift_id}")
   @Transactional
   public UppgiftStatusUpdateResponse update(@PathParam("uppgift_id") String id, UppgiftStatusUpdateRequest body)
   {
    // Använd den id man får från path och sedan status från body för att uppdatera i OperativUppgitslagerService
    //   var updated = service.updateStatus(id, mapper.toDomainStatus(body.getStatus()));
    //   return mapper.toApiUppgift(updated);
        return null;
   }
}
