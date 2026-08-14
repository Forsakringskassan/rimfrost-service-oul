# Plan — FKPOC-866: Team data via API

## Background

Two endpoints already exist in the controller and service:
- `GET /uppgifter/team` — returns tasks assigned to any member of the caller's team
- `POST /uppgifter/{uppgift_id}/handlaggare` — reassigns a task to the caller (requires same-team check)

Both rely on `TeamService`, currently implemented by `HardcodedTeamService` (three hardcoded members).

This ticket replaces the hardcoded implementation with a real call to `rimfrost-service-team-openapi`.

Two other gaps to close:
1. `TeamService` interface methods have no caller context — needed by the real API
2. `reassignUppgift()` does not publish a Kafka notification (required per OUL-FR-18.5)

## Team API

Base URL: `http://rimfrost-k8s-team:8080` (to be confirmed)

| Endpoint | Purpose |
|----------|---------|
| `GET /individ/{idTyp}/{idVarde}/team` | Returns the teams the caller belongs to (list of `{id, namn, kontor}`) |
| `GET /team/{teamId}/individer` | Returns all members of a team (list of `{typId, varde}`) |

---

## Step 1 — Refactor `TeamService` interface to accept caller context

**Why:** `teamMembers()` and `isTeamMember(other)` have no caller parameter — the real API needs to look up the caller's teams first.

### Design

```java
interface TeamService {
    /** Returns all members of the caller's team(s). */
    List<Idtyp> teamMembers(Idtyp caller);

    /**
     * Returns true if caller and other share at least one team.
     * Used for reassignment authorization.
     */
    boolean isSameTeam(Idtyp caller, Idtyp other);
}
```

Update `HardcodedTeamService`:
- `teamMembers(Idtyp caller)` — ignore caller, return hardcoded list (POC)
- `isSameTeam(Idtyp caller, Idtyp other)` — return `TEAM_SET.contains(caller) && TEAM_SET.contains(other)`

Update `OperativtUppgiftslagerService`:
- `getUppgifterTeam(callerHandlaggare)`: `teamService.teamMembers(callerHandlaggare)` (was `teamService.teamMembers()`)
- `reassignUppgift(uppgiftId, callerHandlaggare)`: `teamService.isSameTeam(callerHandlaggare, current.handlaggarId())` (was `teamService.isTeamMember(current.handlaggarId())`)

---

## Step 2 — Add team OpenAPI dependency ✅

Added `rimfrost-service-team-openapi-jaxrs-spec` (version 0.0.1) to `pom.xml`.

Initial plan used `@RegisterRestClient` (MicroProfile REST client), but this was dropped in favour of the Jersey `ClientBuilder` + `WebResourceFactory` pattern used by `HandlaggningAdapter` and `SidAdapter` — see step 3.

---

## Step 3 — Implement `TeamAdapter` and `TeamApiService` ✅

**Deviation from plan:** Instead of `TeamApiClient` (`@RegisterRestClient`) + keeping `HardcodedTeamService`, implemented using the Jersey adapter pattern for consistency with existing adapters.

- `TeamAdapter` (`integration/team/TeamAdapter.java`) — Jersey `ClientBuilder` + `Apache5ConnectorProvider` + `WebResourceFactory.newResource(TeamControllerApi.class, ...)`, configured via `team.api.base-url`.
- `TeamApiService` (`logic/team/TeamApiService.java`) — plain `@ApplicationScoped`, injects `TeamAdapter`. `teamMembers()` fans out team IDs to member lookups; `isSameTeam()` computes the set intersection. `NotFoundException` from the team API is caught in `teamIds()` and treated as empty.
- `HardcodedTeamService`, `TeamApiClient`, and `HardcodedTeamServiceTest` deleted.
- `application.properties`: added `team.api.base-url` / `%dev.team.api.base-url`; removed `quarkus-rest-client-jackson` dependency.
- WireMock stubs added: `get-individ-team-member.json`, `get-individ-team-unknown.json`, `get-team-individer.json`.
- **Note:** generated `TeamControllerApi.Idtyp.getTypId()` returns `UUID`, not `String` — required `.toString()` in the mapper and UUID-format `typId` (`4c34906c-03d9-425f-9a1a-062ef6eb88c7`) throughout tests.

---

## Step 4 — Add Kafka notification to `reassignUppgift()` ✅

Added `notifyStatusUpdate(updated)` call in `OperativtUppgiftslagerService.reassignUppgift()` after the storage update, consistent with `assignTask()`, `unassignTask()`, and `updateTask()`.

Also fixed a latent bug: `getUppgifterTeam()` now short-circuits and returns an empty list when `teamMembers()` returns empty, preventing an invalid `IN ()` SQL clause in `buildTeamListQuery`.

---

## Step 5 — Tests ✅

Added to `OulTeamTest` (12 tests total):

- `reassignTask_publishesKafkaStatusNotification` — verifies Kafka message content (uppgiftId, handlaggningId, status, utforarId) after reassignment (OUL-FR-18.5).
- `getTeamTasks_returnsEmptyList_whenCallerHasNoTeam` — caller with no team gets empty result even when tasks are assigned to team members; also exercises the empty-team short-circuit.
- `getTeamTasks_returns500_whenTeamApiUnavailable` — team API 500 propagates as OUL 500.
- `reassignTask_returns500_whenTeamApiUnavailable` — team API 500 during `isSameTeam` propagates as OUL 500.
- `reassignTask_returns403_whenTeamApiReturns404ForCurrentAssignee` — team API 404 for the current assignee is caught, treated as "not in team", returns 403 (exercises the `NotFoundException` catch path in `teamIds()`).

Added `getTeamTasks(UUID, int expectedStatus)` helper to `OulTestBase`.

All 141 tests pass.
