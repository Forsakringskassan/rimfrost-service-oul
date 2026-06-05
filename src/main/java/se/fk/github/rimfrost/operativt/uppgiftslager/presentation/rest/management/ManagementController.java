package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.DefaultApi;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UpdateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;

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
   @Inject
   EnumMapper enumMapper;

   @Override
   public UppgiftResponse createUppgift(CreateUppgiftRequest createUppgiftRequest)
   {
      log.info("Creating uppgift for handlaggningId: {}", createUppgiftRequest.getHandlaggningId());
      var addRequest = managementMapper.toAddRequest(createUppgiftRequest);
      var uppgift = operativtUppgiftslagerService.addOperativeTask(addRequest, createUppgiftRequest.getSubTopic(),
            createUppgiftRequest.getCloudeventAttributes());

      var response = new UppgiftResponse();
      response.setUppgiftId(uppgift.uppgiftId());
      response.setHandlaggningId(uppgift.handlaggningId());
      response.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      response.setCloudeventAttributes(uppgift.cloudeventAttributes());
      return response;
   }

   @Override
   public UppgiftResponse endUppgift(UUID uppgiftId, EndUppgiftRequest endUppgiftRequest)
   {
      log.info("Ending uppgift: {}", uppgiftId);
      var uppgift = operativtUppgiftslagerService.endTask(uppgiftId, endUppgiftRequest.getReason());

      if (uppgift == null)
      {
         throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      var response = new UppgiftResponse();
      response.setUppgiftId(uppgift.uppgiftId());
      response.setHandlaggningId(uppgift.handlaggningId());
      response.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      response.setCloudeventAttributes(uppgift.cloudeventAttributes());
      return response;
   }

   @Override
   public List<OperativUppgift> getUppgifter()
   {
      var uppgifter = operativtUppgiftslagerService.getTasks();
      return managementMapper.toOperativUppgiftList(uppgifter);
   }

   @Override
   public OperativUppgift unassignUppgift(UUID uppgiftId)
   {
      var uppgift = operativtUppgiftslagerService.unassignTask(uppgiftId);

      if (uppgift == null)
      {
         throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      return managementMapper.toOperativUppgift(uppgift);
   }

   @Override
   public OperativUppgift updateUppgift(UUID uppgiftId, @Valid @NotNull UpdateUppgiftRequest updateUppgiftRequest)
   {
      Idtyp handlaggarId = null;

      if (updateUppgiftRequest.getHandlaggarId() != null)
      {
         handlaggarId = managementMapper.toIdTyp(updateUppgiftRequest.getHandlaggarId());
      }

      var uppgift = operativtUppgiftslagerService.updateTask(uppgiftId, handlaggarId);

      if (uppgift == null)
      {
         throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      return managementMapper.toOperativUppgift(uppgift);
   }
}
