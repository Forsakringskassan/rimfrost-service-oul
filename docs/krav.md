# Krav — Operativt Uppgiftslager (OUL)

## Bakgrund och syfte

Operativt Uppgiftslager (OUL) är en central komponent i handläggningsflödet för Försäkringskassan.
Syftet är att fungera som en operativ kö för manuella arbetsuppgifter som uppstår när regler
identifierar ett behov av manuell handläggning. OUL håller reda på vilka uppgifter som
finns, vilken handläggare som är tilldelad uppgiften, och informerar intressenter när en uppgift byter status.

Sorteringsordning styr prioriteringen av uppgifter som tilldelas handläggare — rätt uppgift ska
nås av rätt handläggare i rätt ordning. En Administratör konfigurerar sorteringsordningen via
management-API. Den består av en ordnad lista med poster där varje post kan filtrera uppgifter
via villkor och styra sorteringsriktning inom sin grupp.

---

## Intressenter och aktörer

| Aktör         | Roll |
|---------------|------|
| Regler        | Skapar och avslutar uppgifter via management-API |
| Handläggare   | Hämtar och utför tilldelade uppgifter via handläggar-API |
| Regler        | Prenumererar på statusnotifieringar via Kafka |
| Administratör | Övervakar och administrerar samtliga uppgifter och sorteringsordningar via management-API |

---

## Funktionella krav

### OUL-FR-01 — Skapa uppgift

- **OUL-FR-01.1** Regler ska kunna skapa en ny operativ uppgift i OUL.
- **OUL-FR-01.2** En ny uppgift ska tilldelas status `NY` vid skapandet.
- **OUL-FR-01.3** Uppgiften ska knytas till ett handläggningsärende.
- **OUL-FR-01.4** Uppgiften ska innehålla:
  - vilken regel som triggade uppgiften
  - roll som ska utföra uppgiften
  - beskrivning
  - URL till regelns tjänst som portalen använder
- **OUL-FR-01.5** Uppgiften ska vara kopplad till ett erbjudande.
- **OUL-FR-01.6** Uppgiften ska innehålla CloudEvent-attribut som OUL bevarar och returnerar oförändrade, för att möjliggöra korrelation mellan regeln och dess flödesprocess.
- **OUL-FR-01.7** OUL ska returnera ett unikt uppgifts-ID vid skapandet.
- **OUL-FR-01.8** OUL ska inte lagra eller returnera berörda individer. Individer hämtas av OUL internt vid behov (t.ex. SID-kontroll vid tilldelning) direkt från handläggningstjänsten via det `handlaggningId` som uppgiften är knuten till. Konsumenter som behöver individinformation ska hämta den från handläggningstjänsten.

### OUL-FR-02 — Avsluta uppgift

- **OUL-FR-02.1** Regler ska kunna avsluta en uppgift med angiven orsak.
- **OUL-FR-02.2** En avslutad uppgift ska tas bort permanent.
- **OUL-FR-02.3** OUL ska returnera uppgiftens senaste status vid avslut.
- **OUL-FR-02.4** Om specificerad uppgift inte finns ska HTTP 404 returneras.

### OUL-FR-03 — Lista uppgifter (management)

- **OUL-FR-03.1** Det ska finnas ett gränssnitt för att lista samtliga uppgifter i lagret, oavsett status.
- **OUL-FR-03.2** Gränssnittet är avsett för Administratörer och driftövervakning.
- **OUL-FR-03.3** En Administratör ska kunna se samtliga uppgifter oavsett tilldelningsstatus, inklusive handläggaridentitet för tilldelade uppgifter.
- **OUL-FR-03.4** `GET /uppgifter` ska acceptera parametrarna `sorteringsordningId` (valfri UUID), `limit` (obligatorisk, minst 1) och `offset` (valfri, standard 0).
- **OUL-FR-03.5** Svaret ska vara av typen `UppgiftPage` med fälten `total` (antal uppgifter före paginering) och `items` (paginerad lista av uppgifter).
- **OUL-FR-03.6** Om en aktiv sorteringsordning finns ska den tillämpas automatiskt vid listning, även om `sorteringsordningId` inte anges.
- **OUL-FR-03.7** Om `sorteringsordningId` anges och matchar en befintlig sorteringsordnings ID ska det accepteras. Om angivet ID inte hittas ska HTTP 404 returneras.
- **OUL-FR-03.8** Tillämpning av sorteringsordning innebär att uppgifter matchas mot poster i ordning. En uppgift tilldelas den första posten vars villkor alla är uppfyllda. Uppgifter utan matchande post hamnar sist.
- **OUL-FR-03.9** Inom en post sorteras matchande uppgifter enligt postens `sort_by`-angivelse, om en sådan finns.

### OUL-FR-04 — Hämta ny uppgift (handläggare)

- **OUL-FR-04.1** En handläggare ska kunna begära en ny icke tilldelad uppgift och automatiskt bli tilldelad den.
- **OUL-FR-04.2** OUL väljer den högst prioriterade tillgängliga icke tilldelade uppgiften enligt aktiv sorteringsordning och sätter status till `TILLDELAD`. Om ingen sorteringsordning är konfigurerad väljs en godtycklig tillgänglig uppgift.
- **OUL-FR-04.3** Handläggarens identitet fastställs från bearer-token.
- **OUL-FR-04.4** Om ingen icke tilldelad uppgift finns ska ett tomt svar returneras utan felkod.
- **OUL-FR-04.5** Vid tilldelning ska en statusnotifiering publiceras på Kafka.
- **OUL-FR-04.6** Vid tilldelning av uppgift så ska SID status tas hänsyn till. Om en uppgift är SID markerad och handläggaren inte har SID roll så ska uppgiften hoppas över och nästa uppgift utvärderas.
- **OUL-FR-04.7** Om handläggningsinformationen för en uppgift inte kan läsas ska uppgiften hoppas över och nästa utvärderas (jfr OUL-FR-04.6). Felet loggas på WARN-nivå. Överhoppningen gäller endast det aktuella anropet.

### OUL-FR-05 — Lista tilldelade uppgifter (handläggare)

- **OUL-FR-05.1** En handläggare ska kunna lista alla uppgifter som är tilldelade till honom/henne.
- **OUL-FR-05.2** Listan filtreras på handläggarens identitet.
- **OUL-FR-05.3** Uppgifterna ska returneras sorterade enligt default sorteringsordning. Om ingen sorteringsordning är konfigurerad är ordningen i resultatet odefinierat.
- **OUL-FR-05.4** Vid varje listning kontrolleras på nytt, för varje redan tilldelad uppgift, om den är SID-märkt och om den tilldelade handläggaren har SID-behörighet — på samma sätt som vid tilldelning (OUL-FR-04.6). En uppgift som är SID-märkt och vars handläggare saknar SID-behörighet tas bort ur tilldelningen (återgår till status `NY`, tillgänglig för ny tilldelning) och exkluderas ur svaret.
- **OUL-FR-05.5** Svaret ska alltid innehålla fältet `borttagna_pga_behorighet` (antal), som anger hur många uppgifter som togs bort enligt OUL-FR-05.4 under det aktuella anropet. 0 om inga togs bort.
- **OUL-FR-05.6** Om en uppgifts SID-status inte kan avgöras (t.ex. avbrott mot handläggnings- eller SID-tjänsten) ska hela listanropet ge HTTP 500, istället för att gissa och riskera att returnera en lista som felaktigt inkluderar eller exkluderar en SID-märkt uppgift. Om enbart handläggarens SID-behörighet inte kan avgöras (avbrott mot Team-tjänsten) behandlas det som att behörighet saknas.

### OUL-FR-06 — Statusnotifiering via Kafka

- **OUL-FR-06.1** När en uppgift tilldelas en handläggare ska OUL publicera ett meddelande på Kafka.
- **OUL-FR-06.2** Meddelandet ska innehålla:
  - handläggningsärendets ID
  - uppgiftens ID
  - den tilldelade handläggarens ID
  - uppgiftens status
- **OUL-FR-06.3** Meddelandet ska bära de CloudEvent-attribut som angavs vid skapandet av uppgiften.
- **OUL-FR-06.4** Kafka-topic ska vara dynamiskt och styras av ett topic-prefix som regler anger vid skapandet av uppgiften, så att flödesprocesser kan prenumerera selektivt på sina egna notifieringar.

### OUL-FR-07 — Uppdatera uppgift (management)

- **OUL-FR-07.1** En Administratör ska kunna uppdatera en befintlig uppgift via `PATCH /uppgifter/{id}`.
- **OUL-FR-07.2** Om specificerad uppgift inte finns ska HTTP 404 returneras.

### OUL-FR-08 — Ta bort tilldelning av uppgift (management)

- **OUL-FR-08.1** En Administratör ska kunna ta bort tilldelningen för en uppgift via `POST /uppgifter/{id}/unassign`.
- **OUL-FR-08.2** En borttagen tilldelning av en uppgift innebär att uppgiften ska återgå till status `NY` och vara tillgänglig för ny tilldelning.
- **OUL-FR-08.3** Om specificerad uppgift inte finns ska HTTP 404 returneras.

### OUL-FR-09 — Skapa sorteringsordning

- **OUL-FR-09.1** En Administratör ska kunna skapa en ny sorteringsordning via management-API.
- **OUL-FR-09.2** Om ingen aktiv-sorteringsordning finns sedan tidigare ska den nyskapade automatiskt anges som aktiv. Finns en aktiv sedan tidigare påverkas den inte.
- **OUL-FR-09.3** OUL ska tilldela ett nytt unikt UUID för varje ny sorteringsordning och returnera det i svaret.
- **OUL-FR-09.4** Sorteringsordningen ska innehålla en ordnad lista med minst en post (`SorteringsordningEntry`).
- **OUL-FR-09.5** Varje post kan innehålla valfria villkor (`constraints`) och en valfri sorteringsangivelse (`sort_by`).

### OUL-FR-10 — Hämta sorteringsordning

- **OUL-FR-10.1** En Administratör ska kunna hämta den aktiva sorteringsordningen via `GET /sorteringsordning/aktiv`.
- **OUL-FR-10.2** Om ingen sorteringsordning finns ska HTTP 404 returneras.
- **OUL-FR-10.3** En Administratör ska kunna hämta en specifik sorteringsordning via dess ID med `GET /sorteringsordning/{id}`.
- **OUL-FR-10.4** Om angivet ID inte hittas ska HTTP 404 returneras.

### OUL-FR-11 — Lista sorteringsordningar

- **OUL-FR-11.1** En Administratör ska kunna lista samtliga sorteringsordningar via `GET /sorteringsordning`.
- **OUL-FR-11.2** `GET /sorteringsordning` ska acceptera parametrarna `limit` (obligatorisk, minst 1) och `offset` (valfri, standard 0).
- **OUL-FR-11.3** Svaret ska vara av typen `SorteringsordningPage` med fälten `total` (antal sorteringsordningar före paginering) och `items` (paginerad lista av sorteringsordningar).

### OUL-FR-12 — Förhandsgranska sorteringsordning

- **OUL-FR-12.1** En Administratör ska kunna skicka in en sorteringsordningsspecifikation för förhandsgranskning via `POST /sorteringsordning/preview`.
- **OUL-FR-12.2** Förhandsgranskningen applicerar specifikationen på befintliga uppgifter och returnerar resultatet utan att persisteringen påverkas.
- **OUL-FR-12.3** Förhandsgranskningen accepterar samma `limit` och `offset` som ordinarie listning.

### OUL-FR-13 — Ta bort sorteringsordning

- **OUL-FR-13.1** En Administratör ska kunna ta bort en sorteringsordning via `DELETE /sorteringsordning/{id}`.
- **OUL-FR-13.2** En sorteringsordning som är angiven som aktiv kan inte tas bort. Om ett sådant försök görs ska HTTP 409 returneras.
- **OUL-FR-13.3** Om angivet ID inte hittas ska HTTP 404 returneras.

### OUL-FR-14 — Ange aktiv sorteringsordning

- **OUL-FR-14.1** En Administratör ska kunna ange en specifik sorteringsordning som aktiv via `PUT /sorteringsordning/{id}/aktiv`.
- **OUL-FR-14.2** Om angivet ID inte hittas ska HTTP 404 returneras.

### OUL-FR-16 — Teammedlemskap via team-API

- **OUL-FR-16.1** Vid behov av att avgöra om en handläggare tillhör ett visst team ska OUL anropa team-tjänstens OpenAPI för att hämta aktuellt teammedlemskap.
- **OUL-FR-16.2** Teammedlemskap ska alltid hämtas från team-API:et vid varje kontroll och får inte cachas på ett sätt som riskerar inaktuella data.

### OUL-FR-17 — Hämta teamets uppgifter (handläggare)

- **OUL-FR-17.1** En handläggare ska kunna hämta alla uppgifter tilldelade till någon av handläggarens teammedlemmar via `GET /uppgifter/team`.
- **OUL-FR-17.2** Handläggarens identitet fastställs från bearer-token.
- **OUL-FR-17.3** OUL avgör handläggarens teamtillhörighet via team-API:et (se OUL-FR-16).
- **OUL-FR-17.4** Om handläggaren inte tillhör ett känt team ska HTTP 403 returneras.
- **OUL-FR-17.5** SID-märkta uppgifter skall endast visas för handläggare med SID-behörighet
- **OUL-FR-17.6** En uppgift som exkluderas enligt OUL-FR-17.5 ska inte räknas i
  `borttagna_pga_behorighet` (OUL-FR-05.5) — ingen avtilldelning sker, uppgiften är bara dold
  för denna anropare.

### OUL-FR-18 — Tilldela om uppgift till anropande handläggare

- **OUL-FR-18.1** En handläggare ska kunna ta över en uppgift som är tilldelad en annan handläggare via `POST /uppgifter/{uppgift_id}/handlaggare`.
- **OUL-FR-18.2** Handläggarens identitet fastställs från bearer-token.
- **OUL-FR-18.3** Omtilldelning är endast tillåten om den nuvarande tilldelade handläggaren tillhör samma team som den anropande handläggaren. Om så inte är fallet ska HTTP 403 returneras.
- **OUL-FR-18.4** Om angiven uppgift inte finns ska HTTP 404 returneras.
- **OUL-FR-18.5** Vid omtilldelning ska en statusnotifiering publiceras på Kafka i enlighet med OUL-FR-06.
- **OUL-FR-18.6** Om uppgiften är SID-märkt och den anropande handläggaren saknar SID-behörighet ska HTTP 403 returneras och uppgiften ska lämnas oförändrad. Om SID-status eller SID-behörighet inte kan avgöras ska 403 returneras (fail-closed).

### OUL-FR-19 — Lämna tillbaka uppgift (handläggare)

- **OUL-FR-19.1** En handläggare ska kunna lämna tillbaka (unassigna) en uppgift som är tilldelad honom/henne via `POST /uppgifter/{uppgift_id}/unassign`.
- **OUL-FR-19.2** Handläggarens identitet fastställs från bearer-token.
- **OUL-FR-19.3** Endast den handläggare uppgiften faktiskt är tilldelad får lämna tillbaka den. Om den anropande handläggaren inte är den tilldelade ska HTTP 403 returneras och uppgiften lämnas oförändrad.
- **OUL-FR-19.4** Om angiven uppgift inte finns ska HTTP 404 returneras.
- **OUL-FR-19.5** En unassign enligt denna endpoint innebär att uppgiften återgår till status `NY` och blir tillgänglig för ny tilldelning (jfr OUL-FR-08.2).
- **OUL-FR-19.6** Den avtilldelande handläggaren läggs till i uppgiftens lista över tidigare avvisande handläggare (se OUL-FR-20).

### OUL-FR-20 — Spärr mot återtilldelning till avvisande handläggare

- **OUL-FR-20.1** OUL ska för varje uppgift kunna hålla en lista av handläggare som tidigare lämnat tillbaka just den uppgiften via OUL-FR-19.
- **OUL-FR-20.2** Vid tilldelning av ny uppgift (OUL-FR-04) ska en uppgift vars avvisandelista innehåller den anropande handläggaren hoppas över, på samma sätt som en SID-blockerad uppgift hoppas över idag (jfr OUL-FR-04.6).
- **OUL-FR-20.3** Listan är en permanent egenskap hos uppgiften — den rensas inte när uppgiften tilldelas någon annan handläggare.
- **OUL-FR-20.4** En handläggare som inte finns i listan påverkas inte av denna spärr.
- **OUL-FR-20.5** Endast unassignade via OUL-FR-19 (handläggarens egen, självbetjänade avtilldelning) lägger till i listan. Administratörens unassign (OUL-FR-08), administratörens uppdatering (OUL-FR-07) och omtilldelning till en annan handläggare (OUL-FR-18) påverkar inte listan.

### OUL-FR-15 — Villkorsutvärdering

- **OUL-FR-15.1** Villkorstypen `eq` innebär att ett fält ska vara exakt lika med angivet värde.
- **OUL-FR-15.2** Villkorstypen `contains` innebär att ett textfält ska innehålla angiven delsträng.
- **OUL-FR-15.3** Villkorstypen `between` innebär att ett datumfält ska ligga inom ett angivet intervall (inklusive).
- **OUL-FR-15.4** Villkorstypen `offset_to_now` innebär att ett datumfält ska ligga inom ett relativt intervall från nu, uttryckt som en offset (t.ex. `-7d` för de senaste 7 dagarna).
- **OUL-FR-15.5** Alla villkor inom en post utvärderas med AND-semantik — samtliga måste vara uppfyllda för att uppgiften ska matcha posten.

---

## Uppgiftsstatus

| Status      | Beskrivning |
|-------------|-------------|
| `NY`        | Uppgiften är skapad men inte tilldelad någon handläggare |
| `TILLDELAD` | Uppgiften är tilldelad en handläggare |
| `AVSLUTAD`  | Uppgiften är utförd och avslutad |
| `AVBRUTEN`  | Uppgiften är avbruten utan att ha utförts |

---

## Icke-funktionella krav

### OUL-NFR-01 — Tillgänglighet

- **OUL-NFR-01.1** Tjänsten ska exponera en liveness-endpoint (`/q/health/live`) för användning av orkestreringsplattformen.

### OUL-NFR-02 — Observerbarhet

- **OUL-NFR-02.1** Alla statustransitioner ska vara spårbara via loggar.

### OUL-NFR-03 — Trådsäkerhet

- **OUL-NFR-03.1** Lagret ska hantera samtidiga anrop korrekt, utan att samma uppgift tilldelas flera handläggare samtidigt.
- **OUL-NFR-03.2** Läs- och skrivoperationer mot den aktiva sorteringsordningen ska vara trådsäkra.

### OUL-NFR-04 — Lagring av uppgifter

- **OUL-NFR-04.1** Data ska lagras persistent och överleva omstart av servicen.
- **OUL-NFR-04.2** Lagringslagret ska stödja individuella CRUD-operationer per uppgift. Varje operation (skapa, hämta, uppdatera, ta bort) ska operera direkt på den berörda uppgiften utan att behöva läsa eller skriva samtliga uppgifter.

### OUL-NFR-05 — Lagring av sorteringsordningar

- **OUL-NFR-05.1** Flera sorteringsordningar ska kunna lagras simultant och överleva omstart. Exakt en är aktiv åt gången.
- **OUL-NFR-05.2** Hämtning av en sorteringsordning via ID ska ske direkt mot den enskilda posten utan att läsa samtliga sorteringsordningar.
- **OUL-NFR-05.3** Byte av aktiv-sorteringsordning ska inte kräva läsning eller skrivning av övriga sorteringsordningar.

---

## API-gränssnitt (översikt)

OUL exponerar tre separata REST-API:er:

| API | Målgrupp | Specifikation |
|-----|----------|---------------|
| Handläggar-API | Handläggare | `rimfrost-service-oul-openapi-jaxrs-spec` |
| Management-API | Administratör | `rimfrost-service-oul-management-api-jaxrs-spec` |
| Regler-API | Regler | `rimfrost-service-oul-management-regler-api-jaxrs-spec` |

Fullständiga API-specifikationer definieras i respektive OpenAPI-repo.

---

## Integration med regler

OUL är designat för att integreras med regelflöden. CloudEvent-attribut skickas transparent
för att möjliggöra korrelation och återkoppling till den anropande flödesprocessen.
