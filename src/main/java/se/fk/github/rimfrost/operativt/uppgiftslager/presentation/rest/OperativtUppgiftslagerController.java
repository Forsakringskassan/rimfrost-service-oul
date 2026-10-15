package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util.IdentityUtil;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.util.PresentationRestMapper;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.OperativtUppgiftslagerControllerApi;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.GetUppgifterHandlaggareResponse;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgiftHandlaggareResponse;
import se.fk.rimfrost.oul.handlaggning.jaxrsspec.controllers.generatedsource.model.PostUppgifterHandlaggareResponse;

import java.util.UUID;

/**
 * REST controller exposing handläggare uppgifter operations.
 *
 * <p>Handläggare identity is read from the {@code Authorization} header on every request.
 */
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

   @Inject
   IdentityUtil identityUtil;

   @Inject
   RoutingContext routingContext;

   /**
    * Returns tasks assigned to the calling handläggare (identity from authorization header).
    *
    * @return assigned tasks
    */
   @GET
   @Path("/handlaggare")
   @Override
   public GetUppgifterHandlaggareResponse getUppgifterHandlaggare()
   {
      var handlaggare = identityUtil.getIdentity(routingContext.request().getHeader(HttpHeaders.AUTHORIZATION));
      var uppgifter = operativtUppgiftslagerService.getUppgifterHandlaggare(handlaggare.typId(), handlaggare.varde());
      return presentationRestMapper.toGetUppgifterHandlaggareResponse(uppgifter);
   }

   /**
    * Returns all tasks assigned to any team member of the calling handläggare (identity from authorization header).
    *
    * @return team tasks
    */
   @GET
   @Path("/team")
   @Override
   public GetUppgifterHandlaggareResponse getUppgifterTeam()
   {
      var handlaggare = identityUtil.getIdentity(routingContext.request().getHeader(HttpHeaders.AUTHORIZATION));
      var uppgifter = operativtUppgiftslagerService.getUppgifterTeam(handlaggare);
      return presentationRestMapper.toGetUppgifterHandlaggareResponse(uppgifter);
   }

   /**
    * Assigns a new task to the calling handläggare (identity from authorization header).
    *
    * @return the assigned task
    */
   @POST
   @Path("/handlaggare")
   @Override
   public PostUppgifterHandlaggareResponse postUppgifterHandlaggare()
   {
      var handlaggare = identityUtil.getIdentity(routingContext.request().getHeader(HttpHeaders.AUTHORIZATION));
      var uppgift = operativtUppgiftslagerService.assignNewTask(handlaggare.typId(), handlaggare.varde());
      return presentationRestMapper.toPostUppgifterHandlaggareResponse(uppgift);
   }

   /**
    * Reassigns the given uppgift to the calling handläggare (identity from authorization header).
    *
    * @param uppgiftId the ID of the uppgift to reassign
    * @return the updated uppgift
    */
   @POST
   @Path("/{uppgift_id}/handlaggare")
   @Override
   public PostUppgiftHandlaggareResponse postUppgiftHandlaggare(@PathParam("uppgift_id") UUID uppgiftId)
   {
      var handlaggare = identityUtil.getIdentity(routingContext.request().getHeader(HttpHeaders.AUTHORIZATION));
      var uppgift = operativtUppgiftslagerService.reassignUppgift(uppgiftId, handlaggare);
      return presentationRestMapper.toPostUppgiftHandlaggareResponse(uppgift);
   }

   /**
    * Unassigns the given uppgift from the calling handläggare (identity from authorization header).
    *
    * @param uppgiftId the ID of the uppgift to unassign
    */
   @DELETE
   @Path("/{uppgift_id}/handlaggare")
   @Override
   public void deleteUppgiftHandlaggare(@PathParam("uppgift_id") UUID uppgiftId)
   {
      var handlaggare = identityUtil.getIdentity(routingContext.request().getHeader(HttpHeaders.AUTHORIZATION));
      operativtUppgiftslagerService.unassignHandlaggareUppgift(uppgiftId, handlaggare);
   }
}
