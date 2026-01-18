package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.ChangePasswordDTO;
import cz.phsoft.hokej.models.services.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    // Přihlášený uživatel – bezpečné
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public AppUserDTO getCurrentUser(Authentication authentication) {
        return appUserService.getCurrentUser(authentication.getName());
    }

    // Změna hesla přihlášeného uživatele
    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 @RequestBody ChangePasswordDTO dto) {
        String email = authentication.getName();
        appUserService.changePassword(email, dto.getOldPassword(), dto.getNewPassword(), dto.getNewPasswordConfirm());
        return ResponseEntity.ok("Heslo úspěšně změněno");
    }

    // Seznam všech uživatelů – jen ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppUserDTO> getAllUsers() {
        return appUserService.getAllUsers();
    }


}
