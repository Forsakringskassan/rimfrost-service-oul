# Plan: DB-nivå sortering och paginering (OUL-NFR-04.3)

## Status: ✅ COMPLETE

Implementerat i `fix/FKPOC-851-improve-sorting`. `SorteringsordningQueryBuilder` ersätter
`SortOrderApplier` och `ConstraintMatcher`. Preview-flödet kör samma SQL-logik som listning.
Steg 4 (prestandatest) är ej genomfört — se `test-gaps.md`.

---

Adresserade OUL-NFR-04.3 från `krav-backlog.md`. Den tidigare implementationen läste alla
uppgifter till minnet, applicerade sorteringsordning i Java och paginerade i minnet.

**Beroende:** Krävde persistent sorteringsordning (FKPOC-848). ✅

---

## Problembeskrivning (historik)

```
getUppgifterPage()
  → findAllUppgifter()          // SELECT * FROM uppgift  (all rows)
  → SortOrderApplier.apply()    // matching + sorting i Java
  → subList(offset, offset+limit) // paginering i Java
```

Med N uppgifter lästes alltid N rader per anrop, oavsett `limit`.

---

## Målbild

```
getUppgifterPage()
  → buildQuery(sorteringsordning, limit, offset)
  → one SQL query with ORDER BY + LIMIT + OFFSET
```

---

## Teknisk approach

### Prioritetsgrupper via CASE WHEN

Sorteringsordningens entries definierar prioriterade grupper. En uppgift tillhör den
**första** entry vars alla villkor är uppfyllda. Uppgifter utan matchande entry hamnar sist.

Detta översätts till en SQL-rankkolumn:

```sql
CASE
  WHEN <entry_0_predicates> THEN 0
  WHEN <entry_1_predicates> THEN 1
  ...
  ELSE <antal entries>
END AS sort_group
```

### Villkorstyper → SQL-predikat

| Villkorstyp     | Java                                    | SQL                                              |
|-----------------|-----------------------------------------|--------------------------------------------------|
| `eq`            | `field.equals(value)`                   | `field = :value`                                 |
| `contains`      | `field.contains(value)`                 | `field LIKE '%' \|\| :value \|\| '%'`            |
| `between`       | `from <= field <= to`                   | `field BETWEEN :from AND :to`                    |
| `offset_to_now` | `now+offset <= field <= now`            | `field BETWEEN NOW() + :offset AND NOW()`        |

### Sortering inom grupp via sort_by

Varje entry kan ha en valfri `sort_by` (fält + riktning). Uppgifter inom samma grupp
sorteras på det fältet. Uppgifter i grupper utan `sort_by` sorteras på `created_at ASC`
som fallback.

```sql
ORDER BY
  sort_group ASC,
  CASE sort_group
    WHEN 0 THEN <entry_0_sort_field>
    WHEN 1 THEN <entry_1_sort_field>
    ...
  END <ASC|DESC>,
  created_at ASC   -- tiebreaker
LIMIT :limit OFFSET :offset
```

### Fältmappning

Sorteringsordningens `field`-värden (från `SorteringsordningField`-enumen) behöver
mappas till kolumnnamn i `uppgift`-tabellen. En statisk `Map<String, String>` i en
hjälpklass håller denna mappning.

### Räkning

`total`-fältet i `UppgiftPage` kräver en separat `SELECT COUNT(*)` med samma WHERE-villkor
men utan `LIMIT`/`OFFSET`. Alternativt `COUNT(*) OVER()` som fönsterfunktion i samma query.

---

## Implementationssteg

### Steg 1 — Fältmappning och predikatsbyggare

Skapa `storage/internal/SorteringsordningQueryBuilder` som tar en
`SorteringsordningEntity` och producerar en Panache `Parameters`-instans + JPQL/SQL-sträng
(eller en `jakarta.persistence.criteria.CriteriaQuery`).

Börja med `eq` och `between` — de täcker de vanligaste fallen. `contains` och
`offset_to_now` läggs till i separata PR:er.

### Steg 2 — Ersätt `findAllUppgifter` i `getUppgifterPage`

`PanacheOulDataStorage` får en ny metod:

```java
UppgiftPage findUppgifterWithSortering(SorteringsordningEntity sorteringsordning,
                                        int limit, int offset);
```

`OperativtUppgiftslagerService.getUppgifterPage` ersätter anropet till `getTasks()` +
`SortOrderApplier` med detta enda lagringsanrop.

### Steg 3 — Behåll `SortOrderApplier` för preview

`previewSorteringsordning` applicerar en osparad spec på befintliga uppgifter — den kan
inte använda DB-query eftersom spec:en inte är persisterad. `SortOrderApplier` behålls
enbart för preview-flödet.

### Steg 4 — Prestandatest

Verifiera med ett test som skapar >1000 uppgifter och kontrollerar att `getUppgifterPage`
inte läser fler rader än `limit` från databasen (via SQL-loggning eller query counter).

---

## Risker

| Risk | Hantering |
|------|-----------|
| `offset_to_now`-offset-format (`-7d`) inte nativt i SQL | Parser i Java → `INTERVAL` i SQL |
| Dynamisk SQL öppnar för SQL-injektion | Använd alltid parameteriserade queries; fältnamn mappas via vitlista |
| `sort_by`-fält varierar per entry — svårt i JPQL | Använd native query eller Criteria API |
| `COUNT(*) OVER()` stöds inte i alla Hibernate-versioner | Fallback: separat COUNT-query |

---

## Vad som inte ingår

- Ändring av `SorteringsordningEntry`-strukturen
- Indexoptimering på `uppgift`-tabellen (separat DBA-uppgift)
- Stöd för nya villkorstyper utöver de fyra befintliga
