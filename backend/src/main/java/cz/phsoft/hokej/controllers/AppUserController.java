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
 * REST controller, který se používá pro správu uživatelských účtů.
 *
 * Zajišťuje práci s přihlášeným uživatelem, včetně zobrazení profilu a změny
 * hesla, a také administrativní správu uživatelů, která je vyhrazena roli ADMIN.
 *
 * Veškerá business logika se předává do {@link AppUserService}.
 */
@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    /**
     * Vrací detail aktuálně přihlášeného uživatele.
     *
     * Identifikace uživatele se provádí podle e-mailu (username),
     * který je získán z objektu {@link Authentication}.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return DTO s detaily přihlášeného uživatele
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AppUserDTO getCurrentUser(Authentication authentication) {
        return appUserService.getCurrentUser(authentication.getName());
    }

    /**
     * Aktualizuje údaje aktuálně přihlášeného uživatele.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @param dto            DTO s aktualizovanými údaji uživatele
     * @return HTTP odpověď s informací o úspěšné aktualizaci
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
     * Mění heslo aktuálně přihlášeného uživatele.
     *
     * Staré heslo, nové heslo a potvrzení nového hesla se předává
     * prostřednictvím DTO {@link ChangePasswordDTO}.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @param dto            DTO obsahující staré a nové heslo
     * @return HTTP odpověď s informací o úspěšné změně hesla
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

    // ADMIN

    /**
     * Vrací seznam všech uživatelů v systému.
     *
     * Endpoint je dostupný pouze pro roli ADMIN.
     *
     * @return seznam uživatelů jako {@link AppUserDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserDTO> getAllUsers() {
        return appUserService.getAllUsers();
    }

    /**
     * Vrací detail uživatele podle jeho ID.
     *
     * Endpoint je dostupný pouze pro roli ADMIN.
     *
     * @param id ID uživatele
     * @return DTO s detaily vybraného uživatele
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppUserDTO getUserById(@PathVariable Long id) {
        return appUserService.getUserById(id);
    }

    /**
     * Resetuje heslo uživatele na výchozí hodnotu.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id ID uživatele, kterému se má heslo resetovat
     * @return HTTP odpověď s informací o úspěšném resetu hesla
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> resetPassword(@PathVariable Long id) {
        appUserService.resetPassword(id);
        return ResponseEntity.ok("Heslo resetováno na 'Player123'");
    }

    /**
     * Aktivuje účet uživatele.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id ID uživatele, který má být aktivován
     * @return HTTP odpověď s informací o úspěšné aktivaci
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUserByAdmin(@PathVariable Long id) {
        appUserService.activateUserByAdmin(id);
        return ResponseEntity.ok("Uživatel byl úspěšně aktivován");
    }

    /**
     * Deaktivuje účet uživatele.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id ID uživatele, který má být deaktivován
     * @return HTTP odpověď s informací o úspěšné deaktivaci
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUserByAdmin(@PathVariable Long id) {
        appUserService.deactivateUserByAdmin(id);
        return ResponseEntity.ok("Uživatel byl úspěšně deaktivován");
    }
}
