package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.LoginResponse;
import cz.phsoft.hokej.models.dto.RegisterRequest;
import cz.phsoft.hokej.models.dto.RegisterResponse;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public RegisterResponse register(RegisterRequest request) {
        if (playerRepository.findByEmail(request.getEmail()).isPresent()) {
            return new RegisterResponse("Email already exists");
        }

        PlayerEntity player = new PlayerEntity();
        player.setName(request.getName());
        player.setSurname(request.getSurname());
        player.setEmail(request.getEmail());
        player.setPhone(request.getPhone());
        player.setPlayerPassword(passwordEncoder.encode(request.getPassword()));
        player.setType(request.getType());  // VIP, STANDARD, BASIC
        player.setRole(Role.PLAYER);  // Role PLAYER
        player.setEnabled(false); // Čeká na schválení adminem

        playerRepository.save(player);
        return new RegisterResponse("Registration successful, waiting for admin approval");
    }

    public LoginResponse login(LoginRequest request) {
        PlayerEntity player = playerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), player.getPlayerPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = tokenProvider.generateToken(player.getEmail(), player.getRole().name(), player.getType().name());
        return new LoginResponse(token, player.getRole().name(), player.isEnabled(), player.getType().name());
    }
}
