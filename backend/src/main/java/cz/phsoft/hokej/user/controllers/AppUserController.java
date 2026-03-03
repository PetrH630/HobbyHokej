package cz.phsoft.hokej.user.controllers;

import cz.phsoft.hokej.user.dto.AppUserDTO;
import cz.phsoft.hokej.user.dto.AppUserHistoryDTO;
import cz.phsoft.hokej.user.dto.ChangePasswordDTO;
import cz.phsoft.hokej.user.services.AppUserHistoryService;
import cz.phsoft.hokej.user.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller, který se používá pro správu uživatelských účtů.
 *
 * Zajišťuje práci s přihlášeným uživatelem, včetně zobrazení profilu,
 * historie změn a změny hesla, a také administrativní správu uživatelů,
 * která je vyhrazena rolím ADMIN a MANAGER.
 *
 * Veškerá business logika se deleguje do AppUserService a AppUserHistoryService.
 */
@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;
    private final AppUserHistoryService appUserHistoryService;

    /**
     * Vytváří instanci controlleru pro správu uživatelských účtů.
     *
     * Vstupní služby se používají pro práci s daty uživatelů a jejich historií.
     * Controller pouze přijímá HTTP požadavky, provádí základní validace
     * a deleguje zpracování do service vrstvy.
     *
     * @param appUserService služba pro správu uživatelských účtů
     * @param appUserHistoryService služba pro práci s historií uživatelů
     */
    public AppUserController(AppUserService appUserService,
                             AppUserHistoryService appUserHistoryService) {
        this.appUserService = appUserService;
        this.appUserHistoryService = appUserHistoryService;
    }

    /**
     * Vrací detail aktuálně přihlášeného uživatele.
     *
     * Identifikace uživatele se provádí podle e-mailu (username),
     * který je získán z objektu Authentication. Metoda deleguje
     * načtení údajů do AppUserService.
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
     * Metoda získá e-mail přihlášeného uživatele z autentizačního kontextu
     * a deleguje aktualizaci údajů do AppUserService.
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
     * prostřednictvím DTO ChangePasswordDTO. Kontrola starého hesla
     * a uložení nového hesla se provádí v AppUserService.
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
     * Endpoint je dostupný pouze pro role ADMIN nebo MANAGER.
     * Metoda deleguje načtení seznamu uživatelů do AppUserService.
     *
     * @return seznam uživatelů jako AppUserDTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<AppUserDTO> getAllUsers() {
        return appUserService.getAllUsers();
    }

    /**
     * Vrací detail uživatele podle jeho ID.
     *
     * Endpoint je dostupný pouze pro role ADMIN nebo MANAGER.
     * Metoda deleguje načtení detailu uživatele do AppUserService.
     *
     * @param id ID uživatele
     * @return DTO s detaily vybraného uživatele
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public AppUserDTO getUserById(@PathVariable Long id) {
        return appUserService.getUserById(id);
    }

    /**
     * Resetuje heslo uživatele na výchozí hodnotu.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     * Vlastní reset hesla včetně nastavení nové hodnoty
     * se provádí v AppUserService.
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
     * Aktualizace stavu účtu se provádí v AppUserService.
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
     * Aktualizace stavu účtu se provádí v AppUserService.
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

    /**
     * Vrací historii vybraného uživatele podle jeho ID.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     * Metoda deleguje načtení historie do AppUserHistoryService.
     *
     * @param id ID uživatele
     * @return historie uživatele jako seznam AppUserHistoryDTO
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserHistoryDTO> getUserHistory(@PathVariable Long id) {
        return appUserHistoryService.getHistoryForUser(id);
    }

    /**
     * Vrací historii aktuálně přihlášeného uživatele.
     *
     * Uživatel se identifikuje pomocí e-mailu získaného z
     * autentizačního kontextu. Načtení historie se deleguje
     * do AppUserHistoryService.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return historie uživatele jako seznam AppUserHistoryDTO
     */
    @GetMapping("/me/history")
    @PreAuthorize("isAuthenticated()")
    public List<AppUserHistoryDTO> getMyUserHistory(
            Authentication authentication
    ) {

        String email = authentication.getName();
        return appUserHistoryService.getHistoryForUser(email);

    }

}