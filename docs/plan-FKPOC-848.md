# Plan: FKPOC-848 — DB persistence for SorteringsordningEntity

Follow-up to FKPOC-795. Replaces the `AtomicReference` in-memory storage with
PostgreSQL persistence so sorteringsordningar survive restarts.

Uppgifter are **already persisted** via `UppgiftRepository` (Panache). The only remaining
in-memory piece is `activeSorteringsordning` in `PanacheOulDataStorage`.

---

## Requirements addressed

| Krav | Fas 1 | FKPOC-848 |
|------|-------|-----------|
| OUL-FR-09.2 — default vid skapelse | Ej implementerat | Automatisk default om ingen finns sedan tidigare |
| OUL-FR-13 — Ta bort sorteringsordning | Ej implementerat (stub: 405) | DELETE med 409-skydd på default |
| OUL-FR-14 — Ange default sorteringsordning | Ej implementerat (stub: no-op) | PUT anger ny default |
| OUL-NFR-04.1 — persistens | In-memory, överlever inte omstart | Persistent i PostgreSQL |
| OUL-NFR-05.1 — flera sorteringsordningar | En åt gången (in-memory) | Flera sparas; exakt en är default åt gången |
| OUL-NFR-05.2 — hämtning via ID | Ej tillämpbart | `findByIdOptional(id)` direkt mot posten |
| OUL-NFR-05.3 — byte av default | Ej tillämpbart | Single-row UPDATE på `default_sorteringsordning`; berör inga andra poster |

---

## Schema design

Two tables are used: `sorteringsordning` holds all stored orders; `default_sorteringsordning`
holds exactly one row — the ID of the current default.

This satisfies **OUL-NFR-05.3** structurally: changing the default is a single `UPDATE` on
`default_sorteringsordning` and touches no rows in `sorteringsordning`.

The `lock = TRUE` primary key on `default_sorteringsordning` ensures only one row can ever
be inserted. The FK to `sorteringsordning` prevents setting a non-existent ID as default and
prevents deleting the current default without first re-pointing the FK.

```sql
CREATE TABLE sorteringsordning (
    id          UUID        NOT NULL PRIMARY KEY,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    entries     TEXT        NOT NULL
);

CREATE TABLE default_sorteringsordning (
    lock                 BOOLEAN NOT NULL DEFAULT TRUE PRIMARY KEY,
    sorteringsordning_id UUID    NOT NULL REFERENCES sorteringsordning(id),
    CONSTRAINT one_row CHECK (lock = TRUE)
);
```

Note: `is_default` is **not** a column on `sorteringsordning`. The default is tracked
exclusively in `default_sorteringsordning`.

---

## Pattern in this service

Existing `UppgiftEntity` establishes the pattern to follow:

- Plain JPA `@Entity` (not Panache) with `@Id`, `@Version`, `@PrePersist`/`@PreUpdate`
- Repository: `@ApplicationScoped class Foo implements PanacheRepositoryBase<FooEntity, UUID>`
- Storage interface stays unchanged; only `PanacheOulDataStorage` changes
- Complex list fields normalised into join tables (`@OneToMany`) — see `UppgiftIndividEntity`

`List<SorteringsordningEntry>` is **not** a candidate for normalisation: the entries contain
a polymorphic `Constraint` union (`@JsonSubTypes`) and a nested `SortBy`. The correct approach
is an `AttributeConverter` storing the list as a PostgreSQL `TEXT` column serialised by Jackson.
This is a deviation from the join-table pattern but justified because the entries are an opaque,
generated API structure not queried column-by-column.

---

## Step 1 — Fix and add tests (red)

Establish the correct expected behaviour in tests before touching any implementation.
Tests will fail (red) until Step 3 completes.

**Fix existing test** — `should_replace_sorteringsordning_on_second_post` asserts the old
wrong behaviour (second create replaces default) and must be replaced:

```
OUL-FR-09.2: Första skapelsen blir default; andra ändrar inte default
  - create first → getDefault returns first ID
  - create second → getDefault still returns first ID
```

**New test:**

```
OUL-FR-11.2: Lista sorteringsordningar — två poster efter två skapelser
  - create twice → list has size 2 containing both IDs
```

---

## Step 2 — Infrastructure (no behaviour change)

Create the DB schema and Java plumbing. Nothing is wired up yet — tests remain red from Step 1.

**Files to create:**

- `storage/internal/entity/SorteringsordningPersistenceEntity.java`
- `storage/internal/entity/DefaultSorteringsordningEntity.java`
- `storage/internal/repository/SorteringsordningRepository.java`
- `storage/internal/repository/DefaultSorteringsordningRepository.java`
- `storage/internal/converter/SorteringsordningEntriesConverter.java`
- `src/main/resources/db/migration/V002__add_sorteringsordning_table.sql`

### `SorteringsordningPersistenceEntity`

```java
@Entity
@Table(name = "sorteringsordning")
public class SorteringsordningPersistenceEntity
{
   @Id
   @Column(nullable = false)
   private UUID id;

   @Version
   private long version;

   @Column(nullable = false, updatable = false)
   private Instant createdAt;

   @Column(nullable = false)
   private Instant updatedAt;

   @Column(nullable = false, columnDefinition = "text")
   @Convert(converter = SorteringsordningEntriesConverter.class)
   private List<SorteringsordningEntry> entries;

   @PrePersist void onCreate() { createdAt = updatedAt = Instant.now(); }
   @PreUpdate  void onUpdate() { updatedAt = Instant.now(); }

   // getters/setters
}
```

### `DefaultSorteringsordningEntity`

```java
@Entity
@Table(name = "default_sorteringsordning")
public class DefaultSorteringsordningEntity
{
   @Id
   @Column(nullable = false)
   private boolean lock = true;   // always TRUE — enforces single-row via PK

   @Column(name = "sorteringsordning_id", nullable = false)
   private UUID sorteringsordningId;

   // getters/setters
}
```

### Repositories

```java
@ApplicationScoped
public class SorteringsordningRepository
      implements PanacheRepositoryBase<SorteringsordningPersistenceEntity, UUID> {}

@ApplicationScoped
public class DefaultSorteringsordningRepository
      implements PanacheRepositoryBase<DefaultSorteringsordningEntity, Boolean> {}
```

### `SorteringsordningEntriesConverter`

```java
@Converter
public class SorteringsordningEntriesConverter
      implements AttributeConverter<List<SorteringsordningEntry>, String>
{
   private static final ObjectMapper MAPPER = JsonMapper.builder()
         .addModule(new JavaTimeModule())
         .build();
   private static final TypeReference<List<SorteringsordningEntry>> TYPE =
         new TypeReference<>() {};

   @Override
   public String convertToDatabaseColumn(List<SorteringsordningEntry> entries) { ... }

   @Override
   public List<SorteringsordningEntry> convertToEntityAttribute(String json) { ... }
}
```

`@JsonSubTypes` on `Constraint` is intrinsic to Jackson and requires no extra registration —
`ConstraintEq` / `ConstraintBetween` etc. deserialise correctly from the stored JSON.

### Migration

```sql
CREATE TABLE sorteringsordning (
    id          UUID        NOT NULL PRIMARY KEY,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    entries     TEXT        NOT NULL
);

CREATE TABLE default_sorteringsordning (
    lock                 BOOLEAN NOT NULL DEFAULT TRUE PRIMARY KEY,
    sorteringsordning_id UUID    NOT NULL REFERENCES sorteringsordning(id),
    CONSTRAINT one_row CHECK (lock = TRUE)
);
```

---

## Step 3 — Replace `AtomicReference` with DB

Wire `PanacheOulDataStorage` to use the new repositories. Step 1 tests go green.

**Requirements delivered:** OUL-FR-09.2, OUL-NFR-04.1, OUL-NFR-05.1, OUL-NFR-05.2

### `storage/internal/PanacheOulDataStorage.java`

Remove `AtomicReference<SorteringsordningEntity> activeSorteringsordning`; inject
`SorteringsordningRepository` and `DefaultSorteringsordningRepository`.

| Method | Change |
|--------|--------|
| `saveSorteringsordning` | `persist(toEntity(entity))`. If `default_sorteringsordning` is empty, insert a row pointing to the new ID (first-created becomes default, OUL-FR-09.2) |
| `getDefaultSorteringsordning` | Read `default_sorteringsordning`; if present, `findById(id)`; return mapped entity or empty |
| `getSorteringsordningById` | `findByIdOptional(id).map(fromEntity)` |
| `getAllSorteringsordningar` | `findAll().stream().map(fromEntity).toList()` |
| `clearSorteringsordning` | `deleteAll()` on default table first, then sorteringsordning table (test helper only; FK order) |

Also add `toEntity` / `fromEntity` private helpers.

---

## Step 4 — Delete and set-default

Add the two new operations end-to-end through all layers.

**Requirements delivered:** OUL-FR-13, OUL-FR-14, OUL-NFR-05.3

### Tests — write first (TDD)

```
OUL-FR-13.1: Ta bort sorteringsordning — lyckas (204)
  - create first (auto-default), create second, setDefault(second), delete first → 204

OUL-FR-13.2: Ta bort sorteringsordning — HTTP 409 om det är default
  - create → delete → 409

OUL-FR-13.3: Ta bort sorteringsordning — HTTP 404 om id inte finns
  - delete random UUID → 404

OUL-FR-14.1: Ange default — lyckas och getDefault returnerar ny default
  - create first (auto-default), create second, setDefault(second) → getDefault returns second ID

OUL-FR-14.2: Ange default — HTTP 404 om id inte finns
  - setDefault random UUID → 404

OUL-FR-13 + OUL-FR-14 — Ta bort tidigare default efter att ny har satts (interaktionstest)
  - create first (auto-default), create second, setDefault(second), delete first → 204
  - (this is already covered by OUL-FR-13.1 above — keep as one combined test)
```

Note: the interaction test (OUL-FR-13.1) is the most important — it exercises the FK guard path
and validates the full lifecycle. The 409 test (OUL-FR-13.2) is the simplest failure case.

### `storage/OulDataStorage.java` (interface)

```java
void deleteSorteringsordning(UUID id);          // throws if default; throws if not found
void setDefaultSorteringsordning(UUID id);      // throws if not found
```

### `storage/internal/PanacheOulDataStorage.java`

| Method | Change |
|--------|--------|
| `deleteSorteringsordning` | 404 if not found; 409 if `default_sorteringsordning` points to this ID; otherwise `deleteById(id)` |
| `setDefaultSorteringsordning` | 404 if ID not in `sorteringsordning`; upsert `default_sorteringsordning` with the new ID |

### `logic/OperativtUppgiftslagerService.java`

```java
public void deleteSorteringsordning(UUID id) { storage.deleteSorteringsordning(id); }
public void setDefaultSorteringsordning(UUID id) { storage.setDefaultSorteringsordning(id); }
```

### `presentation/rest/management/SorteringController.java`

Replace stubs with real implementations:

- `deleteSorteringsordning`: call service; map not-found to 404, is-default to 409
- `setDefaultSorteringsordning`: call service; map not-found to 404

### New tests

- OUL-FR-13: delete succeeds (204); delete non-existent (404); delete current default (409)
- OUL-FR-14: set default succeeds (204); set default for non-existent ID (404)

---

## Effort summary

| Item | Step | New LOC | Changed LOC |
|------|------|---------|-------------|
| `OulSorteringTest` (fix + 1 new test) | 1 | ~15 | ~10 |
| `SorteringsordningPersistenceEntity` | 2 | ~45 | — |
| `DefaultSorteringsordningEntity` | 2 | ~20 | — |
| `SorteringsordningRepository` | 2 | ~8 | — |
| `DefaultSorteringsordningRepository` | 2 | ~8 | — |
| `SorteringsordningEntriesConverter` | 2 | ~30 | — |
| `V002__add_sorteringsordning_table.sql` | 2 | ~15 | — |
| `PanacheOulDataStorage` (existing ops) | 3 | — | ~35 |
| `OulSorteringTest` (5 new tests) | 4 | ~60 | — |
| `PanacheOulDataStorage` (delete/setDefault) | 4 | — | ~20 |
| `OulDataStorage` (interface) | 4 | — | ~5 |
| `OperativtUppgiftslagerService` | 4 | — | ~10 |
| `SorteringController` | 4 | — | ~15 |
| **Total** | | **~201** | **~95** |

---

## Notes

- `SorteringsordningEntity` stays as a domain record — no JPA annotations leak into the logic layer. No `isDefault` field needed since `getDefaultSorteringsordning()` already expresses that relationship by what it returns.
- The `TEXT` column avoids a `jsonb` Hibernate dialect dependency. Upgrade to `jsonb` later if PostgreSQL JSON querying is ever needed.
- The FK in `default_sorteringsordning` acts as a second line of defence: the DB will reject deleting the current default even if the application-level 409 check is bypassed.
- Exception types for "not found" and "is default" from the storage layer should be defined or reused consistently so `SorteringController` can map them to the correct HTTP status codes.
