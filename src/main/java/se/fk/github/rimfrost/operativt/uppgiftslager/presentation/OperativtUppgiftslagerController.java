package se.fk.github.rimfrost.operativt.uppgiftslager.presentation;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.enums.UppgiftStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.ImmutableUppgiftGetAllResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.ImmutableUppgiftGetResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftGetAllResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftGetResponse;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftStatusUpdateRequest;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.dto.UppgiftStatusUpdateResponse;

@Produces("application/json")
@Consumes("application/json")
@ApplicationScoped
@Path("/uppgifter")
public class OperativtUppgiftslagerController
{
   private static final Logger log = LoggerFactory.getLogger(OperativtUppgiftslagerController.class);

   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @Inject
   PresentationMapper presentationMapper;

   @GET
   @Path("/getAll")
   @APIResponse(
      responseCode = "200",
      description = "All uppgifter",
      content = @Content(
         mediaType = MediaType.APPLICATION_JSON,
         schema = @Schema(implementation = ImmutableUppgiftGetAllResponse.class)
      )
   )
   public UppgiftGetAllResponse getAll()
   {
      var uppgifter = operativtUppgiftslagerService.getUppgifter();
      return presentationMapper.toUppgiftGetAllResponse(uppgifter);
   }

   @GET
   @Path("/get/{uppgift_id}")
   @APIResponse(
      responseCode = "200",
      description = "Uppgift found",
      content = @Content(
         mediaType = MediaType.APPLICATION_JSON,
         schema = @Schema(implementation = ImmutableUppgiftGetResponse.class)
      )
   )
   public UppgiftGetResponse get(@PathParam("uppgift_id") String uppgiftId)
   {  
      log.info("Fetching task with ID: {}", uppgiftId);
      var uppgift = operativtUppgiftslagerService.getUppgift(Long.valueOf(uppgiftId));
      log.info("Fetched task: {}", uppgift);
      return presentationMapper.toUppgiftGetResponse(uppgift);
   }

   // @GET
   // @Path("/{handlaggar_id}/next")
   // @Transactional
   // public UppgiftNextResponse next(@PathParam("handlaggar_id") String id)
   // {
   //    var uppgift = operativtUppgiftslagerService.getUppgifter().stream()
   //       .findFirst().get();
   //    return mapper.toNextResponse(task);
   // }

   @PATCH
   @Path("/{uppgift_id}")
   @Transactional
   public UppgiftStatusUpdateResponse update(@PathParam("uppgift_id") String uppgiftId, UppgiftStatusUpdateRequest body)
   {
      var response = operativtUppgiftslagerService.updateOperativeTask(Long.valueOf(uppgiftId),
            UppgiftStatus.AVSLUTAD);
      return presentationMapper.toUppgiftStatusUpdateResponse(response);
   }
}
