# FKPOC-833 — Open Questions

## Spec & endpoints
- [x] Vilket OpenAPI spec-repo gäller? `rimfrost-service-oul-openapi`
- [x] Vilken path och operation för att lista team-uppgifter? → `GET /uppgifter/team/{id_typ}/{id_varde}` (ny) + utökad auktorisering på befintlig `GET /uppgifter/handlaggare/{id_typ}/{id_varde}`
- [x] Vilken path och operation för att tilldela om en uppgift? → `POST /uppgifter/{uppgift_id}/handlaggare/{id_typ}/{id_varde}` (ny operation i spec)

## Data model
- [x] Vilka fält har en `Uppgift`? → `OperativUppgift` i spec: uppgift_id, handlaggning_id, status, skapad, handlaggar_id (Idtyp), m.fl.
- [x] Hur representeras tilldelning? → `handlaggar_id` (Idtyp: typId + varde) på `OperativUppgift`

## Team-members API
- [ ] Vilket API/endpoint hämtar team-medlemmar? (TBD i scope)
- [ ] Vilken befintlig REST-klient eller bean används för att anropa detta API?
- [ ] Vad händer om team-members API är otillgängligt?

## Business rules
- [x] Hur identifieras handläggaren? → via path-parametrar `id_typ` / `id_varde`, ej JWT-claim direkt
- [ ] Hur identifieras handläggarens team — via samma id_typ/id_varde mot team-API?

## Auth & felhantering
- [ ] Vilket HTTP-statuskod returneras när handläggare försöker tilldela sig uppgift från annat team? (AC5 — 403?)
- [ ] Vilket HTTP-statuskod returneras när av-tilldelad handläggare försöker uppdatera uppgiften? (AC8 — 403?)

## Dependencies
- [ ] Finns befintligt `UppgiftRepository` eller liknande att återanvända?
