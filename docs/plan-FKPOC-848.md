# Plan: FKPOC-848 — DB persistence for SorteringsordningEntity

Follow-up to FKPOC-795. Replaces the `AtomicReference` in-memory storage with
PostgreSQL persistence so sorteringsordningar survive restarts.

Uppgifter are **already persisted** via `UppgiftRepository` (Panache). The only remaining
in-memory piece was `activeSorteringsordning` in `PanacheOulDataStorage`.

---

## Status: ✅ COMPLETE

All four steps delivered. See commit history on `fix/FKPOC-848-sortorder-db`.

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

Two tables: `sorteringsordning` holds all stored orders; `default_sorteringsordning`
holds exactly one row — the ID of the current default.

The `lock = TRUE` primary key on `default_sorteringsordning` ensures only one row can ever
be inserted. The FK to `sorteringsordning` acts as a second line of defence: the DB rejects
deleting the current default even if the application-level 409 check is bypassed.

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

## Step 1 — Fix and add tests ✅ DONE

Fixed `should_replace_sorteringsordning_on_second_post` (asserted old wrong behaviour).
Added `OUL-FR-09.2` and `OUL-FR-11.2` tests.

---

## Step 2 — Infrastructure ✅ DONE

Files created:

- `storage/internal/entity/SorteringsordningPersistenceEntity.java`
- `storage/internal/entity/DefaultSorteringsordningEntity.java`
- `storage/internal/repository/SorteringsordningRepository.java`
- `storage/internal/repository/DefaultSorteringsordningRepository.java`
- `storage/internal/converter/SorteringsordningEntriesConverter.java`
- `storage/internal/converter/SorteringsordningEntryDeserializer.java` ← **unplanned, see deviation below**
- `src/main/resources/db/migration/V002__add_sorteringsordning_table.sql`

**Deviation from plan:** The plan assumed `@JsonSubTypes` on `Constraint` would handle
polymorphic deserialisation automatically. This was wrong: the generated OpenAPI model
does not establish Java inheritance between `Constraint` and its subtypes (`ConstraintEq`,
`ConstraintBetween`, `ConstraintContains`, `ConstraintOffsetToNow`), so Jackson's standard
type dispatch cannot be used.

A custom `SorteringsordningEntryDeserializer` was introduced to dispatch on the `operator`
field manually. The resulting constraint list is built as a raw `ArrayList` and cast via
`(List<Constraint>)(List<?>)` to avoid a JVM `checkcast` that would fail at runtime.

Also added `SorteringsordningEntriesConverterTest` with round-trip tests for all four
constraint subtypes.

---

## Step 3 — Replace `AtomicReference` with DB ✅ DONE

Wired `PanacheOulDataStorage` to use the new repositories. Removed `AtomicReference`.

**Race condition fix (unplanned):** The original plan used a check-then-insert for the
default row. Two concurrent `POST /sorteringsordning` requests could both observe an empty
table and both attempt an insert, with the second failing on the PK constraint.

Fixed with `DefaultSorteringsordningRepository.insertIfAbsent(UUID)`:

```sql
INSERT INTO {h-schema}default_sorteringsordning (lock, sorteringsordning_id)
VALUES (TRUE, :id) ON CONFLICT DO NOTHING
```

The `{h-schema}` Hibernate placeholder is required for native queries — unlike JPQL,
native SQL does not automatically apply `quarkus.hibernate-orm.database.default-schema`.

`setCreatedAt` was added to `SorteringsordningPersistenceEntity` and `@PrePersist`
was updated to only set `createdAt` if null, so the service layer can supply the original
creation timestamp from the domain object.

**Requirements delivered:** OUL-FR-09.2, OUL-NFR-04.1, OUL-NFR-05.1, OUL-NFR-05.2

---

## Step 4 — Delete and set-default ✅ DONE

**Requirements delivered:** OUL-FR-13, OUL-FR-14, OUL-NFR-05.3

Files changed:

| File | Change |
|------|--------|
| `storage/OulDataStorage.java` | Added `deleteSorteringsordning` and `setDefaultSorteringsordning` |
| `storage/internal/PanacheOulDataStorage.java` | Implemented both methods |
| `logic/OperativtUppgiftslagerService.java` | Delegation methods added |
| `presentation/rest/management/SorteringController.java` | Stubs replaced with real implementations |

Tests added: OUL-FR-13.1, FR-13.2, FR-13.3, FR-14.1, FR-14.2 in `OulSorteringTest`;
FR-03.6 + FR-14 interaction test in `OulUppgifterSortTest`.

---

## Post-implementation additions

- **Javadoc** added to all Java classes and methods introduced in the branch, using American English spelling throughout.
