package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import java.util.UUID;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.util.EnumMapper;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.UppgifterApi;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.ReglerApi;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.CreateUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.EndUppgiftRequest;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.ProcessInfo;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.OperativUppgift;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UpdateUppgiftRequest;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;
import se.fk.rimfrost.oul.management.regler.jaxrsspec.controllers.generatedsource.model.UppgiftResponse;

/**
 * REST controller for the {@code /uppgifter} resource.
 * Implements the generated {@link UppgifterApi} and {@link ReglerApi} interfaces
 * from the OpenAPI spec.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Path("/uppgifter")
@Produces("application/json")
@Consumes("application/json")
public class UppgifterController implements UppgifterApi, ReglerApi
{
   private static final Logger log = LoggerFactory.getLogger(UppgifterController.class);

   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @Inject
   ManagementMapper managementMapper;
   @Inject
   EnumMapper enumMapper;

   @Override
   @POST
   @ResponseStatus(201)
   public UppgiftResponse createUppgift(@Valid @NotNull CreateUppgiftRequest createUppgiftRequest)
   {
      log.info("Creating uppgift for handlaggningId: {}", createUppgiftRequest.getHandlaggningId());
      var addRequest = managementMapper.toAddRequest(createUppgiftRequest);
      var processInfo = createUppgiftRequest.getProcessInfo();
      var uppgift = operativtUppgiftslagerService.addOperativeTask(addRequest, createUppgiftRequest.getSubTopic(),
            processInfo.getReplyTopic(), processInfo.getCloudeventAttributes());

      var responseProcessInfo = new ProcessInfo();
      responseProcessInfo.setCloudeventAttributes(uppgift.cloudeventAttributes());
      responseProcessInfo.setReplyTopic(uppgift.replyTopic());

      var response = new UppgiftResponse();
      response.setUppgiftId(uppgift.uppgiftId());
      response.setHandlaggningId(uppgift.handlaggningId());
      response.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      response.setProcessInfo(responseProcessInfo);
      return response;
   }

   @Override
   @POST
   @Path("/{uppgiftId}/end")
   public UppgiftResponse endUppgift(@PathParam("uppgiftId") UUID uppgiftId,
         @Valid @NotNull EndUppgiftRequest endUppgiftRequest)
   {
      log.info("Ending uppgift: {}", uppgiftId);
      var uppgift = operativtUppgiftslagerService.endTask(uppgiftId, endUppgiftRequest.getReason());

      var endResponseProcessInfo = new ProcessInfo();
      endResponseProcessInfo.setCloudeventAttributes(uppgift.cloudeventAttributes());
      endResponseProcessInfo.setReplyTopic(uppgift.replyTopic());

      var response = new UppgiftResponse();
      response.setUppgiftId(uppgift.uppgiftId());
      response.setHandlaggningId(uppgift.handlaggningId());
      response.setStatus(enumMapper.mapUppgiftStatusToStatus(uppgift.status()));
      response.setProcessInfo(endResponseProcessInfo);
      return response;
   }

   @Override
   @GET
   public UppgiftPage getUppgifter(@QueryParam("limit") @NotNull @Min(1) Integer limit,
         @QueryParam("sorteringsordningId") UUID sorteringsordningId,
         @QueryParam("offset") @DefaultValue("0") @Min(0) Integer offset)
   {
      var page = operativtUppgiftslagerService.getUppgifterPage(limit, offset != null ? offset : 0, sorteringsordningId);
      return managementMapper.toUppgiftPage(page);
   }

   @Override
   @POST
   @Path("/{uppgiftId}/unassign")
   public OperativUppgift unassignUppgift(UUID uppgiftId)
   {
      var uppgift = operativtUppgiftslagerService.unassignTask(uppgiftId);
      return managementMapper.toOperativUppgift(uppgift);
   }

   @Override
   @PATCH
   @Path("/{uppgiftId}")
   public OperativUppgift updateUppgift(UUID uppgiftId, @Valid @NotNull UpdateUppgiftRequest updateUppgiftRequest)
   {
      Idtyp handlaggarId = null;

      if (updateUppgiftRequest.getHandlaggarId() != null)
      {
         handlaggarId = managementMapper.toIdTyp(updateUppgiftRequest.getHandlaggarId());
      }

      var uppgift = operativtUppgiftslagerService.updateTask(uppgiftId, handlaggarId);
      return managementMapper.toOperativUppgift(uppgift);
   }
}
