package se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.presentation.rest.ManagementApi;

import java.io.IOException;

@Provider
@Priority(1)
public class GlobalRequestPortFilter implements ContainerRequestFilter
{
   Logger logger = LoggerFactory.getLogger(GlobalRequestPortFilter.class);

   @Inject
   ResourceInfo resourceInfo;

   @ConfigProperty(name = "rimfrost.oul.management.api.port")
   int managementApiPort;

   @Override
   public void filter(ContainerRequestContext requestContext) throws IOException
   {
      logger.info("Resource method: " + resourceInfo.getResourceMethod().getName());
      var requestMethod = resourceInfo.getResourceMethod();
      var requestClass = resourceInfo.getResourceClass();

      ManagementApi requestClassAnnotation = requestClass.getAnnotation(ManagementApi.class);
      ManagementApi requestMethodAnnotation = requestMethod.getAnnotation(ManagementApi.class);
      var managementApiEndpoint = requestClassAnnotation != null || requestMethodAnnotation != null;

      logger.info("requestClassAnnotation: {}", requestClassAnnotation);
      logger.info("requestMethodAnnotation: {}", requestMethodAnnotation);
      logger.info("Is management api endpoint? {}", managementApiEndpoint);
      logger.info("Request port: {}", requestContext.getUriInfo().getBaseUri().getPort());

      if ((managementApiEndpoint && requestContext.getUriInfo().getBaseUri().getPort() != managementApiPort)
            || (!managementApiEndpoint && requestContext.getUriInfo().getBaseUri().getPort() == managementApiPort))
      {
         throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
      }
   }
}
