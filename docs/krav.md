# Krav — Operativt Uppgiftslager (OUL)

## Bakgrund och syfte

Operativt Uppgiftslager (OUL) är en central komponent i handläggningsflödet för Försäkringskassan.
Syftet är att fungera som en operativ kö för manuella arbetsuppgifter som uppstår när regler
identifierar ett behov av manuell handläggning. OUL håller reda på vilka uppgifter som
finns, vilken handläggare som är tilldelad uppgiften, och informerar intressenter när en uppgift byter status.

---

## Intressenter och aktörer

| Aktör          | Roll |
|----------------|---|
| Regler         | Skapar och avslutar uppgifter via management-API |
| Handläggare    | Hämtar och utför tilldelade uppgifter via handläggar-API |
| Regler         | Prenumererar på statusnotifieringar via Kafka |
| Administratör  | Övervakar och administrerar samtliga uppgifter i lagret via management-API |

---

## Funktionella krav

### FR-01 — Skapa uppgift

- **FR-01.1** Regler ska kunna skapa en ny operativ uppgift i OUL.
- **FR-01.2** En ny uppgift ska tilldelas status `NY` vid skapandet.
- **FR-01.3** Uppgiften ska knytas till ett handläggningsärende.
- **FR-01.4** Uppgiften ska innehålla:
  - berörda individer
  - vilken regel som triggade uppgiften
  - roll som ska utföra uppgiften
  - beskrivning
  - URL till regelns tjänst som portalen använder
- **FR-01.5** Uppgiften ska vara kopplad till ett erbjudande.
- **FR-01.6** Uppgiften ska innehålla CloudEvent-attribut som OUL bevarar och returnerar oförändrade, för att möjliggöra korrelation mellan regeln och dess flödesprocess.
- **FR-01.7** OUL ska returnera ett unikt uppgifts-ID vid skapandet.

### FR-02 — Avsluta uppgift

- **FR-02.1** Regler ska kunna avsluta en uppgift med angiven orsak.
- **FR-02.2** En avslutad uppgift ska tas bort permanent.
- **FR-02.3** OUL ska returnera uppgiftens senaste status vid avslut.
- **FR-02.4** Om specificerad uppgift inte finns ska ett tydligt felmeddelande returneras (HTTP 404).

### FR-03 — Lista alla uppgifter (management)

> Utökad av **FR-11** i [krav-sortorder-inmemory.md](krav-sortorder-inmemory.md) — den uppdaterade signaturen med paginering och sorteringsordning gäller.

- **FR-03.1** Det ska finnas ett gränssnitt för att lista samtliga uppgifter i lagret, oavsett status.
- **FR-03.2** Gränssnittet är avsett för Administratörer och driftövervakning.
- **FR-03.3** En Administratör ska kunna se samtliga uppgifter oavsett tilldelningsstatus, inklusive handläggaridentitet för tilldelade uppgifter.

### FR-04 — Hämta ny uppgift (handläggare)

- **FR-04.1** En handläggare ska kunna begära en ny icke tilldelad uppgift och automatiskt bli tilldelad den.
- **FR-04.2** OUL väljer den första tillgängliga icke tilldelade uppgiften och sätter status till `TILLDELAD`.
- **FR-04.3** Handläggarens identitet anges via en typad identifierare bestående av identifierartyp och handläggar-ID.
- **FR-04.4** Om ingen icke tilldelad uppgift finns ska ett tomt svar returneras utan felkod.
- **FR-04.5** Vid tilldelning ska en statusnotifiering publiceras på Kafka.

### FR-05 — Lista tilldelade uppgifter (handläggare)

- **FR-05.1** En handläggare ska kunna lista alla uppgifter som är tilldelade till honom/henne.
- **FR-05.2** Listan filtreras på handläggarens identitet.

### FR-06 — Statusnotifiering via Kafka

- **FR-06.1** När en uppgift tilldelas en handläggare ska OUL publicera ett meddelande på Kafka.
- **FR-06.2** Meddelandet ska innehålla:
  - handläggningsärendets ID
  - uppgiftens ID
  - den tilldelade handläggarens ID
  - uppgiftens status
- **FR-06.3** Meddelandet ska bära de CloudEvent-attribut som angavs vid skapandet av uppgiften.
- **FR-06.4** Kafka-topic ska vara dynamiskt och styras av ett ämnesprefix som regler anger vid skapandet av uppgiften, så att flödesprocesser kan prenumerera selektivt på sina egna notifieringar.

### FR-07 — Uppdatera uppgift (management)

- **FR-07.1** En Administratör ska kunna uppdatera en befintlig uppgift via `PATCH /uppgifter/{id}`.
- **FR-07.2** Om specificerad uppgift inte finns ska HTTP 404 returneras.

> Ej implementerat — returnerar 501 tills vidare.

### FR-08 — Avdela uppgift (management)

- **FR-08.1** En Administratör ska kunna ta bort tilldelningen för en uppgift via `POST /uppgifter/{id}/unassign`.
- **FR-08.2** En avdelad uppgift ska återgå till status `NY` och vara tillgänglig för ny tilldelning.
- **FR-08.3** Om specificerad uppgift inte finns ska HTTP 404 returneras.

> Ej implementerat — returnerar 501 tills vidare.

---

## Uppgiftsstatus

| Status | Beskrivning |
|---|---|
| `NY` | Uppgiften är skapad men inte tilldelad någon handläggare |
| `TILLDELAD` | Uppgiften är tilldelad en handläggare |
| `AVSLUTAD` | Uppgiften är utförd och avslutad |
| `AVBRUTEN` | Uppgiften är avbruten utan att ha utförts |

---

## Icke-funktionella krav

### NFR-01 — Tillgänglighet

- **NFR-01.1** Tjänsten ska exponera en liveness-endpoint (`/q/health/live`) för användning av orkestreringsplattformen.

### NFR-02 — Observerbarhet

- **NFR-02.1** Alla statustransitioner ska vara spårbara via loggar.

### NFR-03 — Trådsäkerhet

- **NFR-03.1** Lagret ska hantera samtidiga anrop korrekt, utan att samma uppgift tilldelas flera handläggare samtidigt.

### NFR-04 — Lagring

- **NFR-04.1** Data ska lagras persistent och överleva omstart av servicen.

---

## API-gränssnitt (översikt)

OUL exponerar två separata REST-API:er under samma endpoint `/uppgifter`:

| API | Målgrupp | Specifikation |
|---|---|---|
| Handläggar-API | Handläggare | `rimfrost-service-oul-openapi-jaxrs-spec` |
| Management-API | Regler / administration | `rimfrost-service-oul-management-api-jaxrs-spec` |

Fullständiga API-specifikationer definieras i respektive OpenAPI-repo.

---

## Integration med regler

OUL är designat för att integreras med regelflöden. CloudEvent-attribut skickas transparent
för att möjliggöra korrelation och återkoppling till den anropande flödesprocessen.
