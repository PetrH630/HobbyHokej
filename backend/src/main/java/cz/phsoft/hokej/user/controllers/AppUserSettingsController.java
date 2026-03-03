package cz.phsoft.hokej.user.controllers;

import cz.phsoft.hokej.user.dto.AppUserSettingsDTO;
import cz.phsoft.hokej.user.services.AppUserSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller, který se používá pro správu nastavení uživatele
 * na úrovni aplikačního účtu (AppUser).
 *
 * Pracuje s nastavením navázaným na účet uživatele, nikoli na aktuálního
 * hráče. Slouží například pro nastavení preferencí uživatele a režimu
 * výběru hráče po přihlášení.
 *
 * Veškerá business logika se deleguje do AppUserSettingsService.
 */
@RestController
@RequestMapping("/api/user")
public class AppUserSettingsController {

    private final AppUserSettingsService appUserSettingsService;

    /**
     * Vytváří instanci controlleru pro správu nastavení uživatele.
     *
     * Předaná služba se používá pro čtení a aktualizaci nastavení
     * na úrovni aplikačního uživatele. Controller pouze zpracovává
     * HTTP požadavky a deleguje práci do service vrstvy.
     *
     * @param appUserSettingsService služba pro správu nastavení uživatele
     */
    public AppUserSettingsController(AppUserSettingsService appUserSettingsService) {
        this.appUserSettingsService = appUserSettingsService;
    }

    /**
     * Vrací nastavení aktuálně přihlášeného uživatele.
     *
     * Uživatel se identifikuje pomocí e-mailu získaného z objektu
     * Authentication. Metoda deleguje načtení nastavení do AppUserSettingsService.
     *
     * @param auth autentizační kontext přihlášeného uživatele
     * @return DTO s nastavením uživatele
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
     * Očekává se, že frontend předá kompletní stav nastavení, který se
     * aplikuje na účet uživatele. Metoda deleguje aktualizaci do
     * AppUserSettingsService a vrací aktualizovaný stav.
     *
     * @param auth       autentizační kontext přihlášeného uživatele
     * @param requestDto DTO s novým nastavením uživatele
     * @return DTO s aktualizovaným nastavením uživatele
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