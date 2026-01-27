package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import cz.phsoft.hokej.models.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller pro autentizaci a registraci uživatelů.
 * <p>
 * Zajišťuje:
 * <ul>
 *     <li>registraci nových uživatelů,</li>
 *     <li>aktivaci uživatelského účtu pomocí ověřovacího tokenu,</li>
 *     <li>získání informací o aktuálně přihlášeném uživateli.</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link AppUserService}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Zaregistruje nového uživatele.
     * <p>
     * Po úspěšné registraci je uživateli odeslán aktivační e-mail
     * s ověřovacím odkazem.
     *
     * @param dto registrační údaje uživatele
     * @return informace o úspěšném přijetí registrace
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
     * Vrátí informace o aktuálně přihlášeném uživateli.
     *
     * @param authentication objekt s informacemi o přihlášeném uživateli
     * @return detail přihlášeného uživatele
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUserDTO> getCurrentUser(Authentication authentication) {
        AppUserDTO dto = appUserService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(dto);
    }

    /**
     * Aktivuje uživatelský účet na základě ověřovacího tokenu.
     * <p>
     * Token je zaslán uživateli e-mailem po registraci a má omezenou platnost.
     *
     * @param token aktivační token
     * @return výsledek aktivace účtu
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean activated = appUserService.activateUser(token);

        if (!activated) {
            // sjednocená odpověď pro neplatný nebo expirovaný token
            return ResponseEntity
                    .badRequest()
                    .body("Neplatný nebo expirovaný aktivační odkaz.");
        }

        return ResponseEntity.ok("Účet byl úspěšně aktivován.");
    }
}
