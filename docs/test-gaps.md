# Testtäckningsluckor — Operativt Uppgiftslager (OUL)

Identifierade krav utan testtäckning per 2026-06-01.

| Krav | Beskrivning | Notering |
|---|---|---|
| FR-04.4 | Tomt svar (utan felkod) returneras när inga otilldelade uppgifter finns | Inget test anropar hämta-uppgift när kön är tom |
| FR-06.4 | Kafka-topic styrs dynamiskt av det ämnesprefix som regler anger vid skapandet | Tester verifierar att ett meddelande publiceras men kontrollerar inte topic-namnet |
| NFR-03.1 | Samtidiga tilldelningsanrop leder inte till att samma uppgift tilldelas flera handläggare | Kräver ett concurrency-test; pessimistiskt lås finns i implementationen men är inte verifierat under last |
| NFR-04.1 | Data överlever omstart av servicen | Tester kör mot en databas men verifierar inte att data finns kvar efter omstart av applikationscontexten |
