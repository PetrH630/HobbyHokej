## License
This project is licensed under the MIT License.

# Hobby Hokej – backend

Backendová aplikace v Java Spring Boot pro správu amatérského hokejového týmu
(**Hobby Hokej**).

Aplikace poskytuje REST API pro kompletní správu týmového provozu – od uživatelů
a hráčů, přes sezóny a zápasy, až po registrace, dostupnost hráčů
a automatické notifikace.

> Tento README popisuje **backendovou část** projektu (Maven, Spring Boot).  
> Frontend aplikace (React + Vite) je umístěna ve zvláštní složce projektu.

---

## Funkční rozsah

Backend poskytuje REST API pro:

- správu uživatelských účtů a rolí (ADMIN, MANAGER, USER)
- správu hráčů (hráč může, ale nemusí být vázán na uživatelský účet)
- správu sezón (oddělení historických a aktivních dat)
- evidenci zápasů v rámci sezón
- registrace hráčů na zápasy:
    - přihlášení
    - odhlášení
    - omluva
    - náhradníci
- automatické přesuny hráčů při změně kapacity zápasu
- evidenci období neaktivity hráčů (zranění, dovolená, absence)
- kontrolu dostupnosti hráče při registraci na zápas
- audit změn registrací a důležitých operací
- autentizaci a autorizaci uživatelů (Spring Security, JWT)
- automatické notifikace:
    - e-mail (aktivace účtu, reset hesla, změny zápasu)
    - SMS (registrace, připomínky, zrušení zápasu)
- plánované úlohy (scheduler) pro připomínky zápasů

---

## Dokumentace

- JavaDoc: generován ze zdrojového kódu backendu
- Architektura aplikace: [ARCHITECTURE](docs/ARCHITECTURE.md)
- API dokumentace: [API](docs/API.md)


---

## Technologie

Projekt je postaven na:

### Backend

- **Java**: 19
- **Framework**: Spring Boot `3.2.2`
- **Build**: Maven

### Persistence & data

- Spring Data JPA
- Hibernate
- MariaDB
- Flyway (databázové migrace)
- Hibernate Envers (audit změn)

### Bezpečnost

- Spring Security
- JWT (jjwt `0.11.5`)
- Role-based autorizace

### Validace & mapování

- Jakarta Validation (`spring-boot-starter-validation`)
- MapStruct `1.5.5.Final` (DTO ↔ Entity)

### Notifikace

- Spring Mail (e-mail)
- SMS integrace (TextBee)
- Spring Scheduler (plánované úlohy)

### Testování

- `spring-boot-starter-test`
- `spring-security-test`

---

## Struktura projektu (backend)

Zjednodušená struktura:

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/cz/phsoft/hokej/
│   │   │   ├── config
│   │   │   ├── controllers
│   │   │   ├── data
│   │   │   │   ├── entities
│   │   │   │   ├── enums
│   │   │   │   └── repositories
│   │   │   ├── exceptions
│   │   │   ├── models
│   │   │   │   ├── dto
│   │   │   │   │   └── requests
│   │   │   │   ├── mappers
│   │   │   │   └── services
│   │   │   │       ├── email
│   │   │   │       ├── notification
│   │   │   │       └── sms
│   │   │   └── security    
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
├── README.md
├── docs/
│   ├── ARCHITECTURE.md
│   └── players-api.md
└── logs
