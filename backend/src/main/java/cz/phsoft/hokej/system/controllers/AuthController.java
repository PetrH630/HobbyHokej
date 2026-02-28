package cz.phsoft.hokej.system.controllers;

import cz.phsoft.hokej.user.dto.AppUserDTO;
import cz.phsoft.hokej.notifications.dto.EmailDTO;
import cz.phsoft.hokej.user.dto.ForgottenPasswordResetDTO;
import cz.phsoft.hokej.user.dto.RegisterUserDTO;
import cz.phsoft.hokej.user.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller, který se používá pro autentizaci a registraci uživatelů.
 *
 * Zajišťuje registraci nových uživatelů, aktivaci účtů pomocí ověřovacího
 * tokenu, práci s přihlášeným uživatelem a proces zapomenutého hesla
 * včetně vystavení tokenu a nastavení nového hesla.
 *
 * Veškerá business logika se předává do {@link AppUserService}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;

    /**
     * Základní URL frontendové SPA aplikace (React/Vite).
     *
     * Tato hodnota se používá pro přesměrování uživatele při procesu
     * resetu hesla, aby mohl být otevřen správný route na frontend aplikaci.
     */
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Registruje nového uživatele.
     *
     * Po úspěšné registraci se vytváří aktivační token a odesílá se
     * aktivační e-mail s odkazem na aktivaci účtu.
     *
     * @param dto registrační údaje nového uživatele
     * @return HTTP odpověď s informací o úspěšné registraci
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDTO dto) {
        appUserService.register(dto);
        return ResponseEntity.ok(
                Map.of(
                        "status", "ok",
                        "message", "Registrace úspěšná. Zkontrolujte email pro aktivaci účtu."
                )
        );
    }

    /**
     * Vrací informace o aktuálně přihlášeném uživateli.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return DTO s detaily přihlášeného uživatele
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUserDTO> getCurrentUser(Authentication authentication) {
        AppUserDTO dto = appUserService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(dto);
    }

    /**
     * Aktivuje uživatelský účet na základě ověřovacího tokenu.
     *
     * Token se získává z aktivačního odkazu zaslaného po registraci
     * a má omezenou platnost. V případě neplatného nebo expirovaného
     * tokenu se vrací chyba 400.
     *
     * @param token aktivační token
     * @return textová informace o výsledku aktivace účtu
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean activated = appUserService.activateUser(token);

        if (!activated) {
            return ResponseEntity
                    .badRequest()
                    .body("Neplatný nebo expirovaný aktivační odkaz.");
        }

        return ResponseEntity.ok("Účet byl úspěšně aktivován.");
    }

    /**
     * Přesměrovává uživatele z odkazu v e-mailu na frontendovou stránku
     * pro nastavení nového hesla.
     *
     * Backend provádí redirect na odpovídající route frontendové SPA
     * a předává reset token jako query parametr. Samotná změna hesla
     * se následně provádí pomocí REST endpointů pro zapomenuté heslo.
     *
     * @param token reset token pro zapomenuté heslo
     * @return HTTP 302 s hlavičkou Location na frontendovou URL
     */
    @GetMapping("/reset-password")
    public ResponseEntity<Void> redirectResetPassword(@RequestParam String token) {
        String targetUrl = frontendBaseUrl + "/reset-password?token=" + token;

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header("Location", targetUrl)
                .build();
    }

    /**
     * Vytváří požadavek na reset zapomenutého hesla.
     *
     * Na základě zadané e-mailové adresy se vytvoří reset token
     * a odešle se e-mail s odkazem pro nastavení nového hesla.
     *
     * @param dto DTO s e-mailovou adresou uživatele
     * @return HTTP odpověď 200 v případě úspěchu
     */
    @PostMapping("/forgotten-password")
    public ResponseEntity<Void> requestForgottenPassword(@RequestBody @Valid EmailDTO dto) {
        appUserService.requestForgottenPasswordReset(dto.getEmail());
        return ResponseEntity.ok().build();
    }

    /**
     * Vrací informaci o e-mailu, ke kterému přísluší daný reset token.
     *
     * Endpoint se používá například pro zobrazení e-mailové adresy
     * na frontendové stránce pro reset hesla.
     *
     * @param token reset token
     * @return mapování obsahující e-mail navázaný na token
     */
    @GetMapping("/forgotten-password/info")
    public ResponseEntity<Map<String, String>> getForgottenPasswordInfo(@RequestParam String token) {
        String email = appUserService.getForgottenPasswordResetEmail(token);
        return ResponseEntity.ok(Map.of("email", email));
    }

    /**
     * Provádí nastavení nového hesla na základě reset tokenu.
     *
     * Informace o tokenu, novém hesle a jeho potvrzení se předává
     * prostřednictvím {@link ForgottenPasswordResetDTO}.
     *
     * @param dto DTO obsahující token a nové heslo
     * @return HTTP odpověď 200 v případě úspěchu
     */
    @PostMapping("/forgotten-password/reset")
    public ResponseEntity<Void> forgottenPasswordReset(@RequestBody @Valid ForgottenPasswordResetDTO dto) {
        appUserService.forgottenPasswordReset(dto);
        return ResponseEntity.ok().build();
    }
}
