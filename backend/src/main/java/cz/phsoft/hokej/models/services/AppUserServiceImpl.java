package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.EmailVerificationTokenEntity;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.EmailVerificationTokenRepository;
import cz.phsoft.hokej.exceptions.InvalidOldPasswordException;
import cz.phsoft.hokej.exceptions.PasswordsDoNotMatchException;
import cz.phsoft.hokej.exceptions.UserAlreadyExistsException;
import cz.phsoft.hokej.exceptions.UserNotFoundException;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import cz.phsoft.hokej.models.dto.mappers.AppUserMapper;
import cz.phsoft.hokej.models.services.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;

    public AppUserServiceImpl(AppUserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder, AppUserMapper appUserMapper,
                              EmailService emailService, EmailVerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void register(RegisterUserDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new PasswordsDoNotMatchException();
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Uživatel s tímto emailem již existuje");
        }

        AppUserEntity user = new AppUserEntity();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_PLAYER);
        user.setEnabled(false); // NEaktivní při registraci

        AppUserEntity savedUser = userRepository.save(user);

        // Generování tokenu
        String token = java.util.UUID.randomUUID().toString();
        EmailVerificationTokenEntity verificationToken = new EmailVerificationTokenEntity();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiresAt(java.time.LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);


        // Odeslání aktivačního emailu
        String activationLink = baseUrl + "/api/auth/verify?token=" + token;
        // Pro test lokálně: vypíše odkaz do konzole
        System.out.println("Aktivační odkaz: " + activationLink);

        emailService.sendActivationEmailHTML(savedUser.getEmail(), activationLink);
    }

    @Override
    public boolean activateUser(String token) {
        EmailVerificationTokenEntity verificationToken = tokenRepository.findByToken(token)
                .orElse(null);

        if (verificationToken == null || verificationToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            return false; // neplatný token nebo vypršel
        }

        AppUserEntity user = verificationToken.getUser();
        user.setEnabled(true); // aktivujeme uživatele
        userRepository.save(user);

        // Po aktivaci token smažeme (není potřeba jej uchovávat)
        tokenRepository.delete(verificationToken);

        return true;
    }

    @Override
    public void updateUser(String email, AppUserDTO dto) {

        AppUserEntity user = findUserByEmailOrThrow(email);

        // Nastavení nového hesla
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        userRepository.save(user);
    }

    @Override
    public AppUserDTO getCurrentUser(String email) {
        AppUserEntity user = findUserByEmailOrThrow(email);

        // ← využití mapperu
        return appUserMapper.toDTO(user);
    }

    @Override
    public List<AppUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(appUserMapper::toDTO)
                .toList();
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new PasswordsDoNotMatchException("Nov heslo a potvrzení nového hesla se neshodují");
        }

        AppUserEntity user = findUserByEmailOrThrow(email);

        // Ověření starého hesla
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOldPasswordException();
        }

        // Nastavení nového hesla
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // reset hesla
    @Override
    public void resetPassword(Long userId) {
        AppUserEntity user = findUserByIdOrThrow(userId);

        // Nastavení nového hesla na "Player123"
        user.setPassword(passwordEncoder.encode("Player123"));
        userRepository.save(user);
    }
    private AppUserEntity findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private AppUserEntity findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }


}