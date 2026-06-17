# Plan: FKPOC-795 — Sorteringsordning utan persistence

## Requirements Summary

- **In-memory only** — sort order does NOT need to survive restarts (REQ-OUL-SORT-005 relaxed)
- **Single active sort order** — creating a new one replaces the existing one
- **Always default** — the single sort order is always the default, assigned a new UUID each time
- `GET /sorteringsordning` returns a list of zero or one item

---

## New / Updated Endpoints

| Endpoint | Method | Required | Notes |
|----------|--------|----------|-------|
| `POST /sorteringsordning` | POST | ✅ Required | Creates/replaces, always default |
| `GET /sorteringsordning` | GET | Optional | Returns 0 or 1 item |
| `GET /sorteringsordning/default` | GET | ✅ Required | 404 if none exists |
| `GET /sorteringsordning/{id}` | GET | Optional | 404 if ID doesn't match current |
| `POST /sorteringsordning/preview` | POST | Optional | Useful for testing |
| `PUT /sorteringsordning/{id}/default` | PUT | ❌ Not needed | Can return 204 no-op |
| `DELETE /sorteringsordning/{id}` | DELETE | ❌ Not needed | Can return 405 |
| `GET /uppgifter` | GET | ✅ Updated | New signature (see below) |
| `PATCH /uppgifter/{id}` | PATCH | ⚠️ New in 1.2.0 | `updateUppgift` — not in original plan |
| `POST /uppgifter/{id}/unassign` | POST | ⚠️ New in 1.2.0 | `unassignUppgift` — not in original plan |

**Updated `GET /uppgifter` signature:**
- Adds: `sorteringsordningId` (UUID, optional), `limit` (integer, required, min 1), `offset` (integer, optional, default 0)
- Returns: `UppgiftPage { total: integer, items: array[OperativUppgift] }` instead of plain list
- **Breaking change** — existing callers must supply `limit`

---

## New Schemas in OpenAPI

- **`UppgiftPage`** — `total` + `items[]`
- **`SorteringsordningSpec`** — `entries[]` (minItems: 1)
- **`SorteringsordningEntry`** — `constraints[]?` + `sort_by?`
- **`Constraint`** (discriminator union) — `ConstraintEq`, `ConstraintContains`, `ConstraintBetween`, `ConstraintOffsetToNow`
- **`SortBy`** — `field: SorteringsordningField` + `direction: asc|desc`
- **`SorteringsordningResponse`** — `id`, `skapad`, `entries[]`

All schemas are fully generated. Generated sources are in
`rimfrost-service-oul-management-openapi/rimfrost-service-oul-management-api-jaxrs-spec/build/generated-source/`
under package `se.fk.rimfrost.oul.management.jaxrsspec.controllers.generatedsource`.

### Sortable / Filterable Fields

These enums are generated from the spec and scope what `ConstraintMatcher` and `SortOrderApplier` must handle:

**`SorteringsordningFieldDate`** (supports `between`, `offset_to_now`, and date sorting):
- `skapad`
- `planerad_till`

**`SorteringsordningFieldString`** (supports `eq`, `contains`):
- `status`, `regel`, `roll`, `verksamhetslogik`, `beskrivning`

Note: `planeradTill` was added to the domain (`UppgiftDto`) and the asyncapi status message in the
main-branch commit `831a03d` (asyncapi bump 1.0.5 → 1.0.6). It is already a `LocalDate` field on
`UppgiftDto` and on the generated `OperativUppgift` model — `ManagementMapper` must map it.

---

## Spec Fix: Split `DefaultApi` ✅ Done

The management openapi spec was updated (v1.3.0 → v1.3.1) to split `DefaultApi` into two
tagged interfaces using OpenAPI `tags`:

- `UppgifterApi` — covers all `/uppgifter` endpoints, generates `@Path("/uppgifter")`
- `SorteringsordningApi` — covers all `/sorteringsordning` endpoints, generates `@Path("/sorteringsordning")`

`ManagementController` was renamed to `UppgifterController implements UppgifterApi`, and
`SorteringController implements SorteringsordningApi`, restoring the standard rimfrost pattern.

---

## Implementation Priority

### Tier 0–2 — Fas 1 ✅ Complete

### Out-of-scope for FKPOC-795
1. Database persistence
2. `PUT /default`, `DELETE /{id}` with real semantics

---

## Key Design Decisions

- `ConstraintMatcher.matches(UppgiftDto, List<?>)` — accepts `List<?>` because the generated
  `Constraint` class has no Java inheritance relation with `ConstraintEq` etc. (Jackson polymorphism
  only). Dispatches via `instanceof` pattern matching.
- `SortOrderApplier` returns `SortedUppgiftPage(int total, List<UppgiftDto> items)` — a logic-layer
  record, not the API model. The controller maps it to the generated `UppgiftPage`.
- `SortOrderApplier` is a CDI bean with constructor injection, also directly instantiable for unit tests.
- Unmatched tasks (no entry matches) are appended last in original insertion order.
