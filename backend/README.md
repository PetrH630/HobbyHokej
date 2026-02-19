# Hobby Hokej -- Backend

Backendová aplikace v **Java Spring Boot** pro správu amatérského
hokejového týmu (**Hobby Hokej**).

Aplikace poskytuje REST API pro kompletní správu týmového provozu -- od
uživatelů a hráčů, přes sezóny a zápasy, až po registrace, dostupnost
hráčů, statistiky a automatické notifikace.

Tento README popisuje **backendovou část** projektu (Maven, Spring
Boot). Frontend aplikace (React + Vite) je umístěna ve zvláštní složce
projektu.

------------------------------------------------------------------------

## Funkční rozsah

Backend poskytuje REST API pro:

-   správu uživatelských účtů a rolí (ADMIN, MANAGER, USER)
-   autentizaci a autorizaci uživatelů (Spring Security + JWT)
-   správu hráčů (hráč může, ale nemusí být vázán na uživatelský účet)
-   správu sezón (oddělení historických a aktivních dat)
-   evidenci zápasů v rámci sezón
-   registrace hráčů na zápasy:
    -   přihlášení
    -   odhlášení
    -   omluva
    -   náhradníci
    -   rezervace
-   automatické přesuny hráčů při změně kapacity zápasu
-   evidenci období neaktivity hráčů (zranění, dovolená, absence)
-   kontrolu dostupnosti hráče při registraci na zápas
-   statistiky hráčů za sezónu
-   audit změn důležitých entit pomocí databázových triggerů
-   automatické notifikace:
    -   e-mail (aktivace účtu, reset hesla, změny zápasu)
    -   SMS (registrace, připomínky, zrušení zápasu)
-   plánované úlohy (scheduler) pro připomínky zápasů

------------------------------------------------------------------------

## Audit změn (Database Triggers)

Audit změn je implementován na úrovni databáze pomocí **MariaDB
triggerů**.

Auditované entity:

-   AppUser
-   Player
-   Match
-   Season
-   MatchRegistration

Pro každou z těchto entit jsou vytvořeny:

-   audit tabulky
-   triggery pro operace INSERT
-   triggery pro operace UPDATE
-   triggery pro operace DELETE

Triggery jsou verzovány pomocí **Flyway** a umístěny ve složce:

    src/main/resources/db/migration

Tím je zajištěno:

-   konzistentní auditování i mimo aplikaci
-   plná dohledatelnost změn
-   kompatibilita s produkčním prostředím
-   nezávislost auditu na ORM vrstvě

Aplikace nepoužívá Hibernate Envers. Audit je plně řízen databází.

------------------------------------------------------------------------

## Architektura

Aplikace je navržena jako vícevrstvá backendová aplikace s REST API
rozhraním.

Základní vrstvy:

-   Controller vrstva -- REST API endpointy
-   Service vrstva -- business logika
-   Repository vrstva -- přístup k databázi
-   DTO vrstva -- přenos dat mezi backendem a frontendem
-   Security vrstva -- autentizace a autorizace

Podrobnosti viz dokumentace:

-   docs/ARCHITECTURE.md
-   docs/API.md

------------------------------------------------------------------------

## Technologie

### Backend

-   Java 17
-   Spring Boot 3.2.2
-   Maven

### Persistence & data

-   Spring Data JPA
-   Hibernate
-   MariaDB 10.6
-   Flyway (databázové migrace)
-   Databázové triggery pro audit změn

### Bezpečnost

-   Spring Security
-   JWT (jjwt 0.11.5)
-   Role-based autorizace

### Validace & mapování

-   Jakarta Validation
-   MapStruct 1.5.5.Final (DTO ↔ Entity)

### Notifikace

-   Spring Mail (e-mail)
-   SMS integrace (TextBee)
-   Spring Scheduler (plánované úlohy)

------------------------------------------------------------------------

## Konfigurace

Konfigurace probíhá pomocí `application.properties` a environment
proměnných.

Používány jsou profily:

-   dev -- vývojové prostředí
-   prod -- produkční prostředí
-   demo -- produkční prostředí s omezenými přístupy

Databázové migrace jsou spravovány pomocí Flyway a spouští se
automaticky při startu aplikace.

------------------------------------------------------------------------

## Struktura projektu (backend)

Zjednodušená struktura:

backend/ 
├── src/
│   ├── main/ 
│   │ ├── java/cz/phsoft/hokej/ 
│   │ │ ├──config 
│   │ │ ├── controllers 
│   │ │ ├── data/ 
│   │ │ │ ├── entities 
│   │ │ │ ├── enums 
│   │ │ │ └── repositories 
│   │ │ ├── exceptions 
│   │ │ ├── models/ 
│   │ │ │ ├── dto/ 
│   │ │ │ ├── mappers 
│   │ │ │ └── services/ 
│   │ │ │   ├── email 
│   │ │ │   ├── notification 
│   │ │ │   └── sms 
│   │ │ └── security
│   │ └── resources/ 
│   │       ├── application.properties 
│   │       └── db/migration 
│   └── test/ 
├── pom.xml 
├── README.md 
├── docs/ 
│     ├── ARCHITECTURE.md 
│     └── API.md 
└── logs/

------------------------------------------------------------------------

## Build a spuštění

### Build projektu

    mvn clean install

### Spuštění aplikace

    mvn spring-boot:run

nebo

    java -jar target/HobbyHokej-1.0-SNAPSHOT.jar

------------------------------------------------------------------------

## Licence

Tento projekt je licencován pod licencí MIT.
