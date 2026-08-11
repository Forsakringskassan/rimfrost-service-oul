# Plan: Remove individer from OUL — fetch from handläggning when needed

## Background

OUL currently receives `individer` in the `POST /uppgifter` request body, stores them in the
`uppgift_individ` table, and returns them in both the handläggar API and management API responses.

Investigation shows that no active consumer reads `individer` from either response:
- The portal BFF maps them into its model but the frontend never displays them.
- The management API adapter never passes or reads them.

Per OUL-FR-01.8, OUL shall neither store nor return individer. OUL fetches them internally only
when needed for its own operations (e.g. SID-check at assignment via
`PanacheOulDataStorage.containsSid()`, which is unchanged). Consumers needing individinformation
shall query handläggning directly.

Requirement: OUL-FR-01.8.

---

## Approach: TDD

Write tests first. Remove individer from request helpers and response assertions. Stub minimum to
compile, then delete layer by layer until green.

---

## Steps

### Step 1 — Update tests ✅

**`OulTestData.java`**
- Remove `setIndivider(...)` from `createUppgiftRequest()`.

**`OulHandlaggareTest.java`**
- Remove the `assertNotNull(...getIndivider())` assertion (line 61).
- Remove the `assertEquals(...getIndivider(), ...getIndivider())` assertion (line 128).

**Deviation:** `OulManagementTest` also had an individer assertion and a `toIdtyp()` helper
referencing the regler `Idtyp` — removed during Step 2 when the missing class caused a
`NoClassDefFoundError` at test discovery.

---

### Step 2 — Remove individer from the inbound request ✅

**`OperativtUppgiftslagerAddRequest.java`**
- Remove `Idtyp[] individer()`.

**`ManagementMapper.java`**
- Remove `.individer(toIdtyper(request.getIndivider()))` from `toAddRequest()`.
- Remove the `toIdtyper()` private helper.

**`OperativtUppgiftslagerService.java`**
- Remove `.individer(addRequest.individer())` from the `ImmutableUppgiftEntity.builder()` call.

`rimfrost-service-oul-management-regler-api-jaxrs-spec` bumped to `0.0.4-SNAPSHOT` in `pom.xml`.

---

### Step 3+4 — Remove individer from the domain and API responses ✅

**`UppgiftEntity.java`** (logic entity, Immutables interface)
- Remove `Idtyp[] individer()`.

**`UppgiftDto.java`**
- Remove `Idtyp[] individer()`.

**`LogicMapper.java`**
- Remove `.individer(uppgift.individer())` from the `UppgiftDto` builder call.

**`ManagementMapper.java`** — `toOperativUppgift()`
- Remove `uppgift.individer(...)` and unused `Arrays` import.

**`PresentationRestMapper.java`** — `toUppgift()`
- Remove `uppgift.setIndivider(...)` and unused `Arrays` import.

**Deviation:** Steps 3 and 4 were implemented together. Removing `individer` from `UppgiftDto`
immediately caused compile errors in `ManagementMapper` and `PresentationRestMapper`, so both
were fixed in the same commit. Maven's incremental compiler also masked the errors until a
`mvn clean test` was run, causing `OulDataStorageMapper` individer removal (part of Step 5) to
be pulled in at the same time.

Note: both `OperativUppgift` models still have `individer` in their specs — a new version of
each spec jar without the field is a separate concern (no breaking test failure results from
the field remaining in the model as an unset/null field).

---

### Step 5 — Remove individer from the storage layer ✅

**`UppgiftEntity.java`** (JPA entity, `storage.internal.entity`)
- Remove the `individer` field, its `@OneToMany` mapping, and its getter/setter.

**`OulDataStorageMapper.java`** ✅ (done early, pulled in by Step 3+4 compile errors)
- Remove `entity.setIndivider(...)` from `toUppgiftEntity(domain → persistence)`.
- Remove `.individer(...)` from `toUppgiftEntity(persistence → domain)`.
- Remove the `toIndividEntity()` and `toIdtyp(UppgiftIndividEntity)` private helpers.

**`UppgiftIndividEntity.java`** and **`UppgiftIndividId.java`**
- Delete both files.

---

### Step 6 — Add Flyway migration to drop the table ✅

**New file: `src/main/resources/db/migration/V005__drop_uppgift_individ.sql`**
```sql
DROP TABLE uppgift_individ;
```

---

## Execution order

1 → 2 → 3 → 4 → 5 → 6

Steps 2 and 4 each depend on spec releases in the corresponding OpenAPI repos.
The SID-check in `PanacheOulDataStorage.containsSid()` is unchanged — it already fetches
individer live from handläggning and is unaffected by this change.