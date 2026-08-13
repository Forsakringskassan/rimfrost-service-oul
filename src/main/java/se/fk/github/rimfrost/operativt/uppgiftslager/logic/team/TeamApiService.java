package se.fk.github.rimfrost.operativt.uppgiftslager.logic.team;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import se.fk.github.rimfrost.operativt.uppgiftslager.integration.team.TeamAdapter;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.rimfrost.jaxrsspec.controllers.generatedsource.model.Team;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link TeamService} implementation backed by the team API.
 *
 * <p>Resolves team membership by calling {@code GET /individ/{idTyp}/{idVarde}/team}
 * to retrieve the caller's teams, then {@code GET /team/{teamId}/individer} to fetch
 * each team's members. Results are aggregated and deduplicated.
 *
 * <p>All lookups are performed at call time with no caching, per OUL-FR-16.2.
 */
@ApplicationScoped
public class TeamApiService implements TeamService
{
   @Inject
   TeamAdapter teamAdapter;

   @Override
   public List<Idtyp> teamMembers(Idtyp caller)
   {
      return teamIds(caller).stream()
            .flatMap(teamId -> teamAdapter.getTeamIndivider(teamId).getIndivider().stream())
            .map(i -> (Idtyp) ImmutableIdtyp.builder()
                  .typId(i.getTypId().toString())
                  .varde(i.getVarde())
                  .build())
            .distinct()
            .toList();
   }

   @Override
   public boolean isSameTeam(Idtyp caller, Idtyp other)
   {
      var callerTeamIds = teamIds(caller);
      if (callerTeamIds.isEmpty())
      {
         return false;
      }
      var otherTeamIds = teamIds(other);
      return callerTeamIds.stream().anyMatch(otherTeamIds::contains);
   }

   /**
    * Returns the set of team IDs that the given handläggare belongs to.
    * Returns an empty set if the individ is not found (404).
    *
    * @param handlaggare the handläggare identity
    * @return set of team IDs
    */
   private Set<Integer> teamIds(Idtyp handlaggare)
   {
      try
      {
         return teamAdapter.getIndividTeam(handlaggare.typId(), handlaggare.varde())
               .getTeam().stream()
               .map(Team::getId)
               .collect(Collectors.toSet());
      }
      catch (NotFoundException e)
      {
         return Set.of();
      }
   }
}
