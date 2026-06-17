# Plan: OUL-FR-04.2 & OUL-FR-05.3 — Sorteringsordning för handläggar-endpoints

## Requirements

- **OUL-FR-04.2** `POST /uppgifter/handlaggare/…` ska välja den högst prioriterade icke tilldelade
  uppgiften enligt default sorteringsordning. Utan konfigurerad sorteringsordning väljs godtycklig uppgift.
- **OUL-FR-05.3** `GET /uppgifter/handlaggare/…` ska returnera tilldelade uppgifter sorterade
  enligt default sorteringsordning. Utan konfigurerad sorteringsordning är ordningen odefinierad.

---

## Step 1 — Failing test for OUL-FR-05.3 (sorted handläggare list)

In `OulHandlaggareTest`, add:
```
@Test OUL-FR-05.3: should_return_assigned_tasks_sorted_by_sorteringsordning
```
- Create sorteringsordning with `sort_by = SKAPAD DESC`.
- Create two uppgifter with different `skapad` dates (via `planerad_till` proxy or direct Panache).
- Assign both to the same handläggare.
- GET assigned tasks; verify order matches sorteringsordning.

_Compile-fails until storage interface is updated._

---

## Step 2 — Failing test for OUL-FR-04.2 (priority-ordered assignment)

In `OulHandlaggareTest`, add:
```
@Test OUL-FR-04.2: should_assign_highest_priority_unassigned_task_per_sorteringsordning
```
- Create sorteringsordning with entry constraint `eq(roll, "PRIORITET")` for group 0.
- Create one "low" uppgift (no matching constraint → group 1) and one "high" uppgift (`roll = "PRIORITET"` → group 0).
- Assign to handläggare; assert the returned uppgift has `roll = "PRIORITET"`.

_Compile-fails until storage interface is updated._

---

## Step 3 — Add `buildHandlaggareListQuery` to `SorteringsordningQueryBuilder`

New method signature:
```java
public BuiltQuery buildHandlaggareListQuery(SorteringsordningEntity sorteringsordning,
                                             String handlaggarTypId, String handlaggarVarde)
```

SQL shape (same subquery structure as `build()`):
```sql
SELECT <UPPGIFT_COLUMNS>
FROM (
    SELECT *, <sort_group_expr> AS sort_group
    FROM schema.uppgift
    WHERE handlaggar_id_typ_id = :hl_typ_id
      AND handlaggar_id_varde = :hl_varde
) AS u
ORDER BY u.sort_group ASC, <per_entry_sort_by>, u.created_at ASC
```

No `countSql` needed (return `null` in `BuiltQuery`).

---

## Step 4 — Add `buildAssignQuery` to `SorteringsordningQueryBuilder`

New method signature:
```java
public BuiltQuery buildAssignQuery(SorteringsordningEntity sorteringsordning)
```

SQL shape (two-phase: rank without lock, lock by id):
```sql
SELECT * FROM schema.uppgift
WHERE id = (
    SELECT id FROM (
        SELECT id, <sort_group_expr> AS sort_group
        FROM schema.uppgift
        WHERE handlaggar_id_typ_id IS NULL AND handlaggar_id_varde IS NULL
    ) AS ranked
    ORDER BY ranked.sort_group ASC, ranked.created_at ASC
    LIMIT 1
)
FOR UPDATE SKIP LOCKED
```

If no sorteringsordning entries → fall back to existing Panache approach (or simple
`SELECT * FROM schema.uppgift WHERE handlaggar_id_typ_id IS NULL ... ORDER BY created_at ASC LIMIT 1 FOR UPDATE SKIP LOCKED`).

No `countSql` needed.

---

## Step 5 — Update `OulDataStorage` interface

```java
// Change:
List<UppgiftEntity> findAllUppgifterByHandlaggarId(Idtyp handlaggarId);
// To:
List<UppgiftEntity> findAllUppgifterByHandlaggarId(Idtyp handlaggarId,
                                                    SorteringsordningEntity sorteringsordning);

// Change:
UppgiftEntity assignNewUppgift(Idtyp handlaggarId);
// To:
UppgiftEntity assignNewUppgift(Idtyp handlaggarId, SorteringsordningEntity sorteringsordning);
```

---

## Step 6 — Implement in `PanacheOulDataStorage`

**`findAllUppgifterByHandlaggarId`**: use `queryBuilder.buildHandlaggareListQuery(sorteringsordning, typId, varde)`
and execute as native SQL (same pattern as `findUppgifterPage`). If `sorteringsordning` is null/empty entries,
fall back to the existing Panache query (no ORDER BY guarantee required by spec).

**`assignNewUppgift`**: use `queryBuilder.buildAssignQuery(sorteringsordning)` and execute as native SQL.
On match, UPDATE status to TILLDELAD and handläggare fields, then persist. Return mapped entity.
Replace the existing `PESSIMISTIC_WRITE` Panache approach.

---

## Step 7 — Update `OperativtUppgiftslagerService`

**`assignNewTask`**: resolve default sorteringsordning from storage, pass to `storage.assignNewUppgift(handlaggare, sorteringsordning)`.
If no default, pass `new SorteringsordningEntity(null, null, List.of())` (empty = no priority order).

**`getUppgifterHandlaggare`**: resolve default sorteringsordning from storage, pass to
`storage.findAllUppgifterByHandlaggarId(handlaggare, sorteringsordning)`.

---

## Step 8 — Unit tests in `SorteringsordningQueryBuilderTest`

Add tests for:
- `buildHandlaggareListQuery` SQL contains correct WHERE clause and ORDER BY structure.
- `buildAssignQuery` SQL contains `FOR UPDATE SKIP LOCKED` and sorts by sort_group.
- Both methods degrade gracefully with empty entry list.
