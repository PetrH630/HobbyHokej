package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.LoginRequest;
import cz.phsoft.hokej.models.dto.LoginResponse;
import cz.phsoft.hokej.models.dto.RegisterRequest;
import cz.phsoft.hokej.models.dto.RegisterResponse;
import cz.phsoft.hokej.models.dto.RegistrationDTO;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.services.AuthService;
import cz.phsoft.hokej.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import cz.phsoft.hokej.data.enums.PlayerType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider tokenProvider) {
        this.authService = authService;
        this.tokenProvider = tokenProvider;
    }

    // --- REGISTRACE ---
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegistrationDTO req) {
        // Vytvoření RegisterRequest přes parametrizovaný konstruktor
        RegisterRequest request = new RegisterRequest(
                req.getName(),
                req.getSurname(),
                req.getEmail(),
                req.getPhone(),
                req.getPassword(),
                PlayerType.BASIC // Změň podle potřeby na VIP/STANDARD
        );

        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
