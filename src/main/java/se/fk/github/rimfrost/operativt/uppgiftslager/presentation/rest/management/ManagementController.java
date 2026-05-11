package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.DefaultApi;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;

@ApplicationScoped
@Path("/uppgifter")
@Produces("application/json")
@Consumes("application/json")
public class ManagementController implements DefaultApi
{
   private static final Logger log = LoggerFactory.getLogger(ManagementController.class);

   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @Inject
   ManagementMapper managementMapper;

   @Override
   public UppgiftResponse createUppgift(CreateUppgiftRequest createUppgiftRequest)
   {
      log.info("Creating uppgift for handlaggningId: {}", createUppgiftRequest.getHandlaggningId());
      var addRequest = managementMapper.toAddRequest(createUppgiftRequest);
      var uppgift = operativtUppgiftslagerService.addOperativeTask(addRequest, createUppgiftRequest.getReplyTopic(),
            createUppgiftRequest.getCloudeventAttributes());

      var response = new UppgiftResponse();
      response.setUppgiftId(uppgift.uppgiftId());
      response.setHandlaggningId(uppgift.handlaggningId());
      return response;
   }

   @Override
   public UppgiftResponse endUppgift(UUID uppgiftId)
   {
      throw new UnsupportedOperationException("Not yet implemented");
   }
}
