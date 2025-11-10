package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util.PresentationRestMapper;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.OperativtUppgiftslagerControllerApi;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.*;

@SuppressWarnings("unused")
@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
@Path("/uppgifter")
public class OperativtUppgiftslagerController implements OperativtUppgiftslagerControllerApi
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerController.class);

   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @Inject
   PresentationRestMapper presentationRestMapper;

   @GET
   @APIResponse(responseCode = "200", description = "Alla uppgifter", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetUppgifterResponse.class)))
   public GetUppgifterResponse getUppgifter()
   {
      var uppgifter = operativtUppgiftslagerService.getUppgifter();
      return presentationRestMapper.toGetUppgifterResponse(uppgifter);
   }

   @GET
   @Path("/{uppgift_id}")
   @APIResponse(responseCode = "200", description = "En uppgift", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetUppgiftResponse.class)))
   @Override
   public GetUppgiftResponse getUppgift(@PathParam("uppgift_id") String uppgiftId)
   {
      log.info("Fetching task with ID: {}", uppgiftId);
      var uppgift = operativtUppgiftslagerService.getUppgift(Long.valueOf(uppgiftId));
      log.info("Fetched task: {}", uppgift);
      return presentationRestMapper.toGetUppgiftResponse(uppgift);
   }

   @GET
   @Path("/handlaggare/{handlaggar_id}")
   @APIResponse(responseCode = "200", description = "Uppgifter för en handläggare", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = GetUppgifterHandlaggareResponse.class)))
   @Override
   public GetUppgifterHandlaggareResponse getUppgifterHandlaggare(@PathParam("handlaggar_id") String handlaggarId)
   {
      var uppgifter = operativtUppgiftslagerService.getUppgifterHandlaggare(handlaggarId);
      return presentationRestMapper.toGetUppgifterHandlaggareResponse(uppgifter);
   }

   @POST
   @Path("/handlaggare/{handlaggar_id}")
   @APIResponse(responseCode = "200", description = "Hämta uppgift för en handläggare", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = PostUppgifterHandlaggareResponse.class)))
   public PostUppgifterHandlaggareResponse postUppgifterHandlaggare(@PathParam("handlaggar_id") String handlaggarId)
   {
      var uppgift = operativtUppgiftslagerService.assignNewTask(handlaggarId);
      return presentationRestMapper.toPostUppgifterHandlaggareResponse(uppgift);
   }

   @PATCH
   @Path("/{uppgift_id}")
   @Transactional
   @Override
   public PatchUppgiftResponse patchUppgift(@PathParam("uppgift_id") String uppgiftId,
         @Valid @NotNull PatchUppgiftRequest patchUppgiftRequest)
   {
      var updatedUppgift = operativtUppgiftslagerService.updateOperativeTask(Long.valueOf(uppgiftId),
            UppgiftStatus.valueOf(patchUppgiftRequest.getStatus()));
      return presentationRestMapper.toPatchUppgiftResponse(updatedUppgift);
   }
}
