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
 * REST controller zajišťující autentizační a registrační operace.
 *
 * Controller zprostředkovává registraci uživatelů, aktivaci účtů,
 * práci s přihlášeným uživatelem a proces zapomenutého hesla.
 * Veškerá aplikační logika je delegována do servisní vrstvy
 * reprezentované rozhraním AppUserService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;

    /**
     * Základní URL frontendové SPA aplikace.
     *
     * Hodnota se používá při přesměrování uživatele
     * během procesu resetu hesla.
     */
    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    /**
     * Vytvoří instanci controlleru.
     *
     * @param appUserService servisní vrstva pro práci s uživateli
     */
    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Registruje nového uživatele.
     *
     * Po úspěšné registraci je vytvořen aktivační token
     * a je odeslána notifikace s aktivačním odkazem.
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
     * Token je získán z aktivačního odkazu zaslaného po registraci.
     * V případě neplatného nebo expirovaného tokenu je vrácena chyba 400.
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
     * Přesměrovává uživatele na frontendovou stránku pro reset hesla.
     *
     * Backend vrací HTTP 302 a předává reset token jako parametr,
     * zatímco samotné nastavení nového hesla probíhá přes REST endpoint.
     *
     * @param token reset token pro zapomenuté heslo
     * @return HTTP odpověď s hlavičkou Location
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
     * Pro zadanou e-mailovou adresu je vytvořen reset token
     * a je odeslána notifikace s odkazem pro nastavení nového hesla.
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
     * Vrací e-mailovou adresu svázanou s reset tokenem.
     *
     * Endpoint se používá například pro zobrazení
     * informace o účtu na stránce resetu hesla.
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
     * Nastaví nové heslo na základě reset tokenu.
     *
     * Informace o tokenu a novém hesle jsou předány
     * prostřednictvím ForgottenPasswordResetDTO.
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