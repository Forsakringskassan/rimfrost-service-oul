package se.fk.github.rimfrost.operativt.uppgiftslager.integration.team;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.TeamControllerApi;
import se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.model.GetBehorigheterResponse;
import se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.model.GetIndividTeamResponse;
import se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.model.GetTeamMembersResponse;

/**
 * HTTP adapter for the team API, using the Jersey proxy client pattern.
 *
 * <p>Configured via {@code team.api.base-url}.
 */
@ApplicationScoped
public class TeamAdapter
{
   @ConfigProperty(name = "team.api.base-url")
   String teamBaseUrl;

   private TeamControllerApi teamClient;

   private Client client;

   /**
    * Initialises the Jersey HTTP client and the team API proxy.
    */
   @PostConstruct
   void init()
   {
      ClientConfig clientConfig = new ClientConfig();
      clientConfig.connectorProvider(new Apache5ConnectorProvider());
      this.client = ClientBuilder.newClient(clientConfig);
      this.teamClient = WebResourceFactory.newResource(TeamControllerApi.class, client.target(this.teamBaseUrl));
   }

   /**
    * Cleans up the Jersey client on bean destruction.
    */
   @PreDestroy
   void destroy()
   {
      this.teamClient = null;
      if (this.client != null)
      {
         this.client.close();
         this.client = null;
      }
   }

   /**
    * Returns the teams that the given individ belongs to.
    *
    * @param idTyp   the identity type
    * @param idVarde the identity value
    * @return response containing the list of teams
    * @throws NotFoundException      if the individ is not found
    * @throws ProcessingException    if the team service is unreachable
    * @throws WebApplicationException for other HTTP errors
    */
   public GetIndividTeamResponse getIndividTeam(String idTyp, String idVarde)
   {
      return teamClient.getIndividTeam(idTyp, idVarde);
   }

   /**
    * Returns all members of the given team.
    *
    * @param teamId the team's unique ID
    * @return response containing the list of team members
    * @throws NotFoundException      if the team is not found
    * @throws ProcessingException    if the team service is unreachable
    * @throws WebApplicationException for other HTTP errors
    */
   public GetTeamMembersResponse getTeamIndivider(Integer teamId)
   {
      return teamClient.getTeamIndivider(teamId);
   }

   /**
    * Returns the behörigheter that the given handläggare has.
    *
    * @param idTyp   the identity type
    * @param idVarde the identity value
    * @return response containing the list of behörigheter
    * @throws NotFoundException      if the handläggare is not found
    * @throws ProcessingException    if the team service is unreachable
    * @throws WebApplicationException for other HTTP errors
    */
   public GetBehorigheterResponse getBehorigheter(String idTyp, String idVarde)
   {
      return teamClient.getBehorigheter(idTyp, idVarde);
   }
}
