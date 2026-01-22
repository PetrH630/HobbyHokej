# Stará Garda – backend

Spring Boot backend aplikace pro správu amatérského hokejového týmu („Stará Garda“).  
Backend poskytuje REST API pro:

- správu uživatelů a jejich účtů
- správu hráčů (vázaných na uživatele)
- evidenci zápasů a registrací na zápasy
- autentizaci/autorizaci (Spring Security)

> Tento README popisuje **backend část** projektu (Maven, Spring Boot).  
> Frontend (React/Vite) je ve zvláštní složce.

---

## Dokumentace

- [Player API](docs/players-api.md)

## Technologie

Projekt je postaven na:

- **Java**: 19
- **Build**: Maven
- **Framework**: Spring Boot `3.2.2`
- **Persistence**:
    - Spring Data JPA
    - Hibernate
    - MariaDB
    - Flyway (DB migrace)
    - Hibernate Envers (audit změn)
- **Bezpečnost**:
    - Spring Security
    - JWT (jjwt 0.11.5)
- **Validace**:
    - `spring-boot-starter-validation` (Jakarta Validation)
- **Mapování DTO ↔ Entity**:
    - MapStruct `1.5.5.Final`
- **E-mail**:
    - `spring-boot-starter-mail`
- **Testy**:
    - `spring-boot-starter-test`
    - `spring-security-test`

---

## Struktura projektu (backend)

Zjednodušeně:

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/cz/phsoft/hokej/...
│   │   └── resources/
│   │       └── application.properties (nebo application.yml)
│   └── test/
├── pom.xml
├── README.md
└── docs/
    └── players-api.md   (API dokumentace – hráči, volitelně)



