package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.AppUserSettingsDTO;
import cz.phsoft.hokej.models.services.AppUserSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller pro nastavení uživatele (AppUser).
 *
 * Kontext: účet / uživatel, nikoliv currentPlayer.
 *
 * API:
 *  GET  /api/user/settings   – načtení nastavení přihlášeného uživatele
 *  PATCH /api/user/settings  – aktualizace nastavení přihlášeného uživatele
 */
@RestController
@RequestMapping("/api/user")
public class AppUserSettingsController {

    private final AppUserSettingsService appUserSettingsService;

    public AppUserSettingsController(AppUserSettingsService appUserSettingsService) {
        this.appUserSettingsService = appUserSettingsService;
    }

    /**
     * Vrátí nastavení aktuálně přihlášeného uživatele.
     *
     * Identifikace uživatele probíhá přes Authentication.getName()
     * (typicky email).
     */
    @GetMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUserSettingsDTO> getCurrentUserSettings(Authentication auth) {
        String userEmail = auth.getName();

        AppUserSettingsDTO dto = appUserSettingsService.getSettingsForUser(userEmail);

        return ResponseEntity.ok(dto);
    }

    /**
     * Aktualizuje nastavení aktuálně přihlášeného uživatele.
     *
     * Předpoklad: FE posílá kompletní stav nastavení (full update).
     */
    @PatchMapping("/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppUserSettingsDTO> updateCurrentUserSettings(
            Authentication auth,
            @RequestBody AppUserSettingsDTO requestDto
    ) {
        String userEmail = auth.getName();

        AppUserSettingsDTO updated = appUserSettingsService.updateSettingsForUser(userEmail, requestDto);

        return ResponseEntity.ok(updated);
    }
}
