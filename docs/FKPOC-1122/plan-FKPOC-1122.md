# Plan — FKPOC-1122: Assignable support

## Krav

| Krav | Beskrivning |
|------|-------------|
| OUL-FR-01.9 | `assignable` (boolean) tas emot vid skapandet via Regler-API:ets `POST /uppgifter` och sparas persistent |
| OUL-FR-04.8 | Automatisk tilldelning hoppar över uppgifter med `assignable=false` |
| OUL-FR-18.7 | Handläggare kan manuellt tilldela sig en uppgift med `assignable=false` via `POST /uppgifter/{uppgift_id}/handlaggare` |
| OUL-FR-18.8 | Team-check (OUL-FR-18.3) gäller inte för `assignable=false`-uppgifter — gäller oavsett om uppgiften är otilldelad eller tilldelad annan. SID-check gäller fortfarande. |
| OUL-FR-21   | `POST /uppgifter/individ` söker uppgifter för en individ (id_typ + id_varde) med valfria filter på `statuses` och `assignable`, med SID-filtrering. Initialt stöds endast `assignable=false` — övriga kombinationer returnerar HTTP 501. |

---

## Steg

- [x] **0. Fixa pre-existing kompileringsfel i `SidCheckerService`**

  `SidCheckerService.java:32` anropar `handlaggning.yrkande().individYrkandeRoller()` vilket inte existerar på `Yrkande`. Korrekt metod är `rollerIYrkande()` — varje `RollIYrkande` har ett `individ: Idtyp`-fält med `typId` och `varde`.

  Byt ut `individYrkandeRoller()` mot `rollerIYrkande()` och justera mappningen till att använda `RollIYrkande.individ()`.

- [x] **1. Flyway-migration — lägg till `assignable`-kolumn**

  Ny migrationsfil under `src/main/resources/db/migration/`.
  ```sql
  ALTER TABLE uppgift ADD COLUMN assignable BOOLEAN NOT NULL DEFAULT TRUE;
  ```
  `NOT NULL DEFAULT TRUE` säkerställer bakåtkompatibilitet med befintliga rader.

- [x] **2. Propagera `assignable` genom alla lager**

  Följande filer behöver uppdateras:

  | Fil | Ändring |
  |-----|---------|
  | `storage/internal/entity/UppgiftEntity.java` | Lägg till `@Column(name="assignable") boolean assignable` |
  | `logic/entity/UppgiftEntity.java` | Lägg till `boolean assignable()` i immutable interface |
  | `logic/dto/UppgiftDto.java` | Lägg till `assignable` |
  | `logic/dto/OperativtUppgiftslagerAddRequest.java` | Lägg till `assignable` |
  | `presentation/rest/management/ManagementMapper.java` | Mappa `CreateUppgiftRequest.getAssignable()` → `addRequest.assignable` |
  | `storage/internal/OulDataStorageMapper.java` | Propagera `assignable` i båda riktningarna |
  | `logic/LogicMapper.java` | Propagera `assignable` till/från `UppgiftDto` |
  | `logic/OperativtUppgiftslagerService.java` | Skicka med `assignable` vid `ImmutableUppgiftEntity`-skapande |

- [x] **3. Filtrera `assignable=false` i automatisk tilldelning (OUL-FR-04.8)**

  I `storage/internal/SorteringsordningQueryBuilder.java`, metod `buildAssignQuery()`:
  lägg till `AND u.assignable = TRUE` i WHERE-klausulen, precis som befintliga filter för `handlaggar_id IS NULL`.

- [x] **4. Tillåt manuell tilldelning av `assignable=false`-uppgifter (OUL-FR-18.7, OUL-FR-18.8)**

  `OperativtUppgiftslagerService.reassignUppgift()` har i dag ett team-check (OUL-FR-18.3) som kräver att befintlig tilldelad handläggare är i samma team.

  **Design:**
  - Om uppgiften har `assignable=false` → hoppa över team-checken helt, oavsett om uppgiften är otilldelad eller tilldelad en handläggare i annat team. SID-check (OUL-FR-18.6) gäller fortfarande.

- [x] **5. Implementera `POST /uppgifter/individ` (OUL-FR-21)**

  Endpointen och `PostUppgifterIndividRequest` är uppdaterad i `rimfrost-service-oul-openapi`. Request body:
  - `id_typ` + `id_varde` (obligatoriska) — identifierar individen
  - `statuses` (valfri) — filtrera på uppgiftsstatus
  - `assignable` (valfri) — filtrera på tilldelningsbarhet

  **Initialt scope:** Returnera HTTP 501 om `assignable` ej anges eller `assignable=true`. Endast `assignable=false` implementeras nu.

  **Söklogik (OUL-FR-21.5):**
  1. Filtrera uppgifter i OUL:s DB på `assignable=false` och valfria `statuses`.
  2. För varje kandidatuppgift: anropa `handlaggningAdapter.readHandlaggning(handlaggningId)` och kontrollera om individen (`id_typ`/`id_varde`) matchar något `RollIYrkande.individ()` i `handlaggning.yrkande().rollerIYrkande()`.
  3. Exkludera uppgifter där individen inte matchar.

  **Service-metod** — ny metod i `OperativtUppgiftslagerService`:
  ```java
  List<UppgiftDto> getUppgifterForIndivid(Idtyp individ, List<String> statuses, Boolean assignable, Idtyp handlaggare)
  ```
  - Returnerar HTTP 501 om `assignable == null || assignable == true`.
  - Hämtar kandidatuppgifter via ny storage-metod.
  - Anropar handläggningstjänsten per uppgift för individ/yrkanderoll-matchning.
  - Filtrerar SID-märkta uppgifter om handläggaren saknar SID-behörighet (OUL-FR-21.6).
  - Om SID-status inte kan avgöras → HTTP 500 (OUL-FR-21.7).

  **Storage-metod** — ny metod i `OulDataStorage` + `PanacheOulDataStorage`:
  ```java
  List<UppgiftEntity> findUppgifterByAssignableAndStatuses(boolean assignable, List<UppgiftStatus> statuses)
  ```

  **Controller** — implementera `postUppgifterIndivid()` i `OperativtUppgiftslagerController`.

- [x] **6. Tester**

  - Enhetstester för `SidCheckerService` — verifiera att individer korrekt extraheras från `rollerIYrkande()` och skickas till `sidAdapter` (pre-existing gap, avslöjat i steg 0)
  - Enhetstester för `ManagementMapper` (steg 2)
  - Enhetstester för `SorteringsordningQueryBuilder` — verifiera att `assignable=false` exkluderas (steg 3)
  - Enhetstester för `OperativtUppgiftslagerService.reassignUppgift()` — `assignable=false` med och utan befintlig tilldelning, SID-check gäller (steg 4)
  - Integrationstester (`OulHandlaggareTest`) för `POST /uppgifter/individ` — `assignable=false`, 501 för övriga kombinationer, SID-filtrering (steg 5)
