package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.EmailVerificationTokenEntity;
import cz.phsoft.hokej.data.repositories.EmailVerificationTokenRepository;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import cz.phsoft.hokej.models.services.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AppUserService appUserService;
    private final AppUserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;

    public AuthController(AppUserService appUserService,
                          AppUserRepository userRepository,
                          EmailVerificationTokenRepository tokenRepository) {
        this.appUserService = appUserService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
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
        Optional<EmailVerificationTokenEntity> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Neplatný aktivační odkaz.");
        }

        EmailVerificationTokenEntity verificationToken = optionalToken.get();

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Aktivační odkaz vypršel.");
        }

        AppUserEntity user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);

        return ResponseEntity.ok("Účet byl úspěšně aktivován.");
    }
}
