# docs/

Denna katalog innehåller arbetsdokument som stödjer implementation av nya funktioner i tjänsten.

## Syfte

Dokumenten är **inte** formella kravspecifikationer. De är dokumenterade tolkningar av externa krav, API-kontrakt och designbeslut — formulerade på ett sätt som gör det enkelt att förstå vad som ska implementeras och varför. Huvudsyftet är att underlätta AI-assisterad implementation genom att samla kontext, avgränsningar och beslut på ett ställe.

## Struktur

Varje Jira-ärende har en egen underkatalog med namnet `<jiraid>/` (t.ex. `FKPOC-833/`). Alla dokument som hör till ett ärende samlas där.

```
docs/
├── FKPOC-795/
│   └── plan.md
├── FKPOC-833/
│   ├── plan.md
│   ├── ac-changes.md
│   ├── open-questions.md
│   └── summary.md
├── krav.md
├── krav-backlog.md
└── ...
```

Dokument som inte tillhör ett specifikt ärende (t.ex. övergripande krav eller tekniska specifikationer) ligger direkt under `docs/`.

## Dokumenttyper

### `krav-*.md`

Beskriver funktionella och icke-funktionella krav för ett specifikt område eller fas. Innehållet är en tolkning av indata (Jira, OpenAPI-spec, diskussioner) — inte dokumenterade systemkrav. Avvikelser från den fullständiga specifikationen noteras explicit.

### `<jiraid>/plan.md`

Beskriver implementationsplanen för ett specifikt Jira-ärende. Innehåller numrerade steg, designbeslut och vilka filer som ska skapas eller ändras. Varje steg markeras `[x]` när det är klart.

### Övriga filer i en ärende-katalog

| Filnamn | Typiskt innehåll |
|---|---|
| `ac-changes.md` | Avvikelser från eller förtydliganden av acceptanskriterier |
| `open-questions.md` | Öppna frågor och svar som uppstod under implementationen |
| `summary.md` | Sammanfattning av vad som levererades |
| `fix.md` | Beskrivning av en buggfix eller korrigering |
