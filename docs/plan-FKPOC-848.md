# Plan: DB persistence for SorteringsordningEntity

Follow-up to FKPOC-795 (Fas 1). Replaces the `AtomicReference` in-memory storage with
PostgreSQL persistence so the active sorteringsordning survives restarts.

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

## Files to create

### `storage/internal/entity/SorteringsordningPersistenceEntity.java`

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

### `storage/internal/repository/SorteringsordningRepository.java`

```java
@ApplicationScoped
public class SorteringsordningRepository
      implements PanacheRepositoryBase<SorteringsordningPersistenceEntity, UUID>
{
}
```

### `storage/internal/converter/SorteringsordningEntriesConverter.java`

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

---

## Files to modify

### `storage/internal/PanacheOulDataStorage.java`

Replace `AtomicReference<SorteringsordningEntity>` with injected `SorteringsordningRepository`.

| Method | Change |
|--------|--------|
| `saveSorteringsordning` | `deleteAll()` then `persist(toEntity(entity))` |
| `getDefaultSorteringsordning` | `findAll().firstResult().map(fromEntity)` |
| `getSorteringsordningById` | `findByIdOptional(id).map(fromEntity)` |
| `getAllSorteringsordningar` | `findAll().stream().map(fromEntity).toList()` |
| `clearSorteringsordning` | `deleteAll()` |

Also add `toEntity` / `fromEntity` private helpers (UUID, OffsetDateTime, entries).

### `test/.../StorageTestCleaner.java`

`clearSorteringsordning()` currently calls `panacheOulDataStorage.clearSorteringsordning()`.
This continues to work — the implementation just changes internally to `deleteAll()`.

---

## Migration

### `src/main/resources/db/migration/V002__add_sorteringsordning_table.sql`

```sql
CREATE TABLE sorteringsordning (
    id          UUID        NOT NULL PRIMARY KEY,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL,
    updated_at  TIMESTAMPTZ NOT NULL,
    entries     TEXT        NOT NULL
);
```

Single table, single `entries` text column. No join tables needed.

---

## Effort summary

| Item | New LOC | Changed LOC |
|------|---------|-------------|
| `SorteringsordningPersistenceEntity` | ~50 | — |
| `SorteringsordningRepository` | ~8 | — |
| `SorteringsordningEntriesConverter` | ~30 | — |
| `PanacheOulDataStorage` | — | ~25 |
| `V002__add_sorteringsordning_table.sql` | ~10 | — |
| **Total** | **~98** | **~25** |

**Complexity: low.** No new dependencies. All existing tests pass without change because
`StorageTestCleaner` still calls `clearSorteringsordning()`. The only new test worth adding
is a restart-survival integration test (verifies data persists across a CDI context reset),
but that requires a dedicated `@QuarkusIntegrationTest` setup and can be deferred.

---

## Notes

- `SorteringsordningEntity` stays as a domain record — no JPA annotations leak into the logic layer.
- The business invariant "only one active sorteringsordning at a time" is enforced in
  `saveSorteringsordning` by `deleteAll()` before `persist()`, wrapped in the existing
  `@Transactional` on `PanacheOulDataStorage`.
- The `TEXT` column type avoids a `jsonb` Hibernate dialect dependency while still being
  stored compactly. Upgrade to `jsonb` later if PostgreSQL JSON querying is ever needed.
