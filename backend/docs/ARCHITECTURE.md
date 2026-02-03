# Architektura aplikace Hobby Hokej

## Úvod

Hobby Hokej je backendová webová aplikace postavená na platformě
**Java + Spring Boot**, určená pro správu amatérského hokejového týmu.

Architektura je navržena s důrazem na:
- oddělení odpovědností (separation of concerns),
- čitelnost a udržitelnost kódu,
- doménově orientovaný návrh,
- připravenost pro reálné produkční použití.

---

## Přehled architektury

Aplikace je navržena jako **vícevrstvá backendová aplikace**
s REST API rozhraním.

Základní vrstvy:

- Controller vrstva (REST API)
- Service vrstva (business logika)
- Repository vrstva (perzistence)
- Doménový model (Entity)
- DTO & Mapper vrstva
- Security vrstva
- Notifikační subsystém
- Databázová vrstva (Flyway, audit)

---

## Controller vrstva

**Odpovědnost:**
- definice REST endpointů,
- validace vstupních dat (`@Valid`),
- převod HTTP požadavků na DTO objekty,
- delegace logiky do servisní vrstvy.

**Charakteristika:**
- neobsahuje business logiku,
- nepracuje přímo s databází,
- řeší autorizaci pomocí anotací (`@PreAuthorize`).

**Balíček:**
cz.phsoft.hokej.controllers


---

## Service vrstva

**Odpovědnost:**
- implementace business logiky aplikace,
- koordinace práce mezi repozitáři,
- řízení transakcí,
- validace doménových pravidel.

Service vrstva řeší mimo jiné:
- registrace hráčů na zápasy,
- kapacity zápasů a náhradníky,
- sezóny a jejich aktivaci,
- dostupnost hráče (období neaktivity),
- auditní logiku a notifikace.

**Charakteristika:**
- transakční zpracování (`@Transactional`),
- žádná závislost na HTTP vrstvě,
- návrh orientovaný na doménu, nikoli na databázi.

**Balíček:**
cz.phsoft.hokej.models.services


---

## Repository vrstva

**Odpovědnost:**
- přístup k databázi pomocí Spring Data JPA,
- definice dotazů nad entitami,
- žádná business logika.

Repozitáře jsou používány výhradně servisní vrstvou.

**Balíček:**
cz.phsoft.hokej.data.repositories


---

## Doménový model (Entity)

Doménový model reprezentuje reálné objekty hokejového týmu
a jejich vztahy.

### Hlavní entity

- **AppUserEntity**
    - aplikační uživatel,
    - autentizace, role, nastavení.

- **PlayerEntity**
    - hráč,
    - může být svázán s uživatelem,
    - jeden uživatel může spravovat více hráčů.

- **SeasonEntity**
    - hokejová sezóna,
    - oddělení historických a aktuálních dat.

- **MatchEntity**
    - hokejový zápas,
    - vždy přiřazen ke konkrétní sezóně,
    - kapacita, cena, stav.

- **MatchRegistrationEntity**
    - registrace hráče na zápas,
    - stav účasti (REGISTERED, SUBSTITUTE, EXCUSED…).

- **PlayerInactivityPeriodEntity**
    - období neaktivity hráče (zranění, dovolená),
    - kontrola dostupnosti hráče.

**Balíček:**
cz.phsoft.hokej.data.entities


---

## DTO a mapování

API nepracuje přímo s entitami.

Pro přenos dat jsou použity:
- DTO objekty,
- MapStruct pro automatické mapování.

**Přínosy:**
- oddělení API modelu od perzistence,
- bezpečnější změny doménového modelu,
- čistější API kontrakty.

**Balíčky:**
cz.phsoft.hokej.models.dto
cz.phsoft.hokej.models.mappers


---

## Security vrstva

Bezpečnost je řešena pomocí **Spring Security**.

### Hlavní principy

- autentizace uživatele (login / logout),
- role-based autorizace,
- ochrana endpointů pomocí anotací,
- práce s kontextem přihlášeného uživatele.

Aplikace využívá:
- session-based přihlášení,
- JWT tokeny pro vybrané scénáře,
- ThreadLocal kontext aktuálního hráče a sezóny.

**Balíček:**
cz.phsoft.hokej.security


---

## Sezóny a kontext uživatele

Aplikace pracuje s konceptem **aktuální sezóny**:

- globálně aktivní sezóna (nastavuje ADMIN),
- uživatelsky zvolená sezóna.

Tento kontext:
- ovlivňuje výpis zápasů,
- určuje dostupná data,
- umožňuje práci s historickými sezónami
  bez míchání dat.

---

## Období neaktivity hráče

Hráči mohou mít definována období neaktivity.

**Použití:**
- zranění,
- dovolená,
- dlouhodobá absence.

**Chování:**
- při registraci na zápas je ověřena dostupnost hráče,
- registrace v období neaktivity není povolena,
- logika je řešena v servisní vrstvě.

---

## Databázové migrace (Flyway)

Databázové schéma je řízeno pomocí **Flyway**.

### Princip

- verzované SQL migrace,
- automatické spuštění při startu aplikace,
- databáze je vždy ve známém stavu.

**Umístění migrací:**
src/main/resources/db/migration


**Přínosy:**
- reprodukovatelnost databáze,
- bezpečný vývoj schématu,
- žádné ruční změny v DB.

---

## Audit změn

Audit je v aplikaci řešen **dvěma úrovněmi**.

### Hibernate Envers

- audit vybraných entit,
- automatické `_AUD` tabulky,
- technický audit změn přes ORM.

Používá se pro:
- uživatele,
- hráče,
- zápasy,
- sezóny.

---

### Databázový audit registrací (triggery)

Registrace na zápasy patří mezi nejkritičtější doménová data.

Proto je použit **databázový audit pomocí triggerů**:

- vlastní tabulka `match_registration_history`,
- AFTER INSERT / UPDATE / DELETE triggery,
- audit je nezávislý na ORM vrstvě.
- audit je vytvářen **na úrovni databáze**, nikoliv aplikační logiky

**Přínosy:**
- audit funguje i při změnách mimo JPA (SQL, batch, migrace),
- nelze jej obejít chybou v aplikaci,
- kompletní historie změn včetně typu operace a času,
- vysoká důvěryhodnost dat.

Hibernate Envers je v aplikaci nadále používán pro audit ostatních doménových entit,
zatímco registrace na zápasy mají specializovaný business audit.

---

## Notifikační subsystém

Aplikace podporuje notifikace:

- e-mail (Spring Mail),
- SMS (externí SMS brána).

Notifikace jsou odesílány:
- při změnách registrací,
- při důležitých událostech,
- pomocí plánovaných úloh (scheduler).

Logika generování zpráv je oddělena
od jejich odesílání.

---

## Shrnutí architektury

Architektura aplikace Hobby Hokej:

- odpovídá běžným produkčním Spring Boot aplikacím,
- pracuje s doménovými pravidly,
- řeší audit, historii a časový kontext,
- je připravena na další rozšiřování.

Cílem návrhu nebylo vytvořit demonstrační projekt,
ale **reálně použitelný backendový systém**.


