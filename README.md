# rimfrost-service-oul

Operativt uppgiftslager (OUL) — en Quarkus-tjänst som hanterar livscykeln för handläggaruppgifter inom Rimfrost-plattformen.

## Domänbegrepp

En **uppgift** representerar ett arbetsärende som ska handläggas. Den skapas av ett upstream-system (via regler-API:et), tilldelas en handläggare och avslutas när handläggningen är klar.

| Begrepp | Beskrivning |
|---|---|
| `handlaggning_id` | Kopplar uppgiften till en specifik handläggning |
| `handlaggar_id` | Identifierar den handläggare som äger uppgiften (typ + värde) |
| `regel` | Regelnamnet som triggade uppgiften |
| `roll` | Vilken handläggarroll uppgiften riktar sig till |
| `erbjudande` | Det erbjudande uppgiften är kopplad till |
| `individer` | De individer (personnummer el. liknande) ärendet rör |
| `sub_topic` | Kafka-topic-suffix för statusnotifieringar |

### Uppgiftsstatus

```
NY → TILLDELAD → AVSLUTAD
          ↓
       (avbruten)
```

| Status | Innebörd                        |
|---|---------------------------------|
| `NY` | Skapad, väntar på tilldelning   |
| `TILLDELAD` | Tilldelad en handläggare        |
| `AVSLUTAD` | Slutförd med en orsak           |
| `AVBRUTEN` | Avbruten utan normal avslutning |

## API

Tjänsten exponerar två API-ytor baserade på genererade OpenAPI-specifikationer.

### Handläggare-API (`/uppgifter/handlaggare`)

| Metod | Sökväg | Beskrivning                             |
|---|---|-----------------------------------------|
| `GET` | `/uppgifter/handlaggare/{id_typ}/{handlaggar_id}` | Hämta alla uppgifter för en handläggare |
| `POST` | `/uppgifter/handlaggare/{id_typ}/{handlaggar_id}` | Tilldela nästa icke tilldelade uppgift  |
| `DELETE` | `/uppgifter/{uppgiftId}/handlaggare` | Avsluta tilldelning av uppgiften        |
| `PATCH` | `/uppgifter/{uppgiftId}` | Uppdatera handläggartilldelning         |
| `POST` | `/uppgifter/{uppgiftId}/end` | Avsluta uppgift med orsak               |

### Management-API

| Metod | Sökväg | Beskrivning                                         |
|---|---|-----------------------------------------------------|
| `POST` | `/uppgifter` | Skapa uppgift                                       |
| `GET` | `/uppgifter` | Lista uppgifter (paginerat, med sortering)          |
| `POST` | `/sorteringsordning` | Skapa sorteringsordning                             |
| `GET` | `/sorteringsordning` | Lista alla sorteringsordningar                      |
| `GET` | `/sorteringsordning/default` | Hämta aktiv standardsortering (404 om ingen finns)  |
| `GET` | `/sorteringsordning/{id}` | Hämta specifik sorteringsordning                    |
| `DELETE` | `/sorteringsordning/{id}` | Ta bort sorteringsordning (409 om den är standard)  |
| `PUT` | `/sorteringsordning/{id}/default` | Sätt som ny default                                 |
| `POST` | `/sorteringsordning/preview` | Förhandsgranska en sorteringsordning utan att spara |

`GET /uppgifter` tar parametrarna `limit` (obligatorisk), `offset` (standard 0) och `sorteringsordningId` (valfri — standardsorteringen används om den saknas).

## Sortering

Sorteringen av uppgifter styrs av en **sorteringsordning** — en konfiguration som definierar hur uppgifter ska grupperas och ordnas för handläggare.

### Struktur

En sorteringsordning består av en ordnad lista av **entries**. Varje entry har:

- **constraints** — ett antal villkor som avgör om en uppgift hör hemma i den här gruppen
- **sort_by** — hur uppgifter inom gruppen ska ordnas (fält + riktning)

Utvärderingen sker med **first-match-wins**: den första entryn vars samtliga constraints matchar vinner. En entry utan constraints är en *catch-all* och matchar alltid.

### Constraint-typer

| Typ | Fält | Exempel |
|---|---|---|
| `eq` | `status`, `regel`, `roll`, `beskrivning`, `verksamhetslogik`, `uppgift_id` | `{"type":"eq","field":"status","value":"NY"}` |
| `contains` | Valfritt strängfält | `{"type":"contains","field":"beskrivning","value":"brådskande"}` |
| `between` | `skapad`, `planerad_till` | `{"type":"between","field":"skapad","from":"2026-01-01","to":"2026-06-30"}` |
| `offset_to_now` | `skapad`, `planerad_till` | `{"type":"offset_to_now","field":"planerad_till","offset":"-7d"}` |

`offset_to_now` matchar uppgifter vars datum faller mellan `nu + offset` och `nu`. Offset-format: `±Nd` (dagar), `±Nw` (veckor), `±Nm` (månader), `±Ny` (år). Negativt värde = bakåt i tid.

### Exempel — sorteringsordning med två grupper

```json
{
  "entries": [
    {
      "constraints": [
        {"type": "eq", "field": "status", "value": "NY"},
        {"type": "offset_to_now", "field": "planerad_till", "offset": "-7d"}
      ],
      "sort_by": {"field": "planerad_till", "direction": "ASC"}
    },
    {
      "sort_by": {"field": "skapad", "direction": "ASC"}
    }
  ]
}
```

Första gruppen: Nya uppgifter med förfallen planerad tid, sorterade äldst förfallodag först.
Andra gruppen (catch-all): Alla övriga uppgifter, sorterade i ankomstordning.

### Implementation

Sorteringen sker helt i databasen via en dynamiskt byggd SQL-fråga (se `SorteringsordningQueryBuilder`). Varje rad tilldelas en sortgrupp via ett `CASE WHEN`-uttryck; sedan sorteras raderna på grupp, per-grupps sort_by-fält och sist `created_at` som stabilt tiebreaker.

```sql
SELECT ... FROM (
  SELECT *, CASE
    WHEN (<constraints entry 0>) THEN 0
    WHEN (<constraints entry 1>) THEN 1
    ELSE 2
  END AS sort_group
  FROM operativt_uppgiftslager.uppgift
) AS u
ORDER BY u.sort_group ASC,
         CASE WHEN u.sort_group = 0 THEN u.planerad_till END ASC,
         CASE WHEN u.sort_group = 1 THEN u.skapad END ASC,
         u.created_at ASC
```

Paginering sker via JPA:s `setFirstResult`/`setMaxResults` — SQL-frågorna innehåller inga `LIMIT`/`OFFSET`-litteraler.

### Förhandsgranskning

`POST /sorteringsordning/preview` kör exakt samma SQL-logik som `GET /uppgifter`, men mot en tillfällig (osparad) sorteringsordning. Det garanterar att preview och verklig sortering alltid ger identiskt resultat.

## Lagring

**Databas**: PostgreSQL
**Schema**: `operativt_uppgiftslager`
**Migrationer**: Flyway (körs automatiskt vid uppstart)

### Tabeller

| Tabell | Beskrivning |
|---|---|
| `uppgift` | Uppgifternas huvuddata |
| `uppgift_individ` | Kopplade individer (en uppgift → många individer) |
| `uppgift_cloud_event_attribute` | CloudEvents-metadata per uppgift |
| `sorteringsordning` | Sparade sorteringsordningar (entries lagras som JSON) |
| `default_sorteringsordning` | En-rads-tabell som pekar ut aktiv standardsortering |

## Kafka

Tjänsten publicerar statusnotifieringar när en uppgift tilldelas, frånsägs eller uppdateras.

**Topic-bas**: `operativt-uppgiftslager-status-notification`
**Fullt topic**: `operativt-uppgiftslager-status-notification.<subTopic>`

`subTopic` sätts per uppgift vid skapandet och styr vilket topic notifieringen hamnar på.

## Konfiguration

| Property | Beskrivning |
|---|---|
| `quarkus.flyway.default-schema` | Databasschema (standard: `operativt_uppgiftslager`) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Databasanslutning (prod, via miljövariabler) |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka-anslutning (prod, via miljövariabler) |
| `fullmakts.api.base-url` | Bas-URL till fullmakts-API:et |
| `oul.uppgift.count-cache-ttl-ms` | TTL i millisekunder för `COUNT(*)`-cachen (standard: `5000`) |

I `dev`- och `test`-profil startas PostgreSQL och Kafka automatiskt som Dev Services-containrar.

## Bygga och köra

```bash
# Kör i utvecklingsläge (med live reload)
./mvnw quarkus:dev

# Kör tester
./mvnw test

# Bygg körbar JAR
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Standardport: **8080**
