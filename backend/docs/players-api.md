📘 Player API – technická dokumentace
Přehled

Tento dokument popisuje REST API pro správu hráčů přihlášeného uživatele v aplikaci.
API je navrženo tak, aby:

neumožňovalo manipulaci s cizími hráči,

oddělovalo odpovědnosti (Controller / Service / Repository),

používalo jednotný error-handling (ApiError),

bylo bezpečné vůči podvržení ID z klienta.

Architektura a závislosti
Frontend (React)
↓
PlayerController
↓
PlayerService (PlayerServiceImpl)
↓
PlayerRepository / AppUserRepository
↓
Database

Doplňkové služby

CurrentPlayerService – správa aktuálně vybraného hráče

PlayerMapper – mapování Entity ↔ DTO

GlobalExceptionHandler – centrální zpracování chyb

Spring Security – autentizace + autorizace

Autentizace a autorizace

Všechny endpointy vyžadují přihlášeného uživatele

Autentizace probíhá přes Spring Security (Authentication)

Identita uživatele je reprezentována e-mailem

Autorizace:

nepoužívá se playerId z FE u citlivých operací

vlastnictví hráče je kontrolováno v servisní vrstvě

1️⃣ POST /api/players/me
Vytvoření nového hráče pro přihlášeného uživatele
Účel

Vytvoří nového hráče a automaticky ho naváže na přihlášeného uživatele.

HTTP Request

URL

POST /api/players/me


Headers

Content-Type: application/json
Cookie: JSESSIONID=...


Body

{
"name": "Petr",
"surname": "Novák",
"type": "PLAYER",
"status": "ACTIVE"
}

Sekvence volání (detailně)
1️⃣ Frontend

Uživatel vyplní formulář „Nový hráč“

Klikne na „Uložit“

FE odešle POST /api/players/me

2️⃣ PlayerController
createMyPlayer(PlayerDTO dto, Authentication auth)


Spring:

namapuje JSON → PlayerDTO

vloží Authentication

Controller:

získá email = auth.getName()

deleguje logiku do service

➡️ Neprovádí žádnou validaci ani DB operace

3️⃣ PlayerServiceImpl
createPlayerForUser(PlayerDTO dto, String email)


Postup:

AppUserRepository.findByEmail(email)

❌ UserNotFoundException → 404

Kontrola duplicity:

existsByUserAndNameAndSurname(...)


❌ DuplicateNameSurnameException → 409

Validace statusu:

❌ InvalidPlayerStatusException → 400 / 422

PlayerMapper.toEntity(dto)

Navázání hráče na uživatele

PlayerRepository.save(entity)

PlayerMapper.toDTO(entity)

HTTP Response – úspěch
{
"id": 7,
"name": "Petr",
"surname": "Novák",
"fullName": "Petr Novák",
"type": "PLAYER",
"status": "ACTIVE"
}

Možné chyby
HTTP	Výjimka	Význam
401 / 403	AccessDeniedException	Uživatel není přihlášen
404	UserNotFoundException	Uživatel neexistuje
409	DuplicateNameSurnameException	Duplicitní hráč
400	InvalidPlayerStatusException	Neplatný status
Dopad na systém

Hráč:

se objeví v /api/players/me

může být vybrán jako current player

může být použit v registracích, statistikách, reportech

2️⃣ GET /api/players/me
Seznam hráčů přihlášeného uživatele
Účel

Vrátí všechny hráče patřící přihlášenému uživateli.

HTTP Request
GET /api/players/me

Sekvence volání
PlayerController
getMyPlayers(Authentication auth)


získá email

zavolá service

PlayerServiceImpl
getPlayersByUser(String email)


AppUserRepository.findByEmail(email)

PlayerRepository.findAllByUser(user)

Mapování Entity → DTO

HTTP Response
[
{
"id": 7,
"fullName": "Petr Novák",
"status": "ACTIVE"
},
{
"id": 8,
"fullName": "Adam Novák",
"status": "INJURED"
}
]

Možné chyby
HTTP	Výjimka	Význam
401 / 403	AccessDeniedException	Nepřihlášen
404	UserNotFoundException	Nekonzistence dat
Dopad na systém

Používá se:

po loginu

při výběru aktuálního hráče

při správě hráčů

Prázdný seznam ≠ chyba

3️⃣ PUT /api/players/me
Aktualizace aktuálního hráče
Účel

Upraví aktuálně vybraného hráče bez posílání playerId z frontendu.

Klíčová závislost

CurrentPlayerService je jediný zdroj pravdy o tom, který hráč je upravován

HTTP Request
{
"name": "Petr",
"surname": "Novák",
"status": "INJURED"
}

Sekvence volání
PlayerController
updatePlayer(PlayerDTO dto)


currentPlayerService.requireCurrentPlayer()

❌ NoCurrentPlayerSelectedException → 409

currentPlayerService.getCurrentPlayerId()

playerService.updatePlayer(playerId, dto)

PlayerServiceImpl
updatePlayer(Long playerId, PlayerDTO dto)


PlayerRepository.findById(playerId)

❌ PlayerNotFoundException → 404

Získání přihlášeného uživatele

Kontrola vlastnictví

❌ AccessDeniedException → 403

Kontrola duplicity jména

Validace statusu

Aktualizace entity

save(...)

toDTO(...)

HTTP Response
{
"id": 7,
"fullName": "Petr Novák",
"status": "INJURED"
}

Možné chyby
HTTP	Výjimka	Význam
409	NoCurrentPlayerSelectedException	Nevybrán hráč
404	PlayerNotFoundException	Hráč neexistuje
403	AccessDeniedException	Cizí hráč
409	DuplicateNameSurnameException	Duplicitní jméno
400	InvalidPlayerStatusException	Neplatný status
Dopad na systém

Ovlivňuje:

registrace na zápasy

zobrazování aktuálního hráče

statistiky a reporty

Nemění vazby – ID hráče zůstává stejné

Error handling – společný pro celé API

Všechny business chyby jsou zpracovány přes:

@ControllerAdvice
GlobalExceptionHandler

Struktura chyby (ApiError)
{
"status": 409,
"error": "Conflict",
"message": "Hráč se stejným jménem již existuje",
"path": "/api/players/me",
"ip": "127.0.0.1"
}