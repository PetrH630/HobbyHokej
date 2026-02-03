# Hobby Hokej – REST API

- REST API pro správu amatérského hokejového týmu Hobby Hokej
- Čisté JSON API
- Používáno frontendem (React / Vite SPA)

---

## 1. Základní informace

### 1.1 Base URL

- Lokální vývoj:
    - Backend: http://localhost:8080
    - Frontend: http://localhost:5173
- API root:
    - všechny endpointy mají prefix `/api/...`

---

### 1.2 Autentizace a session

- Autentizace:
    - Spring Security (session-based)
- Přihlášení:
    - Endpoint: `POST /api/auth/login`
    - Vstup:
        - JSON s přihlašovacími údaji (email, password)
    - Chování:
        - ověření přihlašovacích údajů
        - při úspěchu vytvoření HTTP session (cookie `JSESSIONID`)
- Odhlášení:
    - Endpoint: `POST /api/auth/logout`
    - Chování:
        - zneplatnění aktuální session
- Autorizace:
    - Role:
        - `ADMIN`
        - `MANAGER`
        - běžný přihlášený uživatel (bez speciální role)
    - Kontrola:
        - anotace `@PreAuthorize` v controllerech
        - pro běžného přihlášeného uživatele se používá `isAuthenticated()`
- Veřejné endpointy (nevyžadují přihlášení):
    - registrace uživatele
    - aktivace účtu přes e-mailový odkaz
    - proces zapomenutého hesla
    - redirect na frontend pro reset hesla

Typické použití:
- Frontend po přihlášení pracuje se session cookie automaticky, není potřeba posílat tokeny v hlavičkách.

---

### 1.3 Formát dat

- MIME typ:
    - request: `application/json`
    - response: `application/json`
- Datum a čas:
    - formát: ISO-8601 (`LocalDateTime`)
    - příklad: `2025-09-01T18:30:00`
- Validace:
    - anotace `@Valid`
    - Bean Validation (Jakarta)
    - při chybě validace:
        - HTTP 400 (Bad Request)
        - tělo odpovědi ve formátu `ApiError`

---

## 2. Chybové odpovědi

### 2.1 ApiError – jednotný formát chyb

Všechny výjimky jsou mapovány na jednotný JSON:

- typ: `ApiError`
- zpracování: `GlobalExceptionHandler`
- typické zdroje chyb:
    - validační chyby vstupu
    - doménové výjimky (např. entita nenalezena)
    - bezpečnostní chyby (nedostatečná oprávnění)

Příklad struktury `ApiError`:

```json
{
  "timestamp": "2025-02-02 10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "BE - Neplatná vstupní data.",
  "path": "/api/matches/1",
  "clientIp": "127.0.0.1",
  "details": {
    "name": "Křestní jméno je povinné.",
    "email": "Email nemá platný formát."
  }
}
```

- `details` obsahuje mapu polí → validační chybové zprávy.
- `message` je stručný popis chyby pro uživatele / frontend.

---

# 3. Autentizace a správa účtů (AuthController)

Tato kapitola popisuje endpointy pro registraci, aktivaci účtu, přihlášení,
logout a proces zapomenutého hesla. Používá je hlavně login/registrace
část frontendu.

Každý endpoint je navržen tak, aby:
- měl jasně oddělenou odpovědnost,
- vracel konzistentní HTTP status kódy,
- používal stejné DTO objekty napříč aplikací.

---

## 3.1 Registrace uživatele

Endpoint:  
`POST /api/auth/register`

Role:  
Veřejné (bez přihlášení)

Vstup (`RegisterUserDTO`):
- `name` – křestní jméno
- `surname` – příjmení
- `email` – e-mail uživatele (login)
- `password` – heslo
- `passwordConfirm` – potvrzení hesla

Chování:
- kontrola shody hesel
- kontrola jedinečnosti e-mailu
- vytvoření **neaktivního** uživatelského účtu
- vygenerování aktivačního tokenu
- uložení tokenu
- odeslání aktivačního e-mailu s odkazem

Typické použití:
- volá frontend registrační stránky po vyplnění formuláře.
- uživatel se nemůže přihlásit, dokud neprojde aktivací e-mailem.

Odpověď:

Při úspěchu:
- HTTP 200
- JSON např.:
  ```json
  {
    "status": "ok",
    "message": "Registrace úspěšná. Zkontrolujte email pro aktivaci účtu."
  }
  ```

Při chybě:
- HTTP 400 / 409 (duplicitní email)
- tělo odpovědi: `ApiError`

---

## 3.2 Aktivace účtu z e-mailu

Endpoint:  
`GET /api/auth/verify?token=...`

Role:  
Veřejné (bez přihlášení)

Vstup:
- query parametr `token` – aktivační token

Chování:
- ověření existence a platnosti tokenu
- nalezení uživatele navázaného na token
- aktivace uživatelského účtu
- označení tokenu jako použitý / neplatný

Typické použití:
- uživatel klikne na odkaz v e-mailu,
- backend provede aktivaci a může vrátit jednoduchý text nebo redirect na frontend.

Odpověď:

Při úspěchu:
- HTTP 200
- jednoduchý text (např. `"Účet byl úspěšně aktivován."`)

Při chybě:
- HTTP 400 / 404 / 410 (expirace tokenu)
- `ApiError` nebo jednoduchá chybová hláška

---

## 3.3 Přihlášení

Endpoint:  
`POST /api/auth/login`

Role:  
Veřejné (bez přihlášení)

Vstup:
- JSON (např. `LoginRequestDTO`):
  - `email` – přihlašovací e-mail
  - `password` – heslo

Chování:
- ověření, že uživatel existuje a je aktivní
- ověření hesla
- vytvoření HTTP session
- nastavení cookie `JSESSIONID`
- vrácení informací o přihlášeném uživateli (id, role, jméno, email…)

Typické použití:
- login stránka; po úspěchu frontend uloží info o uživateli (např. do contextu)
  a používá cookie k volání dalších endpointů.

Odpověď:

Při úspěchu:
- HTTP 200
- JSON (např. `AppUserDTO`)

Při chybě:
- HTTP 401 / 403
- tělo odpovědi: `ApiError`

---

## 3.4 Odhlášení

Endpoint:  
`POST /api/auth/logout`

Role:  
Přihlášený uživatel

Vstup:
- bez těla requestu

Chování:
- zneplatnění aktuální HTTP session
- odhlášení uživatele

Typické použití:
- tlačítko „Odhlásit se“ ve frontendu.

Odpověď:

Při úspěchu:
- HTTP 200 (bez těla nebo jednoduchý text)

---

## 3.5 Zapomenuté heslo – vytvoření požadavku

Endpoint:  
`POST /api/auth/forgotten-password`

Role:  
Veřejné

Vstup (`EmailDTO`):
- `email` – e-mail uživatele

Chování:
- pokud uživatel existuje:
  - vygeneruje se reset token
  - token se uloží do DB
  - odešle se e-mail s odkazem na reset hesla
- pokud uživatel neexistuje:
  - z bezpečnostních důvodů odpověď stejná (nezrazuje existenci účtu)

Typické použití:
- formulář „Zapomenuté heslo“ – zadání e-mailu.

Odpověď:

Vždy:
- HTTP 200  
- text / JSON potvrzující, že byl odeslán e-mail (nebo že požadavek byl přijat)

Technická chyba:
- `ApiError` (např. problém s e-mail serverem)

---

## 3.6 Zapomenuté heslo – info o e-mailu k tokenu

Endpoint:  
`GET /api/auth/forgotten-password/info?token=...`

Role:  
Veřejné

Vstup:
- `token` – reset token

Chování:
- ověření platnosti tokenu
- získání e-mailu navázaného k tokenu
- používá se pro zobrazení „Reset hesla pro: user@example.com“

Odpověď:

Při úspěchu:
- HTTP 200
- JSON:
  ```json
  { "email": "user@example.com" }
  ```

Při chybě:
- HTTP 400 / 404 / 410
- `ApiError`

---

## 3.7 Zapomenuté heslo – nastavení nového hesla

Endpoint:  
`POST /api/auth/forgotten-password/reset`

Role:  
Veřejné

Vstup (`ForgottenPasswordResetDTO`):
- `token` – reset token
- `newPassword` – nové heslo
- `newPasswordConfirm` – potvrzení nového hesla

Chování:
- ověření existence a platnosti tokenu
- ověření shody hesel
- nastavení nového hesla uživateli
- invalidace tokenu

Typické použití:
- resetovací formulář na frontendu (otevřený z e-mailu).

Odpověď:

Při úspěchu:
- HTTP 200 (bez těla nebo jednoduchý text)

Při chybě:
- HTTP 400 / 404 / 410
- `ApiError`

---

## 3.8 Redirect na frontend pro reset hesla

Endpoint:  
`GET /api/auth/reset-password?token=...`

Role:  
Veřejné

Vstup:
- query `token`

Chování:
- backend provede redirect (302) na URL frontendu, např.:
  - `${frontendBaseUrl}/reset-password?token=...`
- používá se, pokud odkaz v mailu vede na backendovou URL a backend přesměruje na SPA.

Odpověď:

Při úspěchu:
- HTTP 302
- hlavička `Location` s front-endovou URL

---

## 3.9 Zjištění aktuálně přihlášeného uživatele

Endpoint:  
`GET /api/auth/me`

Role:  
Přihlášený uživatel

Chování:
- načte uživatele z `SecurityContext`
- vrací základní informace o aktuálním uživateli (pro inicializaci UI po reloadu)

Odpověď:

Při úspěchu:
- HTTP 200
- `AppUserDTO`

Při chybě:
- HTTP 401 – nepřihlášený
- `ApiError`

---

# 4. Uživatelské účty (AppUserController)

Tato kapitola řeší **správu uživatelských účtů** – jednak vlastní profil,
jednak administraci uživatelů (pro ADMIN).

Základní prefix:  
`/api/users`

---

## 4.1 Detail přihlášeného uživatele

Endpoint:  
`GET /api/users/me`

Role:  
Přihlášený uživatel

Chování:
- vrací detail účtu aktuálně přihlášeného uživatele
- používá se pro zobrazení profilu / nastavení v uživatelském rozhraní

Odpověď:
- HTTP 200
- `AppUserDTO`

---

## 4.2 Aktualizace profilu přihlášeného uživatele

Endpoint:  
`PUT /api/users/me/update`

Role:  
Přihlášený uživatel

Vstup:
- `AppUserDTO` (editovatelná pole: jméno, příjmení apod.)

Chování:
- aktualizuje data účtu přihlášeného uživatele
- login (e-mail) může zůstat neměnný (dle business logiky)

Odpověď:
- HTTP 200
- textová zpráva (např. `"Uživatel byl změněn"`)

Při chybě:
- HTTP 400 – validační chyba
- HTTP 401 – nepřihlášený
- `ApiError`

---

## 4.3 Změna hesla přihlášeného uživatele

Endpoint:  
`POST /api/users/me/change-password`

Role:  
Přihlášený uživatel

Vstup (`ChangePasswordDTO`):
- `oldPassword` – původní heslo
- `newPassword`
- `newPasswordConfirm`

Chování:
- ověří správnost původního hesla
- ověří shodu nového hesla a potvrzení
- nastaví nové heslo

Typické použití:
- stránka „Změna hesla“ v uživatelském profilu.

Odpověď:
- HTTP 200
- `"Heslo úspěšně změněno"`

Při chybě:
- HTTP 400 / 401 / 403
- `ApiError`

---

## 4.4 Seznam všech uživatelů (ADMIN)

Endpoint:  
`GET /api/users`

Role:  
`ADMIN`

Chování:
- vrátí seznam všech uživatelů v systému
- používá se ve „Správě uživatelů“ pro administrátory

Odpověď:
- HTTP 200
- seznam `AppUserDTO`

---

## 4.5 Detail uživatele podle ID (ADMIN)

Endpoint:  
`GET /api/users/{id}`

Role:  
`ADMIN`

Chování:
- vrátí detail konkrétního uživatele

Vstup:
- `id` – ID uživatele

Odpověď:
- HTTP 200
- `AppUserDTO`

Při chybě:
- HTTP 404 – uživatel nenalezen
- `ApiError`

---

## 4.6 Reset hesla uživatele (ADMIN)

Endpoint:  
`POST /api/users/{id}/reset-password`

Role:  
`ADMIN`

Chování:
- resetuje heslo danému uživateli na výchozí hodnotu (např. `"Player123"`)
- používá se při podpoře uživatelů (když si neví rady se zapomenutým heslem)

Odpověď:
- HTTP 200
- textová zpráva o úspěchu

---

## 4.7 Aktivace / deaktivace uživatele (ADMIN)

Endpointy:  
`PATCH /api/users/{id}/activate`  
`PATCH /api/users/{id}/deactivate`

Role:  
`ADMIN`

Chování:
- umožňují ručně aktivovat/deaktivovat účet
- deaktivovaný uživatel se nemůže přihlásit

Odpověď:
- HTTP 200
- textová zpráva

Při chybě:
- HTTP 404 – uživatel nenalezen
- `ApiError`

---

# 5. Nastavení účtu (AppUserSettingsController)

Nastavení účtu (`AppUserSettings`) obsahuje preference, které se váží na
uživatelský účet jako celek (ne na konkrétního hráče).

Základní prefix:  
`/api/user/settings`

---

## 5.1 Načtení nastavení uživatele

Endpoint:  
`GET /api/user/settings`

Role:  
Přihlášený uživatel

Chování:
- načte nastavení pro aktuálně přihlášeného uživatele
- pokud neexistuje, může se vytvořit s výchozími hodnotami

Typické použití:
- načtení uživatelských preferencí po přihlášení (např. jak vybrat aktuálního hráče).

Odpověď:
- HTTP 200
- `AppUserSettingsDTO`

---

## 5.2 Uložení nastavení uživatele

Endpoint:  
`PATCH /api/user/settings`

Role:  
Přihlášený uživatel

Vstup:
- `AppUserSettingsDTO` s novými hodnotami

Chování:
- provede aktualizaci nastavení (částečnou / úplnou)
- uložení preferencí (např. auto-výběr hráče po loginu)

Odpověď:
- HTTP 200
- `AppUserSettingsDTO` v aktuálním stavu

Při chybě:
- HTTP 400 / 401 / 403
- `ApiError`

---

# 6. Hráči (PlayerController)

Hráči (`Player`) reprezentují konkrétní osoby, které se účastní zápasů.
Uživatel (AppUser) může mít více hráčů (např. rodič a děti).

Základní prefix:  
`/api/players`

---

## 6.1 Oprávnění

ADMIN / MANAGER:
- plný CRUD nad hráči
- schvalování / zamítání hráčů
- změna vlastníka hráčů

Běžný uživatel:
- správa pouze vlastních hráčů:
  - endpointy `/api/players/me...`

---

## 6.2 Načtení seznamu všech hráčů (ADMIN / MANAGER)

Endpoint:  
`GET /api/players`

Role:
- `ADMIN` / `MANAGER`

Chování:
- vrací **všechny** hráče v systému
- slouží pro administrační přehled hráčů
- typicky se zde používají filtry (aktivní, neschválení apod.) na úrovni frontendu

Odpověď:
- HTTP 200
- seznam `PlayerDTO`

---

## 6.3 Načtení detailu hráče (ADMIN / MANAGER)

Endpoint:  
`GET /api/players/{id}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `id` – ID hráče

Chování:
- vrátí detail jednoho konkrétního hráče (vč. vazeb na uživatele, nastavení atd.)
- vhodné pro detailní kartu hráče v administraci

Odpověď:
- HTTP 200
- `PlayerDTO`

Při chybě:
- HTTP 404 – hráč nenalezen
- `ApiError`

---

## 6.4 Vytvoření hráče (ADMIN / MANAGER)

Endpoint:  
`POST /api/players`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `PlayerDTO` – údaje nového hráče

Chování:
- vytvoří nového hráče (např. při registraci nového člena přes administraci)
- může nastavit počáteční stav (např. „čeká na schválení“)

Odpověď:
- HTTP 201 (nebo 200)
- `PlayerDTO` – nově vytvořený hráč

Při chybě:
- HTTP 400 / 403
- `ApiError`

---

## 6.5 Úprava hráče (ADMIN)

Endpoint:  
`PUT /api/players/{id}`

Role:
- `ADMIN`

Vstup:
- path `id` – ID hráče
- tělo: `PlayerDTO` – nové hodnoty

Chování:
- aktualizuje existujícího hráče
- typicky se používá pro administrativní opravy, změny údajů apod.

Odpověď:
- HTTP 200
- `PlayerDTO` – upravený hráč

---

## 6.6 Smazání hráče (ADMIN)

Endpoint:  
`DELETE /api/players/{id}`

Role:
- `ADMIN`

Chování:
- smaže hráče z databáze
- může být omezeno (např. pokud existují navázané zápasy) – logika v service

Odpověď:
- HTTP 200 nebo 204

Při chybě:
- HTTP 403 / 404
- `ApiError`

---

## 6.7 Schválení hráče

Endpoint:  
`PUT /api/players/{id}/approve`

Role:
- `ADMIN` / `MANAGER`

Chování:
- změní stav hráče na „schválený“
- po schválení může být hráč plnohodnotně využíván pro registrace

Odpověď:
- HTTP 200
- `SuccessResponseDTO` nebo text (dle implementace)

---

## 6.8 Zamítnutí hráče

Endpoint:  
`PUT /api/players/{id}/reject`

Role:
- `ADMIN` / `MANAGER`

Chování:
- zamítne hráče, který čekal na schválení
- může doplnit důvod zamítnutí (dle DTO)

Odpověď:
- HTTP 200

---

## 6.9 Změna vlastníka hráče (ADMIN / MANAGER)

Endpoint:  
`POST /api/players/{playerId}/change-user`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `playerId` – ID hráče
- tělo – např. `ChangePlayerUserRequest` s `newUserId`

Chování:
- změní vazbu hráče na jiného uživatele (AppUser)
- používá se při opravě chybně přiřazeného hráče

Odpověď:
- HTTP 200
- textová zpráva o úspěchu

---

## 6.10 Vytvoření hráče pro přihlášeného uživatele

Endpoint:  
`POST /api/players/me`

Role:
- přihlášený uživatel

Vstup:
- `PlayerDTO` s údaji hráče

Chování:
- vytvoří hráče navázaného na aktuálně přihlášeného uživatele
- typicky používá rodič, který si zakládá hráče (dítě / sebe)

Odpověď:
- HTTP 201 / 200
- `PlayerDTO` – nově vytvořený hráč

---

## 6.11 Seznam hráčů přihlášeného uživatele

Endpoint:  
`GET /api/players/me`

Role:
- přihlášený uživatel

Chování:
- vrátí všechy hráče, které patří aktuálnímu uživateli
- používá se na stránce „Moji hráči“

Odpověď:
- HTTP 200
- seznam `PlayerDTO`

---

## 6.12 Úprava aktuálního hráče přihlášeného uživatele

Endpoint:  
`PUT /api/players/me`

Role:
- přihlášený uživatel

Vstup:
- `PlayerDTO` – nové hodnoty

Chování:
- v `CurrentPlayerService` se zjistí aktuální hráč
- provede se aktualizace právě tohoto hráče
- vhodné pro scénář „Editace profilu aktuálního hráče“.

Odpověď:
- HTTP 200
- `PlayerDTO` – aktualizovaný hráč

Při chybě:
- HTTP 400 / 401 / 404
- `ApiError`

---

# 7. Aktuální hráč (CurrentPlayerController)

Aktuální hráč je kontext, v němž uživatel pracuje – např. při registraci na
zápas nebo zobrazení přehledu zápasů.

Základní prefix:  
`/api/current-player`

---

## 7.1 Nastavení aktuálního hráče

Endpoint:  
`POST /api/current-player/{playerId}`

Role:
- přihlášený uživatel

Chování:
- ověří, že hráč existuje a je dostupný uživateli (vlastník / admin)
- nastaví ho jako „current player“ v uživatelském kontextu
- informace se použije např. v registračních endpointech `/me`

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

---

## 7.2 Automatický výběr aktuálního hráče

Endpoint:  
`POST /api/current-player/auto-select`

Role:
- přihlášený uživatel

Chování:
- podívá se do `AppUserSettings` a podle nich vybere vhodného hráče
  (např. prvního hráče, naposledy použitého atd.)
- pokud uživatel žádného hráče nemá, vyhodí výjimku

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

Při chybě:
- HTTP 404 – není koho vybrat
- `ApiError`

---

## 7.3 Zjištění aktuálního hráče

Endpoint:  
`GET /api/current-player`

Role:
- přihlášený uživatel

Chování:
- vrací `PlayerDTO` aktuálně nastaveného hráče
- pokud není žádný nastaven, může vracet `null` nebo chybu dle implementace

Odpověď:
- HTTP 200
- `PlayerDTO` nebo `null`

---

# 8. Nastavení hráče (PlayerSettingsController)

Nastavení hráče (`PlayerSettings`) jsou preference vázané na konkrétního
hráče, např. notifikace e-mailem/SMS pro daného hráče.

Základní prefix:  
`/api`

---

## 8.1 Načtení nastavení libovolného hráče

Endpoint:  
`GET /api/players/{playerId}/settings`

Role:
- přihlášený uživatel

Vstup:
- `playerId` – ID hráče

Chování:
- vrátí nastavení daného hráče
- oprávnění: hráče musí vlastnit přihlášený uživatel nebo mít vyšší roli

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

---

## 8.2 Aktualizace nastavení libovolného hráče

Endpoint:  
`PATCH /api/players/{playerId}/settings`

Role:
- přihlášený uživatel

Vstup:
- path `playerId`
- tělo: `PlayerSettingsDTO` – nové volby nastavení

Chování:
- aktualizuje nastavení hráče (např. zapnutí/vypnutí SMS notifikací)
- používá se v administraci hráčů

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

---

## 8.3 Načtení nastavení aktuálního hráče

Endpoint:  
`GET /api/me/settings`

Role:
- přihlášený uživatel s nastaveným aktuálním hráčem

Chování:
- pomocí `CurrentPlayerService` se zjistí aktuální hráč
- načtou se jeho `PlayerSettings`

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

Při chybě:
- HTTP 401 / 404
- `ApiError`

---

## 8.4 Aktualizace nastavení aktuálního hráče

Endpoint:  
`PATCH /api/me/settings`

Role:
- přihlášený uživatel

Vstup:
- `PlayerSettingsDTO` – změny nastavení

Chování:
- aktualizuje nastavení hráče, který je právě nastaven jako „current“
- používá se na stránce „Nastavení hráče“ pro běžného uživatele

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

---

# 9. Sezóny (SeasonController)

Sezóny představují časová období (např. 2024/2025), ke kterým se vážou
zápasy a statistiky.

Základní prefix:  
`/api/seasons`

---

## 9.1 Vytvoření sezóny

Endpoint:  
`POST /api/seasons`

Role:
- `ADMIN`

Vstup:
- `SeasonDTO` – informace o sezóně

Chování:
- vytvoří novou sezónu v systému
- standardně se po vytvoření automaticky nestává „aktivní“ (řeší se zvlášť)

Odpověď:
- HTTP 201
- `SeasonDTO` – nově vytvořená sezóna

---

## 9.2 Úprava sezóny

Endpoint:  
`PUT /api/seasons/{id}`

Role:
- `ADMIN`

Vstup:
- `id` – ID sezóny
- `SeasonDTO` – upravené údaje

Chování:
- aktualizuje existující sezónu (např. název, datumy)

Odpověď:
- HTTP 200
- `SeasonDTO`

Při chybě:
- HTTP 400 / 403 / 404
- `ApiError`

---

## 9.3 Seznam sezón (ADMIN)

Endpoint:  
`GET /api/seasons`

Role:
- `ADMIN`

Chování:
- vrátí seznam všech sezón v systému
- používá se v administračním přehledu sezón

Odpověď:
- HTTP 200
- seznam `SeasonDTO`

---

## 9.4 Aktivní sezóna (globální)

Endpointy:  
`GET /api/seasons/active`  
`PUT /api/seasons/{id}/active`

Role:
- `ADMIN`

GET – Chování:
- vrátí aktuálně globálně aktivní sezónu
- slouží jako výchozí sezóna pro ostatní části aplikace

PUT – Chování:
- nastaví sezónu s daným `id` jako globálně aktivní

Odpověď:
- HTTP 200
- `SeasonDTO` – aktivní sezóna

Při chybě:
- HTTP 404 – sezóna nenalezena
- `ApiError`

---

## 9.5 Seznam sezón pro uživatele

Endpoint:  
`GET /api/seasons/me`

Role:
- přihlášený uživatel

Chování:
- vrací seznam sezón dostupných pro volbu v uživatelském rozhraní
- typicky stejný seznam jako pro admina, ale bez admin-specific detailů

Odpověď:
- HTTP 200
- seznam `SeasonDTO`

---

## 9.6 Aktuální sezóna uživatele

Endpointy:  
`GET /api/seasons/me/current`  
`POST /api/seasons/me/current/{seasonId}`

Role:
- přihlášený uživatel

GET – Chování:
- vrátí aktuálně zvolenou sezónu pro uživatele
- pokud není nastavená, může vrátit globálně aktivní

POST – Chování:
- ověří existenci sezóny (`seasonId`)
- nastaví sezónu jako „aktuální“ pro daného uživatele (v CurrentSeasonService)

Odpověď:
- HTTP 200

Při chybě:
- HTTP 400 / 404
- `ApiError`

---

# 10. Období neaktivity hráče (PlayerInactivityPeriodController)

Období neaktivity eviduje, kdy se hráč **nemůže účastnit zápasů** (zranění,
dovolená…). Tyto endpointy jsou primárně pro administraci.

Základní prefix:  
`/api/inactivity/admin`

---

## 10.1 Seznam všech období neaktivity

Endpoint:  
`GET /api/inactivity/admin/all`

Role:
- `ADMIN` / `MANAGER`

Chování:
- vrátí seznam všech evidovaných období neaktivity pro všechny hráče
- slouží pro náhled, kdo je dlouhodobě mimo hru

Odpověď:
- HTTP 200
- seznam `PlayerInactivityPeriodDTO`

---

## 10.2 Detail období neaktivity

Endpoint:  
`GET /api/inactivity/admin/{id}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `id` – ID záznamu

Chování:
- načte detail konkrétního období neaktivity

Odpověď:
- HTTP 200
- `PlayerInactivityPeriodDTO`

Při chybě:
- HTTP 404
- `ApiError`

---

## 10.3 Seznam období neaktivity pro hráče

Endpoint:  
`GET /api/inactivity/admin/player/{playerId}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `playerId` – ID hráče

Chování:
- vrací všechna období neaktivity daného hráče
- využitelné pro detail hráče v administraci

Odpověď:
- HTTP 200
- seznam `PlayerInactivityPeriodDTO`

---

## 10.4 Vytvoření období neaktivity

Endpoint:  
`POST /api/inactivity/admin`

Role:
- `ADMIN`

Vstup:
- `PlayerInactivityPeriodDTO`

Chování:
- vytvoří nový záznam neaktivity navázaný na konkrétního hráče

Odpověď:
- HTTP 200
- `PlayerInactivityPeriodDTO` – vytvořený záznam

---

## 10.5 Aktualizace období neaktivity

Endpoint:  
`PUT /api/inactivity/admin/{id}`

Role:
- `ADMIN`

Vstup:
- `id` – ID záznamu
- `PlayerInactivityPeriodDTO` – nové hodnoty

Chování:
- aktualizuje existující období neaktivity

Odpověď:
- HTTP 200
- `PlayerInactivityPeriodDTO` – upravený záznam

---

## 10.6 Smazání období neaktivity

Endpoint:  
`DELETE /api/inactivity/admin/{id}`

Role:
- `ADMIN`

Vstup:
- `id` – ID záznamu

Chování:
- smaže období neaktivity
- ovlivňuje dostupnost hráče pro zápasy

Odpověď:
- HTTP 204

Při chybě:
- HTTP 403 / 404
- `ApiError`

---

# 11. Zápasy (MatchController)

Zápasy představují konkrétní utkání v čase, ke kterým se hráči registrují.

Základní prefix:  
`/api/matches`

---

## 11.1 Seznam všech zápasů (ADMIN / MANAGER)

Endpoint:  
`GET /api/matches`

Role:
- `ADMIN` / `MANAGER`

Chování:
- vrátí všechny zápasy v systému bez ohledu na stav (minulé / budoucí / zrušené)
- používá se v administraci zápasů

Odpověď:
- HTTP 200
- seznam `MatchDTO`

---

## 11.2 Nadcházející zápasy (ADMIN / MANAGER)

Endpoint:  
`GET /api/matches/upcoming`

Role:
- `ADMIN` / `MANAGER`

Chování:
- vrací seznam zápasů, které jsou v budoucnu a nejsou zrušené
- vhodné pro plánování sestav, kontrolu registrací

Odpověď:
- HTTP 200
- seznam `MatchDTO`

---

## 11.3 Minulé zápasy (ADMIN / MANAGER)

Endpoint:  
`GET /api/matches/past`

Role:
- `ADMIN` / `MANAGER`

Chování:
- vrátí zápasy, které už proběhly
- používá se pro zpětný přehled (docházka, statistiky)

Odpověď:
- HTTP 200
- seznam `MatchDTO`

---

## 11.4 Vytvoření zápasu (ADMIN)

Endpoint:  
`POST /api/matches`

Role:
- `ADMIN`

Vstup:
- `MatchDTO` – základní parametry zápasu (datum, čas, soupeř, místo, kapacita…)

Chování:
- vytvoří nový zápas v systému
- na nový zápas se pak mohou hráči registrovat

Odpověď:
- HTTP 201 / 200
- `MatchDTO` – nově vytvořený zápas

---

## 11.5 Detail zápasu (ADMIN / MANAGER)

Endpoint:  
`GET /api/matches/{id}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `id` – ID zápasu

Chování:
- vrátí administrativní detail zápasu (bez ohledu na konkrétního hráče)

Odpověď:
- HTTP 200
- `MatchDTO`

Při chybě:
- HTTP 404

---

## 11.6 Úprava zápasu (ADMIN / MANAGER)

Endpoint:  
`PUT /api/matches/{id}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `id` – ID zápasu
- `MatchDTO` – nové hodnoty

Chování:
- aktualizuje existující zápas (čas, soupeř, parametry registrace…)

Odpověď:
- HTTP 200
- `MatchDTO` – aktualizovaný zápas

---

## 11.7 Smazání zápasu (ADMIN)

Endpoint:  
`DELETE /api/matches/{id}`

Role:
- `ADMIN`

Vstup:
- `id` – ID zápasu

Chování:
- smaže zápas ze systému
- může být omezeno (pokud má registrace apod.)

Odpověď:
- HTTP 200
- `SuccessResponseDTO` (text)

---

## 11.8 Zápasy dostupné pro konkrétního hráče

Endpoint:  
`GET /api/matches/available-for-player/{playerId}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `playerId` – ID hráče

Chování:
- vrátí zápasy, na které se daný hráč může registrovat podle nastavených pravidel
  (čas, kapacita, stav zápasu)

Odpověď:
- HTTP 200
- seznam `MatchDTO`

---

## 11.9 Zrušení zápasu

Endpoint:  
`PATCH /api/matches/{matchId}/cancel?reason=...`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `matchId` – ID zápasu
- query `reason` – hodnota enumu `MatchCancelReason`

Chování:
- nastaví zápas do stavu „zrušený“ (+ uloží důvod)
- typicky spouští notifikace hráčům (email/SMS)

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

---

## 11.10 Obnovení zrušeného zápasu

Endpoint:  
`PATCH /api/matches/{matchId}/uncancel`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `matchId` – ID zápasu

Chování:
- vrátí zrušený zápas zpět do aktivního stavu

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

---

## 11.11 Detail zápasu z pohledu hráče

Endpoint:  
`GET /api/matches/{id}/detail`

Role:
- přihlášený uživatel

Vstup:
- `id` – ID zápasu

Chování:
- vrátí detail zápasu rozšířený o informace k aktuálnímu hráči:
  - zda je přihlášen
  - jestli je náhradník
  - kolik je volných míst
  - případná omezení změny registrace

Odpověď:
- HTTP 200
- `MatchDetailDTO`

---

## 11.12 Nejbližší zápas

Endpoint:  
`GET /api/matches/next`

Role:
- přihlášený uživatel

Chování:
- vrátí jeden nejbližší nadcházející zápas (podle data/času)
- používá se pro rychlý highlight „nejbližší zápas“ na homepage

Odpověď:
- HTTP 200
- `MatchDTO` nebo `null`

---

## 11.13 Nadcházející zápasy aktuálního hráče

Endpoint:  
`GET /api/matches/me/upcoming`

Role:
- přihlášený uživatel s aktuálním hráčem

Chování:
- vrátí nadcházející zápasy, které mají význam pro aktuálního hráče:
  - je přihlášen
  - nebo se může registrovat (dle logiky service)

Odpověď:
- HTTP 200
- seznam `MatchDTO`

---

## 11.14 Přehled nadcházejících zápasů (overview)

Endpoint:  
`GET /api/matches/me/upcoming-overview`

Role:
- přihlášený uživatel s aktuálním hráčem

Chování:
- vrací agregovaný seznam nadcházejících zápasů pro aktuálního hráče
- DTO je přizpůsobené pro přehledové karty ve frontendu

Odpověď:
- HTTP 200
- seznam `MatchOverviewDTO`

---

## 11.15 Všechny odehrané zápasy aktuálního hráče

Endpoint:  
`GET /api/matches/me/all-passed`

Role:
- přihlášený uživatel s aktuálním hráčem

Chování:
- vrátí všechny zápasy, které aktuální hráč odehrál (v minulosti)
- slouží pro historii docházky / statistiky

Odpověď:
- HTTP 200
- seznam `MatchOverviewDTO`

---

# 12. Registrace hráčů na zápasy (MatchRegistrationController)

Registrace hráčů na zápasy řeší, kdo jde na zápas, kdo je náhradník, omluvenka
apod. Jde o jednu z klíčových částí aplikace.

Základní prefix:  
`/api/registrations`

---

## 12.1 Přehled všech registrací (ADMIN / MANAGER)

Endpoint:  
`GET /api/registrations`

Role:
- `ADMIN` / `MANAGER`

Chování:
- vrátí **všechny** registrace ve všech zápasech
- spíš administrativní / diagnostický přehled

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

---

## 12.2 Registrace pro konkrétní zápas (ADMIN / MANAGER)

Endpoint:  
`GET /api/registrations/match/{matchId}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `matchId` – ID zápasu

Chování:
- vrátí registrace (účast) všech hráčů pro daný zápas

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

---

## 12.3 Registrace pro konkrétního hráče (ADMIN / MANAGER)

Endpoint:  
`GET /api/registrations/player/{playerId}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `playerId` – ID hráče

Chování:
- vrátí všechny registrace daného hráče napříč zápasy

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

---

## 12.4 Hráči bez reakce na zápas

Endpoint:  
`GET /api/registrations/match/{matchId}/no-response`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `matchId` – ID zápasu

Chování:
- vrátí seznam hráčů, kteří:
  - nejsou přihlášeni
  - nejsou omluveni
  - a zatím nijak nereagovali

Odpověď:
- HTTP 200
- seznam `PlayerDTO`

---

## 12.5 Upsert registrace za konkrétního hráče (ADMIN / MANAGER)

Endpoint:  
`POST /api/registrations/upsert/{playerId}`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- path `playerId`
- tělo: `MatchRegistrationRequest`  
  (obsahuje alespoň ID zápasu a cílový stav registrace)

Chování:
- pokud registrace neexistuje:
  - vytvoří novou
- pokud existuje:
  - aktualizuje
- umožňuje adminovi ručně zasahovat do registrací hráčů

Odpověď:
- HTTP 200
- `MatchRegistrationDTO` – výsledný stav

---

## 12.6 Označení neomluvené neúčasti

Endpoint:  
`PATCH /api/registrations/match/{matchId}/players/{playerId}/no-excused?adminNote=...`

Role:
- `ADMIN` / `MANAGER`

Vstup:
- `matchId` – ID zápasu
- `playerId` – ID hráče
- query `adminNote` (volitelné) – interní poznámka

Chování:
- nastaví stav registrace hráče na `NO_EXCUSED` (neomluvená neúčast)
- zapisuje případnou poznámku admina

Odpověď:
- HTTP 200
- `MatchRegistrationDTO` – aktualizovaná registrace

---

## 12.7 Upsert registrace aktuálního hráče (/me)

Endpoint:  
`POST /api/registrations/me/upsert`

Role:
- přihlášený uživatel s aktuálním hráčem

Vstup:
- `MatchRegistrationRequest`

Chování:
- získá ID aktuálního hráče (`CurrentPlayerService`)
- provede vytvoření nebo změnu registrace za tohoto hráče
- používá se na stránce zápasu (přihlášení/odhlášení/omluva…)

Odpověď:
- HTTP 200
- `MatchRegistrationDTO`

---

## 12.8 Registrace aktuálního hráče

Endpoint:  
`GET /api/registrations/me/for-current-player`

Role:
- přihlášený uživatel s aktuálním hráčem

Chování:
- vrátí všechny registrace aktuálního hráče
- slouží pro přehled docházky hráče

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

---

## 12.9 Přehled DTO – MatchRegistrationRequest

`MatchRegistrationRequest` – vstupní DTO pro změnu stavu registrace hráče:

Obsahuje typicky:
- `matchId` – ID zápasu
- cílový stav (např. `REGISTERED`, `UNREGISTERED`, `EXCUSED`…)
- případný důvod omluvy / poznámka

---

## 12.10 MatchRegistrationDTO

`MatchRegistrationDTO` reprezentuje stav registrace:

- vazba na zápas
- vazba na hráče
- aktuální `PlayerMatchStatus`
- čas vytvoření / změny
- poznámky

Používá se pouze pro čtení dat směrem na frontend.

---

## 12.11 Stavy registrace hráče (PlayerMatchStatus)

Výběr z enumu:

- `REGISTERED` – hráč je přihlášen
- `UNREGISTERED` – hráč odhlášen
- `EXCUSED` – omluven
- `SUBSTITUTE` – náhradník
- `NO_EXCUSED` – neomluvená neúčast

---

# 13. Historie registrací (MatchRegistrationHistoryController)

Historie registrací slouží pro audit toho, jak se registrace v čase měnila.

Základní prefix:  
`/api/registrations/history`

---

## 13.1 Historie aktuálního hráče pro daný zápas

Endpoint:  
`GET /api/registrations/history/me/matches/{matchId}`

Role:
- přihlášený uživatel s aktuálním hráčem

Vstup:
- `matchId` – ID zápasu

Chování:
- vrátí chronologii změn registrace aktuálního hráče na tento zápas

Odpověď:
- HTTP 200
- seznam `MatchRegistrationHistoryDTO`

---

## 13.2 Historie konkrétního hráče pro daný zápas (ADMIN)

Endpoint:  
`GET /api/registrations/history/admin/matches/{matchId}/players/{playerId}`

Role:
- `ADMIN`

Vstup:
- `matchId`
- `playerId`

Chování:
- vrací historii registrace daného hráče na daný zápas
- používá se např. při řešení sporů („byl jsem přihlášen / odhlášen…“)

Odpověď:
- HTTP 200
- seznam `MatchRegistrationHistoryDTO`

---

# 14. Ladicí a testovací endpointy

Tyto endpointy jsou určeny spíše pro vývoj/testování.

---

## 14.1 Debug – zobrazení Authentication (DebugController)

Endpoint:  
`GET /api/debug/me`

Role:
- typicky bez omezení v dev, v produkci by měl být vypnutý

Chování:
- vrací obsah `Authentication` objektu (přihlášený uživatel, role, autority)

Odpověď:
- HTTP 200
- JSON s detailním pohledem na security kontext

---

## 14.2 Test backendu (TestController)

Endpoint:  
`GET /api/test`

Role:
- `ADMIN`

Chování:
- jednoduchý test, že backend běží a funguje autorizace

Odpověď:
- HTTP 200
- např. text `"Backend je online!"`

---

## 14.3 Test e-mailu (TestEmailController)

Prefix controlleru:  
`/api/email/test`

Endpoint:  
`POST /api/email/test/send-mail`

Role:
- typicky bez omezení v dev (v produkci zvaž omezit)

Chování:
- odešle testovací e-mail na nastavenou adresu
- slouží k ověření e-mailové konfigurace

Odpověď:
- HTTP 200
- text `"Email odeslán"`

---

> Poznámka: V kódu je i `TestSmsController`, který je zakomentovaný a není
> součástí aktuálně používaného veřejného API.

---

# 15. Přehled hlavních DTO a enumů

Tato kapitola doplňuje předchozí popis endpointů o stručný přehled
nejdůležitějších přenášených objektů. Slouží jako „rychlá mapa“ pro někoho,
kdo API čte poprvé a chce pochopit, jaké struktury se v jednotlivých částech
opakují.

---

## 15.1 DTO pro uživatele a nastavení

### AppUserDTO

Použití:
- návratová hodnota u endpointů pro správu uživatelů (`/api/users`, `/api/auth/me`).

Typický obsah:
- `id` – ID uživatele
- `name`, `surname`
- `email`
- `roles` – množina rolí (`ADMIN`, `MANAGER`, případně uživatel bez zvláštní role)
- případně další pole (aktivní / neaktivní apod.)

### AppUserSettingsDTO

Použití:
- endpointy `/api/user/settings`.

Obsah (příklad):
- preference související s přihlášeným uživatelem:
  - jak se má vybírat aktuální hráč po loginu
  - další globální preference účtu

---

## 15.2 DTO pro hráče a jejich nastavení

### PlayerDTO

Použití:
- ve všech endpointch `/api/players...`
- jako součást jiných DTO (např. u registrací).

Obsah:
- základní informace o hráči (jméno, příjmení, číslo dresu, tým, stav schválení apod.)
- odkaz na vlastníka (uživatele)

### PlayerSettingsDTO

Použití:
- endpointy `/api/players/{playerId}/settings`, `/api/me/settings`.

Obsah:
- preferovaný způsob notifikací pro daného hráče
- volby, zda hráč chce e-maily, SMS, kopie manažerovi atd.

---

## 15.3 DTO pro sezóny

### SeasonDTO

Použití:
- správa sezón (`/api/seasons`).

Obsah:
- název sezóny (např. „2024/2025“)
- datum od / do (dle implementace)
- příznak, zda je sezóna aktivní (globálně / pro uživatele)

---

## 15.4 DTO pro zápasy

### MatchDTO

Použití:
- většina zápasových endpointů (`/api/matches...`).

Obsah:
- základní informace o zápase:
  - datum a čas
  - soupeř
  - místo
  - kapacita (max. počet hráčů)
  - stav zápasu (např. plánovaný, zrušený, odehraný)
- může obsahovat agregované počty (počet přihlášených atd.)

### MatchDetailDTO

Použití:
- endpoint `/api/matches/{id}/detail`.

Obsah:
- vše z `MatchDTO`
- plus kontext vůči aktuálnímu hráči:
  - stav registrace hráče
  - zda je na soupisce / náhradník
  - zda se může ještě odhlásit / přihlásit
  - další informace pro klientské UI

### MatchOverviewDTO

Použití:
- `/api/matches/me/upcoming-overview`
- `/api/matches/me/all-passed`.

Obsah:
- kompaktní informace vhodné pro přehledové karty:
  - základní info o zápase
  - status z pohledu hráče
  - souhrn počtu hráčů, výsledek apod. (dle implementace)

---

## 15.5 DTO pro registrace

### MatchRegistrationRequest

Použití:
- `POST /api/registrations/upsert/{playerId}`
- `POST /api/registrations/me/upsert`.

Obsah:
- `matchId` – ID zápasu
- cílový stav registrace (`PlayerMatchStatus`)
- případně textový důvod, poznámka apod.

### MatchRegistrationDTO

Použití:
- čtecí endpointy `/api/registrations...`.

Obsah:
- reference na zápas (MatchDTO nebo ID)
- reference na hráče (PlayerDTO nebo ID)
- aktuální stav (`PlayerMatchStatus`)
- metadata (čas vytvoření / změny, poznámky…)

### MatchRegistrationHistoryDTO

Použití:
- `/api/registrations/history/...`.

Obsah:
- záznam jednotlivých změn v čase
- kdo změnu provedl, kdy a z jakého na jaký stav

---

## 15.6 Enumy – přehled

### Role

Používá se v bezpečnostní vrstvě a v `AppUserDTO`:
- `ADMIN` – plný přístup k administraci
- `MANAGER` – správa zápasů, hráčů, registrací
- běžný uživatel – bez speciálního označení, ale s právy „vlastníka“ nad svými hráči

### PlayerMatchStatus

Stavy registrace hráče (viz výše):
- `REGISTERED`
- `UNREGISTERED`
- `EXCUSED`
- `SUBSTITUTE`
- `NO_EXCUSED`

### MatchStatus

Interní stav zápasu (příklad):
- plánovaný
- odehraný
- zrušený

### MatchCancelReason

Používá se v endpointu `PATCH /api/matches/{matchId}/cancel`:
- enum s důvody zrušení (např. málo hráčů, technické důvody, počasí…)

---

Tento přehled DTO a enumů doplňuje popis endpointů a usnadňuje čtení API
jako celku – je jasně vidět, jaké objekty se v jednotlivých oblastech
opakují a jak spolu souvisí.
