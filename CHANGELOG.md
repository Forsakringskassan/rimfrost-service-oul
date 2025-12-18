# rimfrost-service-oul changelog

Changelog of rimfrost-service-oul.

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


