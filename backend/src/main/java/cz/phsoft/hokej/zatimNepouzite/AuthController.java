/*
package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.LoginRequest;
import cz.phsoft.hokej.models.dto.LoginResponse;
import cz.phsoft.hokej.models.services.UserService;
import cz.phsoft.hokej.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.getEmail(),
                        req.getPassword()
                )
        );

        String token = tokenProvider.generateToken(auth);

        return ResponseEntity.ok(new LoginResponse(req.getEmail(), token));
    }
}
*/