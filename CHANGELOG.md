# rimfrost-service-oul changelog

Changelog of rimfrost-service-oul.

## 1.3.0 (2026-06-22)

### Features

-  Use PostgreSQL row-constructor IN for buildTeamListQuery WHERE clause ([c4b98](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/c4b986b20e46b52) Ulf Slunga)  
-  Replace null-return not-found pattern with typed exceptions across all layers ([e011f](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/e011ff80575ec4d) Ulf Slunga)  
-  Remove superseded FKPOC-833 planning docs ([09d38](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/09d386b5ab603e3) Ulf Slunga)  
-  Replace try/catch in SorteringController with ExceptionMappers ([c7036](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/c70361e7ba8ea49) Ulf Slunga)  
-  Reorganise docs into per-ticket subdirectories ([2294f](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/2294f6f9057bdd1) Ulf Slunga)  

### Bug Fixes

-  Replace jaxrsclientfactory dependency ([ce9d1](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/ce9d19de8dd479f) Lars Persson)  

## 1.2.0 (2026-06-12)

### Features

-  Wire sorteringsordning storage to DB via JPA repositories ([c8fb0](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/c8fb0685eda5f67) Ulf Slunga)  
-  Reduce to 2 items instead of 1000 in test of assigned uppgifter ([b76b3](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/b76b3747a81aaf8) Ulf Slunga)  
-  Replace fully qualified java.util.UUID with imported UUID in OulTestBase ([bf771](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/bf77189a00934da) Ulf Slunga)  
-  Remove getUppgifter() shim, use getUppgifter(int) directly ([1c022](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/1c022d9afce1a57) Ulf Slunga)  
-  Fix indentation in OulDataStorage ([57437](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/57437ecf97f6d24) Ulf Slunga)  
-  Defensive copy in SortedUppgiftPage to avoid mutable list exposure ([9357e](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/9357e0031e35a05) Ulf Slunga)  
-  Defensive copy in SorteringsordningEntity to avoid mutable list exposure ([4fa2d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/4fa2d628b515d25) Ulf Slunga)  
-  Add docs — krav, plans and README for FKPOC-795 ([3fac3](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/3fac39a915b2224) Ulf Slunga)  
-  Add support for unassigning and reassigning tasks ([c08eb](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/c08ebe62f7393be) Lars Persson)  

### Bug Fixes

-  Use minimal page limits in OulSorteringTest ([b09c7](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/b09c752807098f3) Ulf Slunga)  
-  Use minimal page limits in OulPreviewTest ([5d80e](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/5d80edc9236d65e) Ulf Slunga)  
-  Add pagination to GET /sorteringsordning ([78218](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/78218e25805f8ac) Ulf Slunga)  
-  Make invalidateCountCache package-private, move StorageTestCleaner to storage.internal ([e37f1](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/e37f1c4317427fa) Ulf Slunga)  
-  Replace two-phase assign with CTE and parameterise ORDER BY alias ([09af9](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/09af950bd7605e0) Ulf Slunga)  
-  Apply default sorteringsordning to handläggare assign and list endpoints ([f3a28](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/f3a282450c794f7) Ulf Slunga)  
-  Document OUL-FR-04.2 and OUL-FR-05.3 requirements for sorteringsordning-aware handläggare endpoints ([3bcfd](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/3bcfd7964f662f6) Ulf Slunga)  
-  Remove dead getTasks/findAllUppgifter replaced by paginated getUppgifterPage ([aa739](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/aa7391bdbdf6c60) Ulf Slunga)  
-  Push sorting and pagination to DB, add count cache, remove in-memory sort ([6de0a](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/6de0a962547ba4b) Ulf Slunga)  
-  Fix constraint REST deserialization and default-sorting tests ([e39c6](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/e39c630642f2e48) Ulf Slunga)  
-  Test unknown constraint operator throws IllegalArgumentException ([33d7e](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/33d7e7016ce7caa) Ulf Slunga)  
-  Add Javadoc to UppgiftEntity.replyTopic explaining distinction from subTopic ([690c8](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/690c89bfcc93f32) Ulf Slunga)  
-  Move toSorteringsordningEntity to OulDataStorageMapper ([7bb84](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/7bb84342af23819) Ulf Slunga)  
-  Add replyTopic field to uppgift — store and propagate from processInfo ([cefe6](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/cefe6f0c4496ecb) Ulf Slunga)  
-  Read sub_topic from CreateUppgiftRequest instead of processInfo.replyTopic ([34640](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/34640f18b8db889) Ulf Slunga)  
-  fix test gaps, refine requirements and rebasing before start impl of sortorder in db ([982c2](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/982c2b1a4071093) Ulf Slunga)  
-  Bump asyncapi to 1.1.1 and adapt to ProcessInfo wrapper ([5b623](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/5b623fca69319c3) Ulf Slunga)  
-  Adapt to ProcessInfo wrapper in regler API 0.0.3 ([d8c98](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/d8c987813a721ca) Ulf Slunga)  
-  Use rimfrost-service-oul-management-regler-openapi ([cd6ce](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/cd6cee43b1af3e3) Lars Persson)  

## rimfrost-1.1 (2026-06-02)

### Bug Fixes

-  Bump rimfrost-service-oul-asyncapi version ([831a0](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/831a03dd4ac278c) Lars Persson)  
-  Untrack test-gaps.md ([59612](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/596120753dac575) Ulf Slunga)  
-  Remove FR-02.4 from test gaps — now covered by should_return_404 test ([b9d35](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/b9d357b38025a28) Ulf Slunga)  
-  Add @DisplayName requirement traceability to tests and refine krav.md ([22466](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/224662ae5290e38) Ulf Slunga)  
-  krav-förbättring ([8ae97](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/8ae97a91ce488c3) Ulf Slunga)  
-  Add reverse-engineered requirements baseline ([b4df4](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/b4df4a57e6c7296) Ulf Slunga)  

### Other changes

**Remove unused imports**


[6d83f](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/6d83f1e942e46c2) Lars Persson *2026-06-02 12:25:33*


## 1.1.0 (2026-06-01)

### Features

-  Add support for database-backed persistent storage ([98885](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/98885dc160bfdd6) Lars Persson)  

## 1.0.3 (2026-05-21)

### Bug Fixes

-  Add support for erbjudande information ([9776d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/9776da08c3b7517) Lars Persson)  
-  Add support for listing OUL uppgifter ([d1fb0](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/d1fb0f4df23362f) Lars Persson)  
-  Cleanup unnecessary use of ParameterizedTest in OulManagementTest suite ([06b42](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/06b428ea190642f) Lars Persson)  
-  Refactor OulTest.java to multiple suites ([ef540](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/ef540bf94c8d3d6) Lars Persson)  
-  Consistently return a list on handlaggare GET operation ([0442a](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/0442a2d57d255e0) Lars Persson)  
-  Do not send kafka status notification on uppgift end ([cb5c8](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/cb5c8afb241130a) Lars Persson)  
-  Update rimfrost-service-oul-management-openapi to correct version ([9edd6](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/9edd6e904ca0ad3) Lars Persson)  
-  kafka uppgift end/status-control replaced by rest api ([fb15d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/fb15d10ae5af73c) Ulf Slunga)  
-  kafka uppgift create/response replaced by rest api ([8b0c6](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/8b0c6902f65bb50) Ulf Slunga)  

## 1.0.2 (2026-05-08)

### Bug Fixes

-  hanterar status Avbruten och tar bort uppgiften ([16658](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/16658fbaf1a4129) Ulf Slunga)  

## 1.0.1 (2026-05-07)

### Bug Fixes

-  lägger till status Avbruten ([71b2a](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/71b2a1f64e41b5c) Ulf Slunga)  
-  hanterar cloudevent_attributes ([b914a](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/b914a615ea326db) Ulf Slunga)  
-  **deps**  update dependency se.fk.maven:fk-maven-quarkus-parent to v1.12.0 ([17512](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/175122a47e4a7d5) renovate[bot])  

## 1.0.0 (2026-04-28)

### Breaking changes

-  release 1.0 ([cb86d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/cb86d0ba60b4121) Lars Persson)  

### Features

-  release 1.0 ([cb86d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/cb86d0ba60b4121) Lars Persson)  

### Bug Fixes

-  Bump to released api versions ([00bd3](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/00bd3da3a90d920) Lars Persson)  

### Dependency updates

- update dependency org.mockito:mockito-junit-jupiter to v5.23.0 ([30841](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/3084162e60724c9) renovate[bot])  
## 0.2.6 (2026-04-14)

### Bug Fixes

-  Bump rimfrost-service-oul-asyncapi and rimfrost-service-oul-openapi versions ([9f826](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/9f826b1c4a1f7cb) Lars Persson)  

## 0.2.5 (2026-04-09)

### Bug Fixes

-  Bump rimfrost-service-oul-asyncapi and rimfrost-service-oul-openapi versions ([e9603](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/e96033b9f4075bc) Lars Persson)  

## 0.2.4 (2026-03-25)

### Bug Fixes

-  Add basic test for verifying OUL functionality ([19a9a](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/19a9a129979b203) Lars Persson)  
-  hanterar individer ist för yrkande ([99ada](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/99ada930d542bbd) Ulf Slunga)  

### Dependency updates

- update dependency org.mockito:mockito-junit-jupiter to v5.22.0 ([0b2be](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/0b2bedaa15cfb84) renovate[bot])  
## 0.2.3 (2026-03-03)

### Bug Fixes

-  renaming handlaggning ([5e626](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/5e626f00a1c5fa8) Ulf Slunga)  

## 0.2.2 (2026-03-03)

### Bug Fixes

-  Bump to trigger release flow ([34ea8](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/34ea803ae571169) Lars Persson)  

## 0.2.1 (2026-02-06)

### Bug Fixes

-  extract final variables to properties ([96e4e](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/96e4e46c5154008) Nils Elveros)  
-  add utforarId to status update ([21237](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/2123715c3c6414a) Nils Elveros)  

### Other changes

**Send OULresponse and StatusNotifcation on replyToSubTopic**


[6221d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/6221d8f00a5c1b7) Nils Elveros *2026-02-06 09:19:32*


## 0.2.0 (2026-01-15)

### Bug Fixes

-  add health check to app ([5dd5f](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/5dd5f65c9f542ee) Nils Elveros)  

## 0.1.1 (2025-12-18)

### Bug Fixes

-  Döper om Uppgift till OperativUppgift ([3d67b](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/3d67bbf09c6fb7e) Ulf Slunga)  

### Dependency updates

- update dependency org.mockito:mockito-junit-jupiter to v5.21.0 ([33af8](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/33af87e9b85a538) renovate[bot])  
## 0.1.0 (2025-12-11)

### Features

-  update to new openapi and asyncapi specifications ([d0367](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/d0367d8f636dfab) Nils Elveros)  

### Bug Fixes

-  Hantera nullable uppgift ([dbac7](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/dbac7b9c370a12c) Ulf Slunga)  
-  missing regeltyp and dont send status update before response ([58d21](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/58d218206f15482) Nils Elveros)  
-  spotless ([53226](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/5322674e9dfdd3b) Nils Elveros)  
-  kafka configuration for all topics ([177d8](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/177d8867d0ed489) Nils Elveros)  
-  Avslut changed to a more general StatusUpdate ([da756](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/da7568aa53639e4) rikrhen)  
-  Avslut sends update through proper channel ([1af1e](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/1af1ef6408fd892) rikrhen)  
-  spotless... ([01038](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/01038025193faae) rikrhen)  
-  added required time parameter to mappers ([41f6f](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/41f6f3cb48aacab) NilsElveros)  
-  added required time parameter to mappers ([22ee7](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/22ee7a2a4aadfac) Nils Elveros)  
-  change name of misspelled class ([78117](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/781179e01f4f30a) David Söderberg)  
-  add docker dir ([46177](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/461777b85063ef6) Ulf Slunga)  

### Dependency updates

- update dependency org.mockito:mockito-junit-jupiter to v5.20.0 ([ffabd](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/ffabd681fdf2f06) renovate[bot])  
- add renovate.json ([749bf](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/749bff0de18ee2a) renovate[bot])  
### Other changes

**Add kundbehovsflodeId to OulStatusMessage**


[f52cf](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/f52cf904bc0468b) Nils Elveros *2025-12-08 10:05:22*

**spotless**


[bc147](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/bc147499239111a) rikrhen *2025-11-12 15:27:37*

**spotless apply**


[c146d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/c146d603ad8a458) Ulf Slunga *2025-11-10 13:58:59*

**use generated code from openapi specs**


[f5cf1](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/f5cf15e943126af) Ulf Slunga *2025-11-10 13:53:50*

**Updated to use the generated models for kafka messages**


[4d8c9](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/4d8c9f77975a033) Nils Elveros *2025-11-07 14:00:08*


## 0.0.1 (2025-11-03)

### Features

-  add bandaid logic to main service of uppgiftslager ([9c63d](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/9c63d74deeebf7c) David Söderberg)  
-  add rest controller ([be71c](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/be71c3fc791a0b5) David Söderberg)  
-  add initial structure for uppgiftslager ([0d410](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/0d410ee5c5e59a8) David Söderberg)  

### Bug Fixes

-  run spotless apply to fix code structure ([02aab](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/02aab748b27fe06) David Söderberg)  
-  update classes to make it work ([6c4f4](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/6c4f474ec615e91) David Söderberg)  

### Other changes

**x permissions mvnw**


[8836a](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/8836a200cba790f) Ulf Slunga *2025-11-03 13:25:04*

**removed adding of dummy data on get**


[79ab8](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/79ab80929301ba5) Nils Elveros *2025-11-03 13:05:52*

**Some fixes to align with the RTF rule**


[1ae0f](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/1ae0fd07183ca96) Nils Elveros *2025-11-03 13:02:24*

**Continued work wiht uppgiftslager**


[a81e5](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/a81e5e9995670cb) Nils Elveros *2025-11-03 08:44:50*

**Initial commit**


[857b3](https://github.com/Forsakringskassan/rimfrost-service-oul/commit/857b39596f747b5) davidsoderberg-ductus *2025-10-30 08:49:41*


