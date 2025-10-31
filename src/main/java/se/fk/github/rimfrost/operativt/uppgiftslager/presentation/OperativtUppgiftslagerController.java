package se.fk.github.rimfrost.operativt.uppgiftslager.presentation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;


@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
@Path("/uppgifter")
public class OperativtUppgiftslagerController {
  @Inject OperativtUppgiftslagerService service;
//   @Inject PresentationMapper mapper;

  @POST @Path("/{handlaggar_id}/next")
  @Transactional
  public UppgiftNextResponse next(@PathParam("handlaggar_id") String id) {
    var task = service.reserveNextFor(id);
    return mapper.toNextResponse(task);
  }

  @PATCH @Path("/{uppgift_id}")
  @Transactional
  public UppgiftRestDTO update(@PathParam("uppgift_id") String id, UppgiftStatusUpdateRequest body) {
    var updated = service.updateStatus(id, mapper.toDomainStatus(body.getStatus()));
    return mapper.toApiUppgift(updated);
  }
}
