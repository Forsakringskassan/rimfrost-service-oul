package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.management;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import org.jboss.resteasy.reactive.ResponseStatus;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.OperativtUppgiftslagerService;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.SorteringsordningApi;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningResponse;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.SorteringsordningSpec;
import se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource.model.UppgiftPage;

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
   public SorteringsordningResponse createSorteringsordning(@NotNull SorteringsordningSpec sorteringsordningSpec)
   {
      var entity = operativtUppgiftslagerService.createSorteringsordning(sorteringsordningSpec);
      return managementMapper.toSorteringsordningResponse(entity);
   }

   @Override
   @GET
   public List<SorteringsordningResponse> getSorteringsordningar()
   {
      return operativtUppgiftslagerService.getAllSorteringsordningar().stream()
            .map(managementMapper::toSorteringsordningResponse)
            .toList();
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
         @NotNull SorteringsordningSpec sorteringsordningSpec,
         @QueryParam("offset") @Min(0) Integer offset)
   {
      var page = operativtUppgiftslagerService.previewSorteringsordning(
            sorteringsordningSpec, limit, offset != null ? offset : 0);
      return managementMapper.toUppgiftPage(page);
   }

   @Override
   @DELETE
   @Path("/{sorteringsordningId}")
   public void deleteSorteringsordning(@PathParam("sorteringsordningId") UUID sorteringsordningId)
   {
      throw new WebApplicationException(Response.Status.METHOD_NOT_ALLOWED);
   }

   @Override
   @PUT
   @Path("/{sorteringsordningId}/default")
   public void setDefaultSorteringsordning(@PathParam("sorteringsordningId") UUID sorteringsordningId)
   {
      // no-op — single active sort order is always default
   }
}
