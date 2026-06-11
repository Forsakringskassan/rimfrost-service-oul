# Testtäckningsluckor — Operativt Uppgiftslager (OUL)

Identifierade krav utan testtäckning per 2026-06-09.

| Krav | Beskrivning | Notering |
|------|-------------|----------|
| OUL-NFR-02.1 | Alla statustransitioner är spårbara via loggar | Svårt att verifiera i automatiserade tester; kräver manuell granskning eller log-assertion-ramverk. |
| OUL-NFR-03.1 | Samtidiga tilldelningsanrop leder inte till att samma uppgift tilldelas flera handläggare | Kräver ett concurrency-test; pessimistiskt lås finns i implementationen men är inte verifierat under last. |
| OUL-NFR-04.1 | Data överlever omstart av servicen | Tester kör mot en databas men verifierar inte att data finns kvar efter omstart av applikationscontexten. |
