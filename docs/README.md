# docs/

Denna katalog innehåller arbetsdokument som stödjer implementation av nya funktioner i tjänsten.

## Syfte

Dokumenten är **inte** formella kravspecifikationer. De är dokumenterade tolkningar av externa krav, API-kontrakt och designbeslut — formulerade på ett sätt som gör det enkelt att förstå vad som ska implementeras och varför. Huvudsyftet är att underlätta AI-assisterad implementation genom att samla kontext, avgränsningar och beslut på ett ställe.

## Dokumenttyper

### `krav-*.md`

Beskriver funktionella och icke-funktionella krav för ett specifikt område eller fas. Innehållet är en tolkning av indata (Jira, OpenAPI-spec, diskussioner) — inte dokumenterade systemkrav. Avvikelser från den fullständiga specifikationen noteras explicit.

### `plan-*.md`

Beskriver implementationsplanen för ett specifikt Jira-ärende. Innehåller t.ex. vilka filer som ska skapas eller ändras samt designbeslut. Namnkonvention: `plan-<jiraid>.md` (t.ex. `plan-FKPOC-795.md`).
