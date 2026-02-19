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

### 1.2 Autentizace a session

- Autentizace:
    - Spring Security (session-based)
- Přihlášení:
    - Endpoint: `POST /api/auth/login` *(konfigurace Spring Security; není explicitní controller metoda)*
    - Vstup: JSON s přihlašovacími údaji (email, password)
    - Výsledek: vytvoření HTTP session (cookie `JSESSIONID`)
- Odhlášení:
    - Endpoint: `POST /api/auth/logout` *(konfigurace Spring Security; není explicitní controller metoda)*
    - Výsledek: zneplatnění aktuální session
- Autorizace:
    - Role:
        - `ADMIN`
        - `MANAGER`
        - běžný přihlášený uživatel (bez speciální role)
    - Kontrola:
        - anotace `@PreAuthorize` v controllerech
        - pro běžného přihlášeného uživatele se používá `isAuthenticated()`
- Veřejné endpointy (nevyžadují přihlášení):
    - `/api/auth/register`
    - `/api/auth/verify`
    - `/api/auth/forgotten-password`
    - `/api/auth/forgotten-password/info`
    - `/api/auth/forgotten-password/reset`
    - `/api/auth/reset-password` *(redirect na frontend)*
    - `/api/public/app-mode` *(informace o režimu aplikace)*

Typické použití:
- Frontend po přihlášení pracuje se session cookie automaticky, není potřeba posílat tokeny v hlavičkách.

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

- `details` typicky obsahuje mapu polí → validační chybové zprávy.
- `message` je stručný popis chyby pro uživatele / frontend.

---

# 3. Veřejné informace (AppModeController)

Základní prefix: `/api/public`

## 3.1 Režim aplikace (demo/produkce)

Endpoint: `GET /api/public/app-mode`  
Role: veřejné

Odpověď:
- HTTP 200
- JSON:
```json
{ "demoMode": true }
```

---

# 4. Autentizace a registrace (AuthController)

Základní prefix: `/api/auth`

## 4.1 Registrace uživatele

Endpoint: `POST /api/auth/register`  
Role: veřejné

Vstup (`RegisterUserDTO`):
- `name`
- `surname`
- `email`
- `password`
- `passwordConfirm`

Odpověď (úspěch):
- HTTP 200
```json
{
  "status": "ok",
  "message": "Registrace úspěšná. Zkontrolujte email pro aktivaci účtu."
}
```

## 4.2 Aktivace účtu z e-mailu

Endpoint: `GET /api/auth/verify?token=...`  
Role: veřejné

Odpověď:
- HTTP 200 – text `"Účet byl úspěšně aktivován."`
- HTTP 400 – text `"Neplatný nebo expirovaný aktivační odkaz."`

## 4.3 Přihlášený uživatel (auth me)

Endpoint: `GET /api/auth/me`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `AppUserDTO`

## 4.4 Redirect na frontend pro reset hesla

Endpoint: `GET /api/auth/reset-password?token=...`  
Role: veřejné

Chování:
- HTTP 302 redirect na `${frontendBaseUrl}/reset-password?token=...`

## 4.5 Zapomenuté heslo – vytvoření požadavku

Endpoint: `POST /api/auth/forgotten-password`  
Role: veřejné

Vstup (`EmailDTO`):
- `email`

Odpověď:
- HTTP 200 (bez těla)

## 4.6 Zapomenuté heslo – info o e-mailu k tokenu

Endpoint: `GET /api/auth/forgotten-password/info?token=...`  
Role: veřejné

Odpověď:
- HTTP 200
```json
{ "email": "user@example.com" }
```

## 4.7 Zapomenuté heslo – nastavení nového hesla

Endpoint: `POST /api/auth/forgotten-password/reset`  
Role: veřejné

Vstup (`ForgottenPasswordResetDTO`):
- `token`
- `newPassword`
- `newPasswordConfirm`

Odpověď:
- HTTP 200 (bez těla)

---

# 5. Uživatelské účty (AppUserController)

Základní prefix: `/api/users`

## 5.1 Detail přihlášeného uživatele

Endpoint: `GET /api/users/me`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `AppUserDTO`

## 5.2 Aktualizace profilu přihlášeného uživatele

Endpoint: `PUT /api/users/me/update`  
Role: přihlášený uživatel

Vstup:
- `AppUserDTO`

Odpověď:
- HTTP 200 – text `"Uživatel byl změněn"`

## 5.3 Změna hesla přihlášeného uživatele

Endpoint: `POST /api/users/me/change-password`  
Role: přihlášený uživatel

Vstup (`ChangePasswordDTO`):
- `oldPassword`
- `newPassword`
- `newPasswordConfirm`

Odpověď:
- HTTP 200 – text `"Heslo úspěšně změněno"`

## 5.4 Seznam všech uživatelů (ADMIN/MANAGER)

Endpoint: `GET /api/users`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `AppUserDTO`

## 5.5 Detail uživatele podle ID (ADMIN/MANAGER)

Endpoint: `GET /api/users/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `AppUserDTO`

## 5.6 Reset hesla uživatele (ADMIN)

Endpoint: `POST /api/users/{id}/reset-password`  
Role: `ADMIN`

Odpověď:
- HTTP 200 – text `"Heslo resetováno na 'Player123'"`

## 5.7 Aktivace / deaktivace uživatele (ADMIN)

Endpointy:
- `PATCH /api/users/{id}/activate`
- `PATCH /api/users/{id}/deactivate`

Role: `ADMIN`

Odpověď:
- HTTP 200 – textová zpráva

## 5.8 Historie změn uživatele (ADMIN)

Endpoint: `GET /api/users/{id}/history`  
Role: `ADMIN`

Odpověď:
- HTTP 200
- seznam `AppUserHistoryDTO`

## 5.9 Historie změn přihlášeného uživatele

Endpoint: `GET /api/users/me/history`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- seznam `AppUserHistoryDTO`

---

# 6. Nastavení účtu (AppUserSettingsController)

Základní prefix (controller): `/api/user`

## 6.1 Načtení nastavení uživatele

Endpoint: `GET /api/user/settings`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `AppUserSettingsDTO`

## 6.2 Uložení nastavení uživatele

Endpoint: `PATCH /api/user/settings`  
Role: přihlášený uživatel

Vstup:
- `AppUserSettingsDTO`

Odpověď:
- HTTP 200
- `AppUserSettingsDTO`

---

# 7. Hráči (PlayerController)

Základní prefix: `/api/players`

## 7.1 Seznam všech hráčů (ADMIN/MANAGER)

Endpoint: `GET /api/players`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `PlayerDTO`

## 7.2 Detail hráče (ADMIN/MANAGER)

Endpoint: `GET /api/players/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerDTO`

## 7.3 Historie hráče (ADMIN/MANAGER)

Endpoint: `GET /api/players/{id}/history`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `PlayerHistoryDTO`

## 7.4 Vytvoření hráče (ADMIN/MANAGER)

Endpoint: `POST /api/players`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerDTO`

## 7.5 Úprava hráče (ADMIN/MANAGER)

Endpoint: `PUT /api/players/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerDTO`

## 7.6 Smazání hráče (ADMIN/MANAGER)

Endpoint: `DELETE /api/players/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

## 7.7 Schválení / zamítnutí hráče (ADMIN/MANAGER)

Endpointy:
- `PUT /api/players/{id}/approve`
- `PUT /api/players/{id}/reject`

Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

## 7.8 Změna vlastníka hráče (ADMIN/MANAGER)

Endpoint: `POST /api/players/{playerId}/change-user`  
Role: `ADMIN` nebo `MANAGER`

Vstup (`ChangePlayerUserRequest`):
- `newUserId`

Odpověď:
- HTTP 200 – textová zpráva

## 7.9 Vytvoření hráče pro přihlášeného uživatele

Endpoint: `POST /api/players/me`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `PlayerDTO`

## 7.10 Seznam hráčů přihlášeného uživatele

Endpoint: `GET /api/players/me`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- seznam `PlayerDTO`

## 7.11 Úprava aktuálního hráče

Endpoint: `PUT /api/players/me`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- `PlayerDTO`

## 7.12 Historie aktuálního hráče

Endpoint: `GET /api/players/me/history`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- seznam `PlayerHistoryDTO`

## 7.13 Statistiky aktuálního hráče

Endpoint: `GET /api/players/me/stats`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- `PlayerStatsDTO`

## 7.14 Statistiky hráče podle ID (ADMIN/MANAGER)

Endpoint: `GET /api/players/{playerId}/stats`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerStatsDTO`

---

# 8. Aktuální hráč (CurrentPlayerController)

Základní prefix: `/api/current-player`

## 8.1 Nastavení aktuálního hráče

Endpoint: `POST /api/current-player/{playerId}`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

## 8.2 Automatický výběr aktuálního hráče

Endpoint: `POST /api/current-player/auto-select`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

## 8.3 Zjištění aktuálního hráče

Endpoint: `GET /api/current-player`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `PlayerDTO` nebo `null`

---

# 9. Nastavení hráče (PlayerSettingsController)

Základní prefix: `/api`

## 9.1 Načtení nastavení hráče podle ID

Endpoint: `GET /api/players/{playerId}/settings`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

## 9.2 Aktualizace nastavení hráče podle ID

Endpoint: `PATCH /api/players/{playerId}/settings`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

## 9.3 Načtení nastavení aktuálního hráče

Endpoint: `GET /api/me/settings`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

## 9.4 Aktualizace nastavení aktuálního hráče

Endpoint: `PATCH /api/me/settings`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- `PlayerSettingsDTO`

---

# 10. Sezóny (SeasonController)

Základní prefix: `/api/seasons`

## 10.1 Vytvoření sezóny (ADMIN/MANAGER)

Endpoint: `POST /api/seasons`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 201
- `SeasonDTO`

## 10.2 Úprava sezóny (ADMIN/MANAGER)

Endpoint: `PUT /api/seasons/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `SeasonDTO`

## 10.3 Historie sezóny (ADMIN/MANAGER)

Endpoint: `GET /api/seasons/{id}/history`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `SeasonHistoryDTO`

## 10.4 Seznam sezón (ADMIN/MANAGER)

Endpoint: `GET /api/seasons`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `SeasonDTO`

## 10.5 Aktivní sezóna (globální) (ADMIN/MANAGER)

Endpointy:
- `GET /api/seasons/active`
- `PUT /api/seasons/{id}/active`

Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `SeasonDTO`

## 10.6 Seznam sezón pro uživatele

Endpoint: `GET /api/seasons/me`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- seznam `SeasonDTO`

## 10.7 Aktuální sezóna uživatele

Endpointy:
- `GET /api/seasons/me/current`
- `POST /api/seasons/me/current/{seasonId}`

Role: přihlášený uživatel

Odpověď:
- HTTP 200 (GET vrací `SeasonDTO` nebo `null`, POST nemá tělo)

---

# 11. Období neaktivity hráče (PlayerInactivityPeriodController)

Základní prefix: `/api/inactivity/admin`

## 11.1 Seznam všech období neaktivity (ADMIN/MANAGER)

Endpoint: `GET /api/inactivity/admin/all`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `PlayerInactivityPeriodDTO`

## 11.2 Detail období neaktivity (ADMIN/MANAGER)

Endpoint: `GET /api/inactivity/admin/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerInactivityPeriodDTO`

## 11.3 Seznam období neaktivity pro hráče (ADMIN/MANAGER)

Endpoint: `GET /api/inactivity/admin/player/{playerId}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `PlayerInactivityPeriodDTO`

## 11.4 Vytvoření období neaktivity (ADMIN/MANAGER)

Endpoint: `POST /api/inactivity/admin`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerInactivityPeriodDTO`

## 11.5 Aktualizace období neaktivity (ADMIN/MANAGER)

Endpoint: `PUT /api/inactivity/admin/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `PlayerInactivityPeriodDTO`

## 11.6 Smazání období neaktivity (ADMIN/MANAGER)

Endpoint: `DELETE /api/inactivity/admin/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 204

## 11.7 Moje období neaktivity (aktuální hráč)

Endpoint: `GET /api/inactivity/admin/me/all`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- seznam `PlayerInactivityPeriodDTO`

---

# 12. Zápasy (MatchController)

Základní prefix: `/api/matches`

## 12.1 Seznam všech zápasů (ADMIN/MANAGER)

Endpoint: `GET /api/matches`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchDTO`

## 12.2 Nadcházející / minulé zápasy (ADMIN/MANAGER)

Endpointy:
- `GET /api/matches/upcoming`
- `GET /api/matches/past`

Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchDTO`

## 12.3 Vytvoření zápasu (ADMIN/MANAGER)

Endpoint: `POST /api/matches`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `MatchDTO`

## 12.4 Detail zápasu (ADMIN/MANAGER)

Endpoint: `GET /api/matches/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `MatchDTO`

## 12.5 Historie zápasu (ADMIN/MANAGER)

Endpoint: `GET /api/matches/{id}/history`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchHistoryDTO`

## 12.6 Úprava zápasu (ADMIN/MANAGER)

Endpoint: `PUT /api/matches/{id}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `MatchDTO`

## 12.7 Smazání zápasu (ADMIN)

Endpoint: `DELETE /api/matches/{id}`  
Role: `ADMIN`

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

## 12.8 Zápasy dostupné pro konkrétního hráče (ADMIN/MANAGER)

Endpoint: `GET /api/matches/available-for-player/{playerId}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchDTO`

## 12.9 Zrušení / obnovení zápasu (ADMIN/MANAGER)

Endpointy:
- `PATCH /api/matches/{matchId}/cancel?reason=...`
- `PATCH /api/matches/{matchId}/uncancel`

Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `SuccessResponseDTO`

## 12.10 Detail zápasu z pohledu hráče

Endpoint: `GET /api/matches/{id}/detail`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `MatchDetailDTO`

## 12.11 Nejbližší zápas

Endpoint: `GET /api/matches/next`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- `MatchDTO` nebo `null`

## 12.12 Nadcházející zápasy aktuálního hráče

Endpoint: `GET /api/matches/me/upcoming`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- seznam `MatchDTO`

## 12.13 Přehled nadcházejících zápasů (overview)

Endpoint: `GET /api/matches/me/upcoming-overview`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- seznam `MatchOverviewDTO`

## 12.14 Všechny odehrané zápasy aktuálního hráče

Endpoint: `GET /api/matches/me/all-passed`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- seznam `MatchOverviewDTO`

---

# 13. Registrace hráčů na zápasy (MatchRegistrationController)

Základní prefix: `/api/registrations`

## 13.1 Přehled všech registrací (ADMIN/MANAGER)

Endpoint: `GET /api/registrations`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

## 13.2 Registrace pro zápas / hráče (ADMIN/MANAGER)

Endpointy:
- `GET /api/registrations/match/{matchId}`
- `GET /api/registrations/player/{playerId}`

Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

## 13.3 Hráči bez reakce na zápas (ADMIN/MANAGER)

Endpoint: `GET /api/registrations/match/{matchId}/no-response`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `PlayerDTO`

## 13.4 Upsert registrace za konkrétního hráče (ADMIN/MANAGER)

Endpoint: `POST /api/registrations/upsert/{playerId}`  
Role: `ADMIN` nebo `MANAGER`

Vstup (`MatchRegistrationRequest`):
- minimálně `matchId`
- cílový stav / doplňující data dle DTO

Odpověď:
- HTTP 200
- `MatchRegistrationDTO`

## 13.5 Označení neomluvené neúčasti (ADMIN/MANAGER)

Endpoint: `PATCH /api/registrations/match/{matchId}/players/{playerId}/no-excused?adminNote=...`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `MatchRegistrationDTO`

## 13.6 Zrušení neomluvené neúčasti a nastavení omluvy (ADMIN/MANAGER)

Endpoint: `PATCH /api/registrations/match/{matchId}/players/{playerId}/cancel-no-excused?excuseReason=...&excuseNote=...`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- `MatchRegistrationDTO`

## 13.7 Upsert registrace aktuálního hráče

Endpoint: `POST /api/registrations/me/upsert`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- `MatchRegistrationDTO`

## 13.8 Registrace aktuálního hráče

Endpoint: `GET /api/registrations/me/for-current-player`  
Role: přihlášený uživatel + vybraný current player

Odpověď:
- HTTP 200
- seznam `MatchRegistrationDTO`

---

# 14. Historie registrací (MatchRegistrationHistoryController)

Základní prefix: `/api/registrations/history`

## 14.1 Historie aktuálního hráče pro daný zápas

Endpoint: `GET /api/registrations/history/me/matches/{matchId}`  
Role: přihlášený uživatel

Odpověď:
- HTTP 200
- seznam `MatchRegistrationHistoryDTO`

## 14.2 Historie konkrétního hráče pro daný zápas (ADMIN/MANAGER)

Endpoint: `GET /api/registrations/history/admin/matches/{matchId}/players/{playerId}`  
Role: `ADMIN` nebo `MANAGER`

Odpověď:
- HTTP 200
- seznam `MatchRegistrationHistoryDTO`

---

# 15. Uživatelský kontext (MeController)

Základní prefix: `/api/me`

## 15.1 Informace o impersonaci

Endpoint: `GET /api/me/impersonation`  
Role: přihlášení (v praxi; controller nemá anotaci, ale endpoint předpokládá security)

Odpověď:
- HTTP 200
- `ImpersonationInfoDTO`
```json
{ "impersonating": true, "playerId": 123, "playerName": "Jan Novák" }
```

---

# 16. Demo notifikace (DemoNotificationController)

Základní prefix: `/api/demo/notifications`

> Controller je aktivní pouze v demo režimu (`app.demo-mode=true`).  
> Pokud demo režim není aktivní, endpointy nejsou registrovány a typicky vrací 404.

## 16.1 Načtení a vymazání zachycených demo notifikací

Endpoint: `GET /api/demo/notifications`  
Role: dle zabezpečení aplikace (controller nemá `@PreAuthorize`)

Odpověď:
- HTTP 200
- `DemoNotificationsDTO`

## 16.2 Vymazání demo notifikací

Endpoint: `DELETE /api/demo/notifications`  
Role: dle zabezpečení aplikace (controller nemá `@PreAuthorize`)

Odpověď:
- HTTP 204

---

# 17. Ladicí a testovací endpointy

## 17.1 Debug – zobrazení Authentication (DebugController)

Endpoint: `GET /api/debug/me`

Odpověď:
- HTTP 200
- JSON s obsahem `Authentication`

## 17.2 Test backendu (TestController)

Endpoint: `GET /api/test`  
Role: `ADMIN`

Odpověď:
- HTTP 200
- text `"Backend je online!"`

## 17.3 Test e-mailu (TestEmailController)

Endpoint: `POST /api/email/test/send-mail`

Odpověď:
- HTTP 200
- text `"Email odeslán"`

---

# 18. Přehled hlavních DTO a enumů

Tato kapitola je orientační. Konkrétní struktury polí se řídí aktuálními DTO třídami na backendu.

## 18.1 DTO – uživatel

- `AppUserDTO`
- `AppUserHistoryDTO`
- `AppUserSettingsDTO`
- `ChangePasswordDTO`
- `RegisterUserDTO`
- `ForgottenPasswordResetDTO`
- `EmailDTO`
- `ImpersonationInfoDTO`
- `DemoNotificationsDTO`

## 18.2 DTO – hráč

- `PlayerDTO`
- `PlayerHistoryDTO`
- `PlayerStatsDTO`
- `PlayerSettingsDTO`
- `PlayerInactivityPeriodDTO`
- `PlayerSummaryDTO`

## 18.3 DTO – zápasy a registrace

- `MatchDTO`
- `MatchDetailDTO`
- `MatchOverviewDTO`
- `MatchHistoryDTO`
- `MatchRegistrationDTO`
- `MatchRegistrationHistoryDTO`
- `NumberedMatchDTO`
- `SeasonDTO`
- `SeasonHistoryDTO`
- `SuccessResponseDTO`


## 18.4 Enumy – přehled

- `Role`: `ROLE_PLAYER`, `ROLE_MANAGER`, `ROLE_ADMIN`
- `PlayerMatchStatus`: `REGISTERED`, `UNREGISTERED`, `EXCUSED`, `RESERVED`, `NO_RESPONSE`, `SUBSTITUTE`, `NO_EXCUSED`
- `MatchStatus`: `UNCANCELED`, `CANCELED`, `UPDATED`
- `MatchCancelReason`: `NOT_ENOUGH_PLAYERS`, `TECHNICAL_ISSUE`, `WEATHER`, `ORGANIZER_DECISION`, `OTHER`
- `ExcuseReason`: `NEMOC`, `PRACE`, `NECHE_SE_MI`, `JINE`
- `PlayerStatus`: `PENDING`, `APPROVED`, `REJECTED`, `ARCHIVED`
- `PlayerType`: `VIP`, `STANDARD`, `BASIC`
- `Team`: `DARK`, `LIGHT`
- `GlobalNotificationLevel`: `ALL`, `IMPORTANT_ONLY`, `NONE`
- `NotificationCategory`: `REGISTRATION`, `MATCH_INFO`, `SYSTEM`
- `NotificationChannel`: `EMAIL`, `SMS`
- `NotificationType`: `MATCH_REGISTRATION_CREATED`, `MATCH_REGISTRATION_UPDATED`, `MATCH_REGISTRATION_CANCELED`, `MATCH_REGISTRATION_RESERVED`, `MATCH_REGISTRATION_SUBSTITUTE`, `MATCH_WAITING_LIST_MOVED_UP`, `MATCH_REGISTRATION_NO_RESPONSE`, `PLAYER_EXCUSED`, `PLAYER_NO_EXCUSED`, `MATCH_REMINDER`, `MATCH_CANCELED`, `MATCH_UNCANCELED`, `MATCH_TIME_CHANGED`, `PLAYER_CREATED`, `PLAYER_UPDATED`, `PLAYER_APPROVED`, `PLAYER_REJECTED`, `PLAYER_DELETED`, `PLAYER_CHANGE_USER`, `USER_CREATED`, `USER_ACTIVATED`, `USER_DEACTIVATED`, `USER_UPDATED`, `PASSWORD_RESET`, `USER_CHANGE_PASSWORD`, `FORGOTTEN_PASSWORD_RESET_REQUEST`, `FORGOTTEN_PASSWORD_RESET_COMPLETED`, `SECURITY_ALERT`
