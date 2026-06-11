# Krav — Backlog (ej planerade)

Krav som är identifierade men ännu inte inplanerade. Ska integreras i `krav.md` när de
schemaläggs för implementation.

---

## Icke-funktionella krav

### OUL-NFR-04.3 — DB-nivå sortering och paginering

- **OUL-NFR-04.3** Listning av uppgifter ska utföras med sortering och paginering direkt i
  databasen. Applikationslagret ska inte läsa samtliga uppgifter för att producera ett
  paginerat svar.

**Bakgrund:** Nuvarande implementation läser alla uppgifter till minnet, applicerar
sorteringsordning och paginerar i Java (`SortOrderApplier`). Detta är korrekt för små
datamängder men skalas inte. Implementationen kräver att sorteringsordningens entries
(inklusive villkor av typerna `eq`, `contains`, `between`, `offset_to_now`) översätts till
dynamisk SQL med `CASE WHEN`-rangordning och `ORDER BY` på databasnivå, följt av `LIMIT`/`OFFSET`.

**Beroende:** Kräver att sorteringsordningen är persistent (OUL-NFR-04.1, OUL-NFR-05.1) —
dvs. måste implementeras efter eller som del av FKPOC-848.
