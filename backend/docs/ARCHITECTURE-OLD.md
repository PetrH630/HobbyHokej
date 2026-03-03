# Architektura aplikace Hobby Hokej

## Úvod

Hobby Hokej je backendová webová aplikace postavená na platformě\
**Java + Spring Boot**, určená pro správu amatérského hokejového týmu.

Architektura je navržena s důrazem na:

-   oddělení odpovědností (separation of concerns)
-   čitelnost a udržitelnost kódu
-   doménově orientovaný návrh
-   připravenost pro reálné produkční použití

------------------------------------------------------------------------

## Přehled architektury

Aplikace je navržena jako **vícevrstvá backendová aplikace** s REST API
rozhraním.

### Základní vrstvy

-   Controller vrstva (REST API)
-   Service vrstva (business logika)
-   Repository vrstva (perzistence)
-   Doménový model (Entity)
-   DTO & Mapper vrstva
-   Security vrstva
-   Notifikační subsystém
-   Databázová vrstva (Flyway, audit)

------------------------------------------------------------------------

## Controller vrstva

### Odpovědnost

-   definice REST endpointů
-   validace vstupních dat (`@Valid`)
-   převod HTTP požadavků na DTO objekty
-   delegace logiky do servisní vrstvy

### Charakteristika

-   neobsahuje business logiku
-   nepracuje přímo s databází
-   řeší autorizaci pomocí anotací (`@PreAuthorize`)

**Balíček:**\
`cz.phsoft.hokej.controllers`

------------------------------------------------------------------------

## Service vrstva

### Odpovědnost

-   implementace business logiky aplikace
-   koordinace práce mezi repozitáři
-   řízení transakcí
-   validace doménových pravidel

### Řešené oblasti

-   registrace hráčů na zápasy
-   kapacity zápasů a náhradníci
-   sezóny a jejich aktivace
-   dostupnost hráče (období neaktivity)
-   auditní logika
-   notifikace

### Charakteristika

-   transakční zpracování (`@Transactional`)
-   žádná závislost na HTTP vrstvě
-   návrh orientovaný na doménu

**Balíček:**\
`cz.phsoft.hokej.models.services`

------------------------------------------------------------------------

## Repository vrstva

### Odpovědnost

-   přístup k databázi pomocí Spring Data JPA
-   definice dotazů nad entitami
-   žádná business logika

Repozitáře jsou používány výhradně servisní vrstvou.

**Balíček:**\
`cz.phsoft.hokej.data.repositories`

------------------------------------------------------------------------

## Doménový model (Entity)

Doménový model reprezentuje reálné objekty hokejového týmu a jejich
vztahy.

### Hlavní entity

-   **AppUserEntity** -- aplikační uživatel, autentizace, role,
    nastavení
-   **PlayerEntity** -- hráč, svázán s uživatelem
-   **SeasonEntity** -- hokejová sezóna
-   **MatchEntity** -- zápas přiřazený k sezóně
-   **MatchRegistrationEntity** -- registrace hráče na zápas
-   **PlayerInactivityPeriodEntity** -- období neaktivity hráče

**Balíček:**\
`cz.phsoft.hokej.data.entities`

------------------------------------------------------------------------

## DTO a mapování

API nepracuje přímo s entitami.

Používají se:

-   DTO objekty
-   MapStruct pro automatické mapování

### Přínosy

-   oddělení API modelu od perzistence
-   bezpečnější změny doménového modelu
-   čisté API kontrakty

**Balíčky:**

-   `cz.phsoft.hokej.models.dto`
-   `cz.phsoft.hokej.models.mappers`

------------------------------------------------------------------------

## Security vrstva

Bezpečnost je řešena pomocí **Spring Security**.

### Principy

-   autentizace (login / logout)
-   role-based autorizace
-   ochrana endpointů (`@PreAuthorize`)
-   BCrypt hashování hesel
-   session-based přihlášení
-   ThreadLocal kontext aktuálního hráče

**Balíček:**\
`cz.phsoft.hokej.security`

------------------------------------------------------------------------

## Sezóny a kontext uživatele

Aplikace pracuje s konceptem **aktuální sezóny**:

-   globálně aktivní sezóna (nastavuje ADMIN)
-   uživatelsky zvolená sezóna

Kontext:

-   filtruje zápasy
-   odděluje historická data
-   brání míchání sezón

------------------------------------------------------------------------

## Období neaktivity hráče

Hráči mohou mít definována období neaktivity:

-   zranění
-   dovolená
-   dlouhodobá absence

### Chování

-   při registraci je ověřena dostupnost
-   registrace v období neaktivity není povolena
-   logika je řešena v service vrstvě

------------------------------------------------------------------------

## Databázové migrace (Flyway)

Databázové schéma je řízeno pomocí **Flyway**.

### Princip

-   verzované SQL migrace
-   automatické spuštění při startu aplikace
-   databáze je vždy ve známém stavu

**Umístění migrací:**\
`src/main/resources/db/migration`

------------------------------------------------------------------------

## Audit změn

Audit je řešen ve dvou vrstvách.

### Hibernate Envers

-   audit entit
-   automatické `_AUD` tabulky

Používá se pro:

-   uživatele
-   hráče
-   zápasy
-   sezóny

------------------------------------------------------------------------

### Databázový audit registrací

Registrace mají specializovaný audit:

-   tabulka `match_registration_history`
-   AFTER INSERT / UPDATE / DELETE triggery
-   audit na úrovni databáze

### Přínosy

-   nelze obejít aplikací
-   funguje i při přímém SQL zásahu
-   plná historie změn

------------------------------------------------------------------------

## Notifikační subsystém

Aplikace podporuje:

-   e-mail (Spring Mail)
-   SMS (externí brána)

### Vlastnosti

-   oddělení generování zpráv od odesílání
-   rozhodování podle uživatelských preferencí
-   podpora DEMO režimu (notifikace se ukládají do paměti)

------------------------------------------------------------------------

## Shrnutí architektury

Architektura aplikace Hobby Hokej:

-   odpovídá produkčním Spring Boot aplikacím
-   pracuje s doménovými pravidly
-   řeší audit a historii
-   podporuje sezónní kontext
-   je připravena na další rozšiřování

Cílem návrhu je **reálně použitelný backendový systém**, nikoli
demonstrační projekt.
