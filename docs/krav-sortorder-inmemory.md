# Krav — Sorteringsordning utan persistence

## Bakgrund och syfte

Detta dokument definierar krav för en inkrementell implementation av sorteringsordning i OUL.
Det är ett tillägg till [krav.md](krav.md) och gäller specifikt för fas 1.

Huvudsyftet med sorteringsordning är att styra prioriteringen av uppgifter som tilldelas
handläggare — rätt uppgift ska nås av rätt handläggare i rätt ordning. En Administratör
konfigurerar sorteringsordningen via management-API. Den består av en ordnad lista med poster
där varje post kan filtrera uppgifter via villkor och styra sorteringsriktning inom sin grupp.

---

## Avvikelser från fullständig implementation

Följande krav från den fullständiga specifikationen gäller **inte** i fas 1:

| Krav | Avvikelse |
|------|-----------|
| REQ-OUL-SORT-005 (persistens) | En in-memory-lagring är tillräcklig. Sorteringsordningen behöver inte överleva omstart. NFR-04 i [krav.md](krav.md) gäller inte för sorteringsordning i fas 1. |
| REQ-OUL-SORT-003 (obligatorisk default) | Endast en sorteringsordning stöds åt gången. Ny skapelse ersätter befintlig och är alltid default. |
| REQ-OUL-SORT-006 (unikt ID per skapelse) | Partiellt uppfyllt — ett nytt UUID tilldelas vid varje `POST`, men det finns alltid max ett aktivt. |

---

## Funktionella krav

### FR-07 — Skapa sorteringsordning

- **FR-07.1** En Administratör ska kunna skapa en ny sorteringsordning via management-API.
- **FR-07.2** En ny skapelse ersätter alltid en eventuell befintlig sorteringsordning.
- **FR-07.3** Den skapade sorteringsordningen ska alltid vara aktiv och betraktas som default.
- **FR-07.4** OUL ska tilldela ett nytt unikt UUID vid varje skapelse och returnera det i svaret.
- **FR-07.5** Sorteringsordningen ska innehålla en ordnad lista med minst en post (`SorteringsordningEntry`).
- **FR-07.6** Varje post kan innehålla valfria villkor (`constraints`) och en valfri sorteringsangivelse (`sort_by`).

### FR-08 — Hämta aktiv sorteringsordning

- **FR-08.1** En Administratör ska kunna hämta den aktiva sorteringsordningen via `GET /sorteringsordning/default`.
- **FR-08.2** Om ingen sorteringsordning är skapad ska HTTP 404 returneras.
- **FR-08.3** En Administratör ska kunna hämta sorteringsordningen via dess ID med `GET /sorteringsordning/{id}`.
- **FR-08.4** Om angivet ID inte matchar aktiv sorteringsordning ska HTTP 404 returneras.

### FR-09 — Lista sorteringsordningar

- **FR-09.1** En Administratör ska kunna lista befintliga sorteringsordningar via `GET /sorteringsordning`.
- **FR-09.2** Svaret innehåller noll eller en post beroende på om en sorteringsordning finns.

### FR-10 — Förhandsgranska sorteringsordning

- **FR-10.1** En Administratör ska kunna skicka in en sorteringsordningsspecifikation för förhandsgranskning via `POST /sorteringsordning/preview`.
- **FR-10.2** Förhandsgranskning applicerar specifikationen på befintliga uppgifter och returnerar resultatet utan att persisteringen påverkas.
- **FR-10.3** Förhandsgranskning accepterar samma `limit` och `offset` som ordinarie listning.

### FR-11 — Tillämpa sorteringsordning vid listning av uppgifter

FR-11 ersätter och utökar **FR-03** för management-listning.

- **FR-11.1** `GET /uppgifter` ska acceptera parametrarna `sorteringsordningId` (valfri UUID), `limit` (obligatorisk, minst 1) och `offset` (valfri, standard 0).
- **FR-11.2** Svaret ska vara av typen `UppgiftPage` med fälten `total` (antal matchande uppgifter före paginering) och `items` (paginerad lista av uppgifter).
- **FR-11.3** Om en aktiv sorteringsordning finns ska den tillämpas automatiskt vid listning, även om `sorteringsordningId` inte anges.
- **FR-11.4** Om `sorteringsordningId` anges och matchar aktiv sorteringsordnings ID ska det accepteras utan fel. Om angivet ID inte matchar aktiv sorteringsordning ska HTTP 404 returneras.
- **FR-11.5** Tillämpning av sorteringsordning innebär att uppgifter matchas mot poster i ordning. En uppgift tilldelas den första posten vars villkor alla är uppfyllda. Uppgifter utan matchande post hamnar sist.
- **FR-11.6** Inom en post sorteras matchande uppgifter enligt postens `sort_by`-angivelse, om en sådan finns.

### FR-12 — Villkorsutvärdering

- **FR-12.1** Villkorstypen `eq` innebär att ett fält ska vara exakt lika med angivet värde.
- **FR-12.2** Villkorstypen `contains` innebär att ett textfält ska innehålla angiven delsträng.
- **FR-12.3** Villkorstypen `between` innebär att ett datumfält ska ligga inom ett angivet intervall (inklusive).
- **FR-12.4** Villkorstypen `offset_to_now` innebär att ett datumfält ska ligga inom ett relativt intervall från nu, uttryckt som en offset (t.ex. `-7d` för de senaste 7 dagarna).
- **FR-12.5** Alla villkor inom en post utvärderas med AND-semantik — samtliga måste vara uppfyllda för att uppgiften ska matcha posten.

---

## Icke-funktionella krav (tillägg)

### NFR-05 — Lagring av sorteringsordning (fas 1)

- **NFR-05.1** Sorteringsordningen lagras i minnet och behöver inte vara persistent i fas 1.
- **NFR-05.2** Implementationen ska vara förberedd för att byta ut in-memory-lagringen mot persistent lagring i en senare fas utan ändringar i service- eller controllerskikt.

### NFR-06 — Trådsäkerhet för sorteringsordning

- **NFR-06.1** Läs- och skrivoperationer mot den aktiva sorteringsordningen ska vara trådsäkra.

---

## Ej krav i fas 1

| Endpoint / Funktion | Hantering i fas 1 |
|---------------------|-------------------|
| `PUT /sorteringsordning/{id}/default` | Kan returnera `204` utan sidoeffekter |
| `DELETE /sorteringsordning/{id}` | Kan returnera `405 Method Not Allowed` eller lämnas oimplementerat |
| Persistens av sorteringsordning | Ej krav — in-memory räcker |

---

## Uppgradering till fullständig implementation

Inga API-förändringar krävs för att gå från fas 1 till fullständig implementation.
Klienter byggda mot specifikationen i fas 1 fortsätter att fungera oförändrade.