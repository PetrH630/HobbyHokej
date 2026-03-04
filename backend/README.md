# Hobby Hokej – Backend

Backendová aplikace v **Java 17 + Spring Boot 3** pro správu hobby hokejových zápasů.

Aplikace poskytuje REST API pro organizaci zápasů mezi dvěma týmy, kdy se hráči samostatně 
registrují na konkrétní termín a vybírají si dostupnou herní pozici v jednom ze dvou týmů.

Aplikace zajišťuje:
- řízení kapacity a obsazenosti pozic,
- evidenci registrací (hráč, tým, pozice),
- přehled náhradníků a omluvených,
- vyhodnocení výsledků zápasů,
- výpočet hráčských statistik v rámci sezóny,
- auditní historii změn a notifikační mechanismus.

Aplikace je navržena jako modulární webový systém s oddělením doménové logiky, servisní vrstvy a REST API.

Tento README popisuje **backendovou část projektu** (Spring Boot + Maven).  
Frontend aplikace (React + Vite) je umístěn v samostatné složce projektu.

---

# Přehled projektu

Hobby Hokej je doménově orientovaná backendová aplikace, která řeší:

- organizaci amatérských hokejových zápasů
- registraci hráčů na konkrétní pozice
- automatické řízení kapacity týmů
- správu sezón
- evidenci statistik hráčů
- audit změn na úrovni databáze
- automatické notifikace (e-mail + SMS)

Architektura je navržena s důrazem na:

- oddělení odpovědností (Separation of Concerns)
- doménově orientované členění balíčků
- čisté REST API
- produkční připravenost
- auditovatelnost změn

---

# Funkční rozsah

Backend poskytuje REST API pro:

## Uživatelský a bezpečnostní model

- správu uživatelských účtů
- role: `ADMIN`, `MANAGER`, `USER`
- autentizaci pomocí JWT
- autorizaci pomocí Spring Security
- správu uživatelských nastavení

## Hráči

- hráč musí být vázán na uživatelský účet
- správa hráčských údajů
- evidence období neaktivity (zranění, dovolená, absence)
- kontrola dostupnosti hráče při registraci
- výpočet statistik hráče v rámci sezóny

## Sezóny

- oddělení historických a aktivních dat
- výběr aktuální sezóny (kontext aplikace)

## Zápasy

- evidence zápasů v rámci sezón
- rozdělení hráčů do týmů (DARK / LIGHT)
- kapacitní omezení pozic
- automatické přesuny mezi stavem:
    - `REGISTERED`
    - `RESERVED`
    
## Registrace hráčů

- přihlášení na konkrétní pozici
- změna týmu
- omluva
- náhradníci
- kontrola kapacity týmu
- automatické vyhodnocení dostupnosti

## Notifikace

- e-mail (aktivace účtu, reset hesla, změny zápasu, registrace)
- SMS (registrace, připomínky, změny zápasu)
- interní aplikační notifikace (badge)
- globální i individuální úroveň notifikací

## Scheduler

- plánované úlohy pro:
    - připomínky zápasů
    - kontrolní procesy

---

# Audit změn (Database Triggers)

Audit je implementován na úrovni databáze pomocí **MariaDB triggerů**.

Auditované entity:

- `AppUser`
- `Player`
- `Match`
- `Season`
- `MatchRegistration`

Pro každou entitu jsou vytvořeny:

- audit tabulky
- triggery pro `INSERT`
- triggery pro `UPDATE`
- triggery pro `DELETE`

Triggery jsou verzovány pomocí **Flyway** a umístěny ve složce: /db/migration

Výhody řešení:
- audit funguje i mimo aplikaci
- plná dohledatelnost změn
- nezávislost na ORM vrstvě
- produkční spolehlivost

Aplikace nepoužívá Hibernate Envers. Audit je plně řízen databází.

---

# Architektura

Aplikace je navržena jako vícevrstvá backendová aplikace s REST API.

Vrstvy
- Controller – REST endpointy
- Service – business logika
- Repository ## Vrstvy

- **Controller** – REST endpointy
- **Service** – business logika
- **Repository** – přístup k databázi
- **DTO** – přenos dat mezi backendem a frontendem
- **Security** – autentizace a autorizace
- **Mapper** – převod DTO ↔ Entity

Projekt je členěn **doménově**, nikoliv podle typu vrstvy.

## Hlavní domény

- `match`
- `player`
- `registration`
- `season`
- `notifications`
- `user`
- `security`
- `config`

Podrobnosti viz:

- `docs/ARCHITECTURE.md`
- `docs/API.md`
- `docs/Javadoc/index.html`

---

# Technologie

## Backend

- Java 17
- Spring Boot 3.x
- Maven

## Persistence

- Spring Data JPA
- Hibernate
- MariaDB 10.6+
- Flyway

## Bezpečnost

- Spring Security
- JWT (jjwt)
- Role-based autorizace

## Mapování a validace

- Jakarta Validation
- MapStruct

## Notifikace

- Spring Mail
- SMS integrace (TextBee)
- Spring Scheduler

---

# Konfigurace

Konfigurace probíhá pomocí:

- `application.properties`
- environment proměnných

Používány jsou profily:

- `dev`
- `prod`
- `demo`

Databázové migrace jsou spouštěny automaticky při startu aplikace.

---

# Struktura projektu (backend)

backend/
├── src/
│   ├── main/
│   │   ├── java/cz/phsoft/hokej/
│   │   │   ├── config
│   │   │   ├── demo
│   │   │   ├── match
│   │   │   ├── notifications
│   │   │   ├── player
│   │   │   ├── registration
│   │   │   ├── season
│   │   │   ├── security
│   │   │   ├── shared
│   │   │   ├── system
│   │   │   └── user
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration
│   └── test/
├── docs/
│   ├── ARCHITECTURE.md
│   ├── API.md
│   └── Javadoc/
├── pom.xml
├── README.md
└── logs/

---

# Dokumentace

Projekt obsahuje generovanou dokumentaci:

- Architektura: `docs/ARCHITECTURE.md`
- API: `docs/API.md`
- Javadoc: `docs/Javadoc/index.html`

---

# Licence

Tento projekt je licencován pod licencí MIT.