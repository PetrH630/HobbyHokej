# Hobby Hokej – Backend

Backendová aplikace v **Java 17 + Spring Boot 3** pro správu hobby hokejových zápasů.

Aplikace poskytuje REST API pro organizaci zápasů mezi dvěma týmy, kdy se hráči samostatně registrují na konkrétní pozice a jsou automaticky rozdělováni do týmů.

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

- hráč může, ale nemusí být vázán na uživatelský účet
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
    - `SUBSTITUTE`
    - `EXCUSED`
    - `NO_RESPONSE`

## Registrace hráčů

- přihlášení na konkrétní pozici
- změna týmu
- omluva
- náhradníci
- kontrola kapacity týmu
- automatické vyhodnocení dostupnosti

## Notifikace

- e-mail (aktivace účtu, reset hesla, změny zápasu)
- SMS (registrace, připomínky, zrušení zápasu)
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

Triggery jsou verzovány pomocí **Flyway** a umístěny ve složce:
