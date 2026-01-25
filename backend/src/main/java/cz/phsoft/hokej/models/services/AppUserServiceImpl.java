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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service pro správu aplikačních uživatelských účtů.
 * <p>
 * Odpovědnosti:
 * <ul>
 *     <li>registrace nových uživatelů,</li>
 *     <li>aktivace účtů pomocí emailového ověřovacího tokenu,</li>
 *     <li>změna a reset hesla,</li>
 *     <li>správa základních údajů uživatelského účtu.</li>
 * </ul>
 *
 * Bezpečnost:
 * <ul>
 *     <li>hesla jsou vždy ukládána hashovaná pomocí BCrypt,</li>
 *     <li>nově registrovaný účet je neaktivní, dokud není ověřen email.</li>
 * </ul>
 *
 * Tato service neřeší:
 * <ul>
 *     <li>autentizaci (řeší Spring Security),</li>
 *     <li>správu hráčů (řeší {@link PlayerService}).</li>
 * </ul>
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserServiceImpl.class);

    /** Výchozí heslo při resetu účtu administrátorem */
    private static final String DEFAULT_RESET_PASSWORD = "Player123";

    /** Base URL aplikace – používá se pro generování aktivačních odkazů */
    @Value("${app.base-url}")
    private String baseUrl;

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;

    public AppUserServiceImpl(AppUserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder,
                              AppUserMapper appUserMapper,
                              EmailService emailService,
                              EmailVerificationTokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Zaregistruje nového uživatele.
     * <p>
     * Průběh registrace:
     * <ol>
     *     <li>ověření shody hesel,</li>
     *     <li>kontrola duplicity emailu,</li>
     *     <li>vytvoření neaktivního uživatele,</li>
     *     <li>vytvoření ověřovacího tokenu,</li>
     *     <li>odeslání aktivačního emailu.</li>
     * </ol>
     *
     * @param dto registrační údaje uživatele
     */
    @Override
    @Transactional
    public void register(RegisterUserDTO dto) {

        ensurePasswordsMatch(dto.getPassword(), dto.getPasswordConfirm(), null);
        ensureEmailNotUsed(dto.getEmail(), null);

        AppUserEntity user = createUserFromRegisterDto(dto);
        AppUserEntity savedUser = userRepository.save(user);

        EmailVerificationTokenEntity verificationToken =
                createVerificationToken(savedUser);

        sendActivationEmail(savedUser, verificationToken);
    }

    /**
     * Aktivuje uživatelský účet na základě ověřovacího tokenu.
     *
     * @param token aktivační token z emailu
     * @return {@code true} pokud byl účet úspěšně aktivován,
     *         {@code false} pokud je token neplatný nebo expirovaný
     */
    @Override
    @Transactional
    public boolean activateUser(String token) {

        EmailVerificationTokenEntity verificationToken =
                tokenRepository.findByToken(token).orElse(null);

        if (verificationToken == null ||
                verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        AppUserEntity user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);
        return true;
    }

    /**
     * Aktualizuje základní údaje přihlášeného uživatele.
     *
     * @param email email aktuálního uživatele
     * @param dto   aktualizovaná data účtu
     */
    @Override
    @Transactional
    public void updateUser(String email, AppUserDTO dto) {

        AppUserEntity user = findUserByEmailOrThrow(email);

        if (!user.getEmail().equals(dto.getEmail())) {
            ensureEmailNotUsed(dto.getEmail(), user.getId());
        }

        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());

        userRepository.save(user);
    }

    /**
     * Vrátí detail aktuálně přihlášeného uživatele.
     *
     * @param email email uživatele
     * @return DTO uživatele
     */
    @Override
    public AppUserDTO getCurrentUser(String email) {
        AppUserEntity user = findUserByEmailOrThrow(email);
        return appUserMapper.toDTO(user);
    }

    /**
     * Vrátí seznam všech uživatelů v systému.
     * <p>
     * Určeno pouze pro administrátora.
     *
     * @return seznam uživatelů
     */
    @Override
    public List<AppUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(appUserMapper::toDTO)
                .toList();
    }

    /**
     * Změní heslo přihlášeného uživatele.
     *
     * @param email              email uživatele
     * @param oldPassword        původní heslo
     * @param newPassword        nové heslo
     * @param newPasswordConfirm potvrzení nového hesla
     */
    @Override
    @Transactional
    public void changePassword(String email,
                               String oldPassword,
                               String newPassword,
                               String newPasswordConfirm) {

        ensurePasswordsMatch(
                newPassword,
                newPasswordConfirm,
                "BE - Nové heslo a potvrzení nového hesla se neshodují"
        );

        AppUserEntity user = findUserByEmailOrThrow(email);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidOldPasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Resetuje heslo uživatele na výchozí hodnotu.
     * <p>
     * Operace dostupná pouze administrátorovi.
     *
     * @param userId ID uživatele
     */
    @Override
    @Transactional
    public void resetPassword(Long userId) {
        AppUserEntity user = findUserByIdOrThrow(userId);
        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        userRepository.save(user);
    }

    // ==================================================
    // HELPER METODY
    // ==================================================

    private AppUserEntity findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private AppUserEntity findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Ověří shodu hesla a potvrzení hesla.
     */
    private void ensurePasswordsMatch(String password,
                                      String confirm,
                                      String customMessage) {

        if (password == null || confirm == null || !password.equals(confirm)) {
            if (customMessage == null) {
                throw new PasswordsDoNotMatchException();
            }
            throw new PasswordsDoNotMatchException(customMessage);
        }
    }

    /**
     * Ověří, že email není používán jiným uživatelem.
     *
     * @param email         nový email
     * @param currentUserId ID uživatele, který je ignorován (při update),
     *                      při registraci {@code null}
     */
    private void ensureEmailNotUsed(String email, Long currentUserId) {
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (currentUserId == null || !existing.getId().equals(currentUserId)) {
                throw new UserAlreadyExistsException(
                        "BE - Uživatel s tímto emailem již existuje"
                );
            }
        });
    }

    /**
     * Vytvoří nového uživatele z registračního DTO.
     */
    private AppUserEntity createUserFromRegisterDto(RegisterUserDTO dto) {
        AppUserEntity user = appUserMapper.fromRegisterDto(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_PLAYER);
        user.setEnabled(false);
        return user;
    }

    /**
     * Vytvoří a uloží emailový ověřovací token.
     */
    private EmailVerificationTokenEntity createVerificationToken(AppUserEntity user) {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return tokenRepository.save(token);
    }

    /**
     * Odešle aktivační email s ověřovacím odkazem.
     */
    private void sendActivationEmail(AppUserEntity user,
                                     EmailVerificationTokenEntity token) {

        String activationLink =
                baseUrl + "/api/auth/verify?token=" + token.getToken();

        log.info("Aktivační odkaz pro {}: {}", user.getEmail(), activationLink);
        emailService.sendActivationEmailHTML(user.getEmail(), activationLink);
    }
}
