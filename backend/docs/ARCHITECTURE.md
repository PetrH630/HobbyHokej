# Architektura aplikace Hobby Hokej

## Úvod

Hobby Hokej je backendová webová aplikace postavená na platformě  
**Java + Spring Boot**, určená pro správu hobby hokejových zápasů, hráčů, sezón a registrací.

Architektura je navržena s důrazem na:

- oddělení odpovědností (Separation of Concerns)
- modulární monolitický návrh
- doménově orientovanou strukturu balíčků
- selektivní použití CQRS
- audit na úrovni databáze
- připravenost pro reálné produkční použití

------------------------------------------------------------------------

## Přehled architektury

Aplikace je navržena jako **modulární monolit** s vícevrstvovou strukturou a REST API rozhraním.

### Základní vrstvy

- Controller vrstva (REST API)
- Service vrstva (business logika)
- Repository vrstva (perzistence)
- Doménový model (Entity)
- DTO & Mapper vrstva (MapStruct)
- Security vrstva
- Session kontext (aktuální hráč / sezóna)
- Notifikační subsystém (In-App, Email, SMS)
- Scheduler komponenty
- Databázová vrstva (Flyway, triggery, history tabulky)

------------------------------------------------------------------------

## Modulární struktura aplikace

Hlavní balíček:

`cz.phsoft.hokej`

Doménové moduly:

- `match`
- `player`
- `registration`
- `season`
- `user`
- `notifications`
- `security`
- `config`
- `demo`
- `shared`
- `system`

Každý doménový modul obsahuje typicky:

- controllers
- services
- repositories
- entities
- dto
- mappers
- enums
- exceptions

Tato struktura podporuje jasné oddělení domén a jejich odpovědností.

------------------------------------------------------------------------

## Controller vrstva

### Odpovědnost

- definice REST endpointů
- validace vstupních dat (`@Valid`)
- autorizace (`@PreAuthorize`)
- delegace logiky do servisní vrstvy
- práce s DTO objekty

### Charakteristika

- neobsahuje business logiku
- nepracuje přímo s databází
- neobsahuje transakční logiku
- využívá centrální zpracování výjimek přes `@ControllerAdvice`

------------------------------------------------------------------------

## Service vrstva

### Odpovědnost

- implementace business logiky
- koordinace práce mezi repozitáři
- řízení transakcí
- aplikace doménových pravidel

### Selektivní CQRS

CQRS je aplikováno pouze v modulech:

- `match`
- `player`
- `registration`

Tyto moduly obsahují:

- CommandService (změny stavu)
- QueryService (čtecí operace)
- Facade Service (např. MatchServiceImpl)

Transakční hranice (`@Transactional`) jsou definovány na úrovni facade vrstvy, která deleguje do command služeb.

Ostatní moduly používají klasický servisní model bez oddělení command/query.

------------------------------------------------------------------------

## Repository vrstva

### Odpovědnost

- přístup k databázi pomocí Spring Data JPA
- definice dotazů nad entitami
- žádná business logika

Repozitáře jsou používány výhradně servisní vrstvou.

------------------------------------------------------------------------

## Doménový model (Entity)

Doménový model reprezentuje klíčové objekty systému.

### Hlavní entity

- **AppUserEntity** – aplikační uživatel, role, autentizace
- **PlayerEntity** – hráč propojený s uživatelem
- **SeasonEntity** – hokejová sezóna
- **MatchEntity** – zápas přiřazený k sezóně
- **MatchRegistrationEntity** – registrace hráče na zápas
- **NotificationEntity** – in-app notifikace

Každá klíčová entita má odpovídající history entitu pro audit.

------------------------------------------------------------------------

## DTO a mapování

API nepracuje přímo s entitami.

Používají se:

- DTO objekty
- MapStruct pro automatické mapování

### Přínosy

- oddělení API modelu od databázového modelu
- čisté API kontrakty
- minimalizace úniků interních struktur
- generované implementace mapperů (target/generated-sources)

------------------------------------------------------------------------

## Security vrstva

Bezpečnost je řešena pomocí **Spring Security**.

### Hlavní komponenty

- `SecurityConfig`
- `CustomJsonLoginFilter`
- `CustomUserDetailsService`
- `CurrentPlayerFilter`
- `PlayerSecurity`
- `SessionKeys`

### Principy

- JSON-based login
- role-based autorizace (enum Role)
- ochrana endpointů pomocí `@PreAuthorize`
- session-based autentizace
- ukládání kontextu aktuálního hráče do session

------------------------------------------------------------------------

## Session kontext (Season & Player Context)

Aplikace používá session-based kontext:

- `CurrentSeasonService`
- `CurrentPlayerService`

Kontext:

- filtruje data podle sezóny
- určuje aktuálního hráče uživatele
- brání míchání dat napříč sezónami
- centralizuje práci se session

Tento vzor zjednodušuje aplikační logiku a odděluje HTTP kontext od doménových služeb.

------------------------------------------------------------------------

## Období neaktivity hráče

Hráči mohou mít definována období neaktivity:

- zranění
- dovolená
- dlouhodobá absence

### Chování

- při registraci je ověřena dostupnost
- překryv období je validován
- registrace v období neaktivity není povolena
- validace probíhá ve service vrstvě

------------------------------------------------------------------------

## Databázové migrace (Flyway)

Databázové schéma je řízeno pomocí **Flyway**.

### Princip

- verzované SQL migrace (V1, V2, ...)
- automatické spuštění při startu aplikace
- databáze je vždy ve známém a konzistentním stavu

Umístění migrací:  
`src/main/resources/db/migration`

------------------------------------------------------------------------

## Audit změn

Audit je řešen pomocí databázových triggerů a history tabulek.

### Strategie

- každá klíčová entita má history tabulku
- AFTER INSERT / UPDATE / DELETE triggery
- audit je prováděn na úrovni databáze

### Přínosy

- audit nelze obejít aplikační vrstvou
- funguje i při přímém SQL zásahu
- zajišťuje plnou historii změn

------------------------------------------------------------------------

## Notifikační subsystém

Systém podporuje více kanálů:

- In-App notifikace
- Email (EmailService / EmailWedosService)
- SMS (SmsService / SmsTextBeeService)

### Architektura

- NotificationService (orchestrátor)
- NotificationQueryService
- NotificationPreferencesService
- SpecialNotificationService
- NotificationCleanupService

### Scheduler komponenty

- MatchReminderScheduler
- NoResponseReminderScheduler
- MatchAutoLineupScheduler

Notifikační systém je navržen jako rozšiřitelný bez zásahu do doménových modulů.

------------------------------------------------------------------------

## Demo režim

Balíček `demo` izoluje logiku DEMO režimu.

Obsahuje:

- DemoModeGuard
- DemoModePolicy
- DemoModeOperationNotAllowedException

Tím je zajištěno, že demo logika nezasahuje do jádra domén.

------------------------------------------------------------------------

## Sdílená vrstva (Shared)

Balíček `shared` obsahuje:

- ApiError
- BusinessException
- SuccessResponseDTO

Slouží pro:

- sjednocení odpovědí API
- centralizaci výjimek
- sdílené struktury napříč doménami

------------------------------------------------------------------------

## Shrnutí architektury

Architektura aplikace Hobby Hokej:

- je modulární monolit
- používá selektivní CQRS
- má jasně oddělené domény
- řeší audit pomocí databázových triggerů
- využívá MapStruct pro mapování
- implementuje session-based kontext
- obsahuje rozšiřitelný notifikační subsystém
- je připravena na další růst bez zásadní refaktorizace

Cílem návrhu je **reálně použitelný backendový systém produkční kvality**, nikoli demonstrační projekt.