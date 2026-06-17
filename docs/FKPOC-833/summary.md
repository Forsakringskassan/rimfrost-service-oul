# FKPOC-833

**Summary**
Tilldelning av uppgift från annan handläggare

**Description**
En handläggare ska kunna tilldela sig en uppgift från en annan handläggare i sitt team. Handläggarens team-medlemmar hämtas via befintligt service-api.

**Scope**
- Repo: `rimfrost-service-oul`
- OpenAPI spec: `rimfrost-service-oul-openapi`
- Befintliga operationer: `getUppgifterHandlaggare`, `postUppgifterHandlaggare`
- Utökad operation: `GET /uppgifter/handlaggare/{id_typ}/{id_varde}` — utökas med team-auktorisering (tillåts för valfri handläggare i samma team)
- Ny operation för team-översikt: `GET /uppgifter/team/{id_typ}/{id_varde}`
- Ny operation för tilldelning: `POST /uppgifter/{uppgift_id}/handlaggare/{id_typ}/{id_varde}`
- Team-medlemmar API: TBD
- Out of scope: administration av team-konstellationer, notifiering till av-tilldelad handläggare

**Data model**
Inga ändringar av befintliga scheman. Handläggare identifieras via path-parametrar `id_typ` / `id_varde`

**Dependencies / integration points**
- Team-medlemmar REST-api: TBD

**Acceptance Criteria**
```
- AC1: GET /uppgifter/team/{id_typ}/{id_varde} returnerar 200 med alla uppgifter tilldelade till handläggare inom samma team
- AC2: GET /uppgifter/handlaggare/{id_typ}/{id_varde} returnerar 200 även när den angivna handläggaren är en annan än den anropande, förutsatt att de är i samma team
- AC3: GET /uppgifter/handlaggare/{id_typ}/{id_varde} returnerar 403 om den angivna handläggaren är utanför det egna teamet
- AC4: Listan av team-medlemmar hämtas via befintligt REST-api — inga hårdkodade team-konstellationer
- AC5: POST /uppgifter/{uppgift_id}/handlaggare/{id_typ}/{id_varde} tilldelar uppgiften till den angivna handläggaren och returnerar 200 med uppdaterad OperativUppgift
- AC6: Efter tilldelning är uppgiften tilldelad den nya handläggaren och ska inte längre synas som tilldelad den tidigare
- AC7: POST /uppgifter/{uppgift_id}/handlaggare/{id_typ}/{id_varde} returnerar 403 om uppgiften är tilldelad en handläggare utanför det egna teamet
- AC8: Notifiering till den tidigare tilldelade handläggaren sker ej
- AC9: Administration av team-konstellationer är out-of-scope och hanteras ej av denna feature
- AC10: En handläggare som ej längre är tilldelad en uppgift kan inte uppdatera den uppgiften
```
