package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.jboss.resteasy.reactive.ResponseStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.SorteringsordningApi;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningPage;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;

/**
 * REST controller for the {@code /sorteringsordning} resource.
 * Implements the generated {@link SorteringsordningApi} interface from the OpenAPI spec.
 */
@SuppressWarnings("unused")
@ApplicationScoped
@Path("/sorteringsordning")
@Produces("application/json")
@Consumes("application/json")
public class SorteringController implements SorteringsordningApi
{
   @Inject
   OperativtUppgiftslagerService operativtUppgiftslagerService;

   @Inject
   ManagementMapper managementMapper;

   @Override
   @POST
   @ResponseStatus(201)
   public SorteringsordningResponse createSorteringsordning(@Valid @NotNull SorteringsordningSpec sorteringsordningSpec)
   {
      var entity = operativtUppgiftslagerService.createSorteringsordning(sorteringsordningSpec);
      return managementMapper.toSorteringsordningResponse(entity);
   }

   @Override
   @GET
   public SorteringsordningPage getSorteringsordningar(@QueryParam("limit") @NotNull @Min(1) Integer limit,
         @QueryParam("offset") @DefaultValue("0") @Min(0) Integer offset)
   {
      if (limit == null)
      {
         throw new WebApplicationException(Response.Status.BAD_REQUEST);
      }
      var page = operativtUppgiftslagerService.getSorteringsordningarPage(limit, offset != null ? offset : 0);
      return managementMapper.toSorteringsordningPage(page);
   }

   @Override
   @GET
   @Path("/default")
   public SorteringsordningResponse getDefaultSorteringsordning()
   {
      return operativtUppgiftslagerService.getDefaultSorteringsordning()
            .map(managementMapper::toSorteringsordningResponse)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
   }

   @Override
   @GET
   @Path("/{sorteringsordningId}")
   public SorteringsordningResponse getSorteringsordning(@PathParam("sorteringsordningId") UUID sorteringsordningId)
   {
      return operativtUppgiftslagerService.getSorteringsordningById(sorteringsordningId)
            .map(managementMapper::toSorteringsordningResponse)
            .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
   }

   @Override
   @POST
   @Path("/preview")
   public UppgiftPage previewSorteringsordning(@QueryParam("limit") @NotNull @Min(1) Integer limit,
         @Valid @NotNull SorteringsordningSpec sorteringsordningSpec,
         @QueryParam("offset") @DefaultValue("0") @Min(0) Integer offset)
   {
      var page = operativtUppgiftslagerService.previewSorteringsordning(
            sorteringsordningSpec, limit, offset != null ? offset : 0);
      return managementMapper.toUppgiftPage(page);
   }

   /**
    * Deletes the sorteringsordning with the given id.
    * Exceptions mapped by {@link se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.SorteringsordningNotFoundExceptionMapper}
    * and {@link se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.SorteringsordningIsDefaultExceptionMapper}.
    *
    * @param sorteringsordningId the UUID of the sorteringsordning to delete
    */
   @Override
   @DELETE
   @Path("/{sorteringsordningId}")
   @ResponseStatus(204)
   public void deleteSorteringsordning(@PathParam("sorteringsordningId") UUID sorteringsordningId)
   {
      operativtUppgiftslagerService.deleteSorteringsordning(sorteringsordningId);
   }

   /**
    * Sets the given sorteringsordning as the system default.
    * Exceptions mapped by {@link se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.exception.SorteringsordningNotFoundExceptionMapper}.
    *
    * @param sorteringsordningId the UUID of the sorteringsordning to set as default
    */
   @Override
   @PUT
   @Path("/{sorteringsordningId}/default")
   @ResponseStatus(204)
   public void setDefaultSorteringsordning(@PathParam("sorteringsordningId") UUID sorteringsordningId)
   {
      operativtUppgiftslagerService.setDefaultSorteringsordning(sorteringsordningId);
   }
}
