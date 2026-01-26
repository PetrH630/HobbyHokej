package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.ChangePasswordDTO;
import cz.phsoft.hokej.models.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro správu uživatelských účtů.
 * <p>
 * Zajišťuje:
 * <ul>
 *     <li>práci s přihlášeným uživatelem (profil, změna hesla),</li>
 *     <li>administrativní správu uživatelů (pouze ADMIN).</li>
 * </ul>
 * <p>
 * Veškerá business logika je delegována do {@link AppUserService}.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Vrátí detail aktuálně přihlášeného uživatele.
     * <p>
     * Identifikace uživatele probíhá pomocí e-mailu (username)
     * získaného z {@link Authentication}.
     *
     * @param authentication objekt s informacemi o přihlášeném uživateli
     * @return detail přihlášeného uživatele
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AppUserDTO getCurrentUser(Authentication authentication) {
        return appUserService.getCurrentUser(authentication.getName());
    }

    /**
     * Změní heslo aktuálně přihlášeného uživatele.
     *
     * @param authentication objekt s informacemi o přihlášeném uživateli
     * @param dto            DTO obsahující staré a nové heslo
     * @return informace o úspěšném provedení změny
     */
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO dto) {

        String email = authentication.getName();
        appUserService.changePassword(
                email,
                dto.getOldPassword(),
                dto.getNewPassword(),
                dto.getNewPasswordConfirm()
        );
        return ResponseEntity.ok("Heslo úspěšně změněno");
    }

    /**
     * Aktualizuje údaje aktuálně přihlášeného uživatele.
     *
     * @param authentication objekt s informacemi o přihlášeném uživateli
     * @param dto            aktualizovaná data uživatele
     * @return informace o úspěšné aktualizaci
     */
    @PutMapping("/me/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> updateUser(
            Authentication authentication,
            @Valid @RequestBody AppUserDTO dto) {

        String email = authentication.getName();
        appUserService.updateUser(email, dto);
        return ResponseEntity.ok("Uživatel byl změněn");
    }

    /**
     * Resetuje heslo uživatele na výchozí hodnotu.
     * <p>
     * Operace je vyhrazena pouze pro administrátora.
     *
     * @param id ID uživatele
     * @return informace o úspěšném resetu hesla
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resetPassword(@PathVariable Long id) {
        appUserService.resetPassword(id);
        return ResponseEntity.ok("Heslo resetováno na 'Player123'");
    }

    /**
     * Vrátí seznam všech uživatelů v systému.
     * <p>
     * Endpoint je dostupný pouze pro administrátora.
     *
     * @return seznam uživatelů
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserDTO> getAllUsers() {
        return appUserService.getAllUsers();
    }

    /**
     * Aktivuje účet uživatele.
     * <p>
     * Endpoint je dostupný pouze pro administrátora.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUserByAdmin(@PathVariable Long id) {
        appUserService.activateUserByAdmin(id);
        return ResponseEntity.ok("Uživatel byl úspěšně aktivován");
    }

    /**
     * Dektivuje účet uživatele.
     * <p>
     * Endpoint je dostupný pouze pro administrátora.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUserByAdmin(@PathVariable Long id) {
        appUserService.deactivateUserByAdmin(id);
        return ResponseEntity.ok("Uživatel byl úspěšně deaktivován");
    }
}
