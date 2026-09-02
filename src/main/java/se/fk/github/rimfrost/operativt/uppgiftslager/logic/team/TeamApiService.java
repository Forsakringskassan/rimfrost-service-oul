package se.fk.github.rimfrost.operativt.uppgiftslager.logic.team;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.Idtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.dto.ImmutableIdtyp;
import se.fk.github.rimfrost.operativt.uppgiftslager.logic.exception.NotTeamMemberException;
import se.fk.rimfrost.adapter.team.adapter.TeamAdapter;
import se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.model.Team;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
   private static final Logger log = LoggerFactory.getLogger(TeamApiService.class);

   @Inject
   TeamAdapter teamAdapter;

   @Override
   public List<Idtyp> teamMembers(Idtyp caller)
   {
      var ids = teamIds(caller);
      if (ids.isEmpty())
      {
         throw new NotTeamMemberException();
      }
      return ids.stream()
            .flatMap(this::teamIndivider)
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
    * Returns whether the given handläggare has SID-behörighet. Returns {@code false} if the
    * handläggare is not found (404) — this is indistinguishable from "found, but no SID
    * rights" from the caller's perspective; see {@code callOrNotFound}'s log line if that
    * distinction ever matters for an audit trail.
    *
    * @param handlaggare the handläggare identity
    * @return {@code true} if the handläggare has SID-behörighet
    */
   @Override
   public boolean harSidBehorighet(Idtyp handlaggare)
   {
      return callOrNotFound(
            () -> teamAdapter.hasSidPermission(handlaggare.typId(), handlaggare.varde()),
            false,
            "Handläggare {} not found when checking SID-behörighet; treating as no behörighet",
            handlaggare.varde());
   }

   /**
    * Returns the members of the given team.
    * Returns an empty stream if the team is not found (404) — guards against
    * a race condition where a team ID returned by the individ lookup no longer exists.
    *
    * @param teamId the team ID
    * @return stream of team member identities
    */
   private Stream<se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.model.Idtyp> teamIndivider(Integer teamId)
   {
      List<se.fk.rimfrost.team.jaxrsspec.controllers.generatedsource.model.Idtyp> individer = callOrNotFound(
            () -> teamAdapter.getTeamIndivider(teamId).getIndivider(),
            List.of(),
            "Team {} not found when fetching members; skipping", teamId);
      return individer != null ? individer.stream() : Stream.empty();
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
      return callOrNotFound(
            () -> teamAdapter.getIndividTeam(handlaggare.typId(), handlaggare.varde())
                  .getTeam().stream()
                  .map(Team::getId)
                  .collect(Collectors.toSet()),
            Set.of(),
            "Individ {} not found when fetching teams; treating as no teams", handlaggare.varde());
   }

   /**
    * Calls the team API and returns a fallback value if the target is not found (404),
    * logging a WARN so a data-sync issue ("not found") is distinguishable in the logs from
    * a genuine negative result, even though both resolve to the same fallback here.
    *
    * @param call the team API call to make
    * @param fallback the value to return if the target is not found
    * @param notFoundMessage an SLF4J-style message template for the not-found case
    * @param args arguments for {@code notFoundMessage}
    * @return the call's result, or {@code fallback} if the target was not found
    */
   private <T> T callOrNotFound(Supplier<T> call, T fallback, String notFoundMessage, Object... args)
   {
      try
      {
         return call.get();
      }
      catch (NotFoundException e)
      {
         log.warn(notFoundMessage, args);
         return fallback;
      }
   }
}
