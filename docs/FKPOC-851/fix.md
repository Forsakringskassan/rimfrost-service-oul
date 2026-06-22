# Fix notes: FKPOC-851

## Null guard in `buildPredicate`

`SorteringsordningQueryBuilder.buildPredicate` looks up column names from the
whitelist maps (`EQ_FIELD_TO_COLUMN`, `DATE_FIELD_TO_COLUMN`) but does not check
for null. If a field value from the spec has no entry in the map, the SQL string
will contain `"null = :param"` instead of failing fast.

Add a guard after each `get()` call and throw `IllegalArgumentException` when
the column is null, e.g.:

```java
var col = EQ_FIELD_TO_COLUMN.get(eq.getField().toString());
if (col == null) throw new IllegalArgumentException("Unmapped field: " + eq.getField());
```

Affects: `ConstraintEq`, `ConstraintContains`, `ConstraintBetween`,
`ConstraintOffsetToNow` cases.

## `ConstraintContains` uses wrong field map

`ConstraintContains.getField()` returns `SorteringsordningFieldString`, but the
lookup uses `EQ_FIELD_TO_COLUMN` (intended for `SorteringsordningFieldEq`). The
sets overlap today so it works, but it would silently accept `uppgift_id` as a
`LIKE`-able field if the string-field enum is ever extended.

Replace with a dedicated string-field map, or use `SORT_FIELD_TO_COLUMN` which
already covers all string fields without `uppgift_id`.
