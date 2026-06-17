# Plan: Pagination for GET /sorteringsordning

## Background

`GET /sorteringsordning` in the management API now requires `limit` (required, min 1) and
`offset` (optional, default 0) query parameters and returns `SorteringsordningPage` (with
`total` and `items`) instead of a bare array. Requirements: OUL-FR-11.2 and OUL-FR-11.3.

The management openapi 1.3.3 jar (already declared in `pom.xml`) contains the updated
`SorteringsordningApi` interface and `SorteringsordningPage` model — no dependency bump needed.

---

## Approach: TDD

Write tests first. To get them to compile, stub out the minimum types needed before
implementing. Run tests — they fail. Then implement layer by layer until green.

---

## Steps

### Step 1 — Write failing tests

**`src/test/java/se/fk/github/rimfrost/operativt/uppgiftslager/OulTestBase.java`**
Replace `getSorteringsordningar()` helper with two overloads returning `SorteringsordningPage`:
- `getSorteringsordningar(int limit)`
- `getSorteringsordningar(int limit, int offset)`

**`src/test/java/se/fk/github/rimfrost/operativt/uppgiftslager/OulSorteringTest.java`**
- Update all existing call sites: use `page.getTotal()` / `page.getItems()`.
- Add two new tests:
  - `limit` truncates items but `total` reflects full count
  - `offset` skips items but `total` reflects full count

---

### Step 2 — Stub minimum types to make tests compile

**New file:**
`src/main/java/se/fk/github/rimfrost/operativt/uppgiftslager/logic/SorteringsordningEntityPage.java`

Mirrors `UppgiftEntityPage`: `int total` + `List<SorteringsordningEntity> items`.

**`src/main/java/se/fk/github/rimfrost/operativt/uppgiftslager/storage/OulDataStorage.java`**
Add method stub:
```java
SorteringsordningEntityPage findSorteringsordningarPage(int limit, int offset);
```

At this point tests compile but fail at runtime — the controller still returns the old
bare-array response and the new interface contract is not met.

---

### Step 3 — Implement storage

**`src/main/java/se/fk/github/rimfrost/operativt/uppgiftslager/storage/internal/PanacheOulDataStorage.java`**
Implement `findSorteringsordningarPage` using Panache `.range(offset, offset + limit - 1)` on
`findAll(Sort.by("createdAt", Descending))`, with a separate `.count()` call for `total`.

---

### Step 4 — Implement service delegation

**`src/main/java/se/fk/github/rimfrost/operativt/uppgiftslager/logic/OperativtUppgiftslagerService.java`**
Add thin delegation method:
```java
public SorteringsordningEntityPage getSorteringsordningarPage(int limit, int offset)
{
    return storage.findSorteringsordningarPage(limit, offset);
}
```

---

### Step 5 — Implement mapper

**`src/main/java/se/fk/github/rimfrost/operativt/uppgiftslager/presentation/rest/management/ManagementMapper.java`**
Add `toSorteringsordningPage(SorteringsordningEntityPage)` that streams items through the
existing `toSorteringsordningResponse` and wraps with `total`.

---

### Step 6 — Implement controller

**`src/main/java/se/fk/github/rimfrost/operativt/uppgiftslager/presentation/rest/management/SorteringController.java`**
Replace current `getSorteringsordningar()` override with the new interface signature:
```java
SorteringsordningPage getSorteringsordningar(
    @QueryParam("limit") @NotNull @Min(1) Integer limit,
    @QueryParam("offset") @Min(0) Integer offset)
```
Delegate to service and mapper. Use `offset != null ? offset : 0` guard (same pattern as
`getUppgifter` and `previewSorteringsordning`).

Tests should now be green.

---

## Execution order

1 → 2 → 3 → 4 → 5 → 6

No Flyway migration needed — no schema changes.
