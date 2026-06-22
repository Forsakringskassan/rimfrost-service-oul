# Plan: FKPOC-833 — Sno en uppgift från en kollega

A handläggare should be able to claim an uppgift assigned to a colleague in the same team.
Team membership is TBD from a real REST API. This iteration uses a hardcoded team of 3
members so the feature can be built and tested end-to-end without the team API dependency.

---

## Status: ✅ DONE

---

## Requirements addressed

| AC  | Requirement |
|-----|-------------|
| AC1 | `GET /uppgifter/team` → 200 with all uppgifter assigned to any team member (caller identity from bearer token) |
| AC4 | Team members fetched via TeamService — no hardcoded conditions in business logic |
| AC5 | `POST /uppgifter/{uppgift_id}/handlaggare` → 200 with updated OperativUppgift (caller identity from bearer token) |
| AC6 | After reassignment, uppgift appears in new assignee's list and not in old assignee's list |
| AC7 | `POST /uppgifter/{uppgift_id}/handlaggare` → 403 if task's current assignee is outside the team |
| AC8 | No notification to previous assignee (out of scope) |
| AC9 | Team constellation management is out of scope |
| AC10 | A handläggare no longer assigned to an uppgift cannot update it |

## Out of scope

- Real team members REST API (replaced by hardcoded implementation)
- Notification to de-assigned handläggare
- Team constellation administration

---

## Design decisions

**Team service abstraction (AC4)**
`TeamService` is an interface with one method: `isTeamMember(Idtyp)`. The hardcoded
implementation returns true for exactly 3 members, all using `oulHandlaggareTypId` as typId
and three fixed UUID vardes (`TEAM_MEMBER_1`, `TEAM_MEMBER_2`, `TEAM_MEMBER_3` constants).
When the real team API is available, only the implementation changes — all callers stay the same.

These same identities double as test bearer tokens. Tests pass e.g.
`Authorization: Bearer <oulHandlaggareTypId>:<TEAM_MEMBER_1>` — no JWT infrastructure needed.

**Team check strategy (AC7)**
The target handläggare is validated by the security scheme — no separate team check needed
for the target. The only team check at the service level is:
- POST reassignment: is the task's current assignee a team member? No → 403 (AC7).

**AC10 — ex-assignee cannot update**
Enforcement of AC10 is **out of scope for rimfrost-service-oul**. The responsibility lies
in `rimfrost-framework-regel-manuell`, which enforces update permissions on uppgifter via
the regel API. Preventing a de-assigned handläggare from updating requires API changes in
the regel APIs — not in OUL. OUL's only obligation is to correctly reflect the new
assignee after reassignment, which is covered by AC5/AC6. AC10 is verified here by a
read-only test: after reassignment, the old assignee's task list no longer contains the
uppgift.

**No schema changes**
`handlaggar_id_typ_id` and `handlaggar_id_varde` on `uppgift` already exist. No migration.

**Spec update is a prerequisite**
The controller implements a generated interface from `rimfrost-service-oul-openapi`.
The two new operations must be added to the spec and the dependency bumped before the
controller step.

---

## Design *(optional — fill in before implementing non-trivial steps)*

---

## Step 0 — Update OpenAPI spec in rimfrost-service-oul-openapi [x]

**Goal:** Add the two new operations so generated code is available for the controller step.

New operations:
- `GET /uppgifter/team` → response schema `GetUppgifterHandlaggareResponse`; caller identity from bearer token
- `POST /uppgifter/{uppgift_id}/handlaggare` → response schema `PostUppgiftHandlaggareResponse`; caller identity from bearer token

After spec update: bump the dependency version in `pom.xml` and verify compile.

---

## Step 0b — Adapt controller to 2.1.0 interface [x]

**Goal:** Restore compile after the spec bump. The 2.1.0 interface moved handläggare identity
from path params to bearer token for all four operations. This step adapts the existing
methods and adds stubs for the two new ones — no new business logic yet.

**Changes in `presentation/rest/OperativtUppgiftslagerController.java`:**
- `getUppgifterHandlaggare` / `postUppgifterHandlaggare` — remove `{id_typ}/{handlaggar_id}`
  path params; extract `Idtyp` from the `Authorization: Bearer` header instead
- Add stub `getUppgifterTeam()` — throws `UnsupportedOperationException` (implemented in Step 5)
- Add stub `postUppgiftHandlaggare(UUID)` — throws `UnsupportedOperationException` (implemented in Step 5)

**Token format:** bearer token is `<id_typ>:<id_varde>` (e.g. `card:handlaggare1`).
A small `BearerTokenExtractor` helper in the presentation layer parses the header value.

**OulTestBase** — update `getUppgifterHandlaggare` and `postUppgifterHandlaggare` helpers
to pass `Authorization: Bearer card:<handlaggarId>` instead of path params.

---

## Step 1 — Add failing tests [x]

**Goal:** Define the expected behaviour for all ACs before any production code is written.
Tests drive the implementation and confirm when each step is complete.

**File to create:** `src/test/java/se/fk/github/rimfrost/operativt/uppgiftslager/OulTeamTest.java`

**Tests to add:**

| Test | AC |
|------|----|
| `GET /uppgifter/team returns all team members uppgifter` | AC1 |
| `GET /uppgifter/team returns empty list when no team members have tasks` | AC1 |
| `POST /uppgifter/{id}/handlaggare reassigns task and returns 200 with updated uppgift` | AC5 |
| `After reassignment, task appears in new assignee's list` | AC6 |
| `After reassignment, task no longer in old assignee's list` | AC6, AC10 |
| `POST /uppgifter/{id}/handlaggare returns 403 when current assignee is outside team` | AC7 |

Helper methods to add to `OulTestBase`:
- `getTeamTasks(String bearerToken)` — GET /uppgifter/team; token is one of the hardcoded member IDs (e.g. `handlaggare1`)
- `reassignTask(UUID uppgiftId, String bearerToken)` — POST /uppgifter/{uppgift_id}/handlaggare; same token convention

---

## Step 2 — TeamService (hardcoded) [x]

**Goal:** Provide the team membership abstraction so all business logic uses the interface,
not the hardcoded values directly.

**Files to create:**
- `logic/team/TeamService.java` — interface: `boolean isTeamMember(Idtyp handlaggare)`
- `logic/team/HardcodedTeamService.java` — `@ApplicationScoped` implementation returning
  true for `("card","handlaggare1")`, `("card","handlaggare2")`, `("card","handlaggare3")`

**Tests to add:** `HardcodedTeamServiceTest` — unit test covering in-team and out-of-team cases.

---

## Step 3 — Storage: team query [x]

**Goal:** Add the team listing query to support `getUppgifterTeam`.

**Note:** `findUppgiftById` and `updateUppgift` (covers reassignment) already existed —
only `findAllUppgifterByTeam` was added.

**Files changed:**
- `storage/OulDataStorage.java` — added `findAllUppgifterByTeam(List<Idtyp>, SorteringsordningEntity)`
- `storage/internal/PanacheOulDataStorage.java` — implemented the above
- `storage/internal/SorteringsordningQueryBuilder.java` — added `buildTeamListQuery()` using
  a disjunction of `(typ_id = :p AND varde = :p)` OR-predicates with named params (one pair
  per team member) to avoid SQL injection

---

## Step 4 — Service: team methods and team-auth guard [x]

**Goal:** Implement business logic for the three new behaviours.

**File to change:** `logic/OperativtUppgiftslagerService.java`

New methods:
- `getUppgifterTeam(Idtyp callerHandlaggare)` — resolves team members via `TeamService`,
  calls `storage.findAllUppgifterByTeam(...)`, maps to DTOs
- `reassignUppgift(UUID uppgiftId, Idtyp callerHandlaggare)` — fetches current assignee,
  checks current assignee is team member (AC7), calls `storage.reassignUppgift(...)`,
  returns updated DTO

**Exception to add:** `logic/exception/NotTeamMemberException.java` (unchecked)

---

## Step 5 — Controller: new endpoints and 403 mapping [x]

**Goal:** Expose the new service methods as REST endpoints and map `NotTeamMemberException`
to 403.

**File to change:** `presentation/rest/OperativtUppgiftslagerController.java`
- Implement `getUppgifterTeam()` — extract caller `Idtyp` from bearer token, delegate to service, map response
- Implement `postUppgiftHandlaggare(UUID uppgiftId)` — extract caller `Idtyp` from bearer token, delegate to service, map response

**File to create:** `presentation/rest/exception/NotTeamMemberExceptionMapper.java`
- `@Provider` JAX-RS `ExceptionMapper<NotTeamMemberException>` returning HTTP 403

---

## Open questions

- [ ] Confirm HTTP 403 is correct for team-auth failures (AC7)
- [ ] Real team API contract — endpoint, auth, response shape (for when HardcodedTeamService is replaced)
