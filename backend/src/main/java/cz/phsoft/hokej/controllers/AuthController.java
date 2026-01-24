package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import cz.phsoft.hokej.models.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;

    }
    // ===== Registrace =====
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDTO dto) {
        appUserService.register(dto);
        return ResponseEntity.ok(
                Map.of("status", "ok", "message", "Registrace úspěšná. Zkontrolujte email pro aktivaci účtu.")
        );
    }
    // ===== Získání aktuálního uživatele =====
    @GetMapping("/me")
    public ResponseEntity<AppUserDTO> getCurrentUser(Authentication authentication) {
        AppUserDTO dto = appUserService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(dto);
    }
    // ===== Aktivace účtu =====
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean activated = appUserService.activateUser(token);

        if (!activated) {
            // sjednocená chyba: neplatný NEBO expirovaný token
            return ResponseEntity
                    .badRequest()
                    .body("Neplatný nebo expirovaný aktivační odkaz.");
        }

        return ResponseEntity.ok("Účet byl úspěšně aktivován.");
    }
}

