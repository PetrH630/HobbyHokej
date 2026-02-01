package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.EmailVerificationTokenEntity;
import cz.phsoft.hokej.data.entities.ForgottenPasswordResetTokenEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.EmailVerificationTokenRepository;
import cz.phsoft.hokej.data.repositories.ForgottenPasswordResetTokenRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.ForgottenPasswordResetDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import cz.phsoft.hokej.models.mappers.AppUserMapper;
import cz.phsoft.hokej.models.services.email.EmailService;
import cz.phsoft.hokej.models.services.notification.ForgottenPasswordResetContext;
import cz.phsoft.hokej.models.services.notification.NotificationService;
import cz.phsoft.hokej.models.services.notification.UserActivationContext;
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
 * <p>
 * Bezpečnost:
 * <ul>
 *     <li>hesla jsou vždy ukládána hashovaná pomocí BCrypt,</li>
 *     <li>nově registrovaný účet je neaktivní, dokud není ověřen email.</li>
 * </ul>
 * <p>
 * Tato service neřeší:
 * <ul>
 *     <li>autentizaci (řeší Spring Security),</li>
 *     <li>správu hráčů (řeší {@link PlayerService}).</li>
 * </ul>
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserServiceImpl.class);

    /**
     * Výchozí heslo při resetu účtu administrátorem
     */
    private static final String DEFAULT_RESET_PASSWORD = "Player123";

    /**
     * Base URL aplikace – používá se pro generování aktivačních odkazů
     */
    @Value("${app.base-url}")
    private String baseUrl;

    private String buildActivationLink(EmailVerificationTokenEntity token) {
        return baseUrl + "/api/auth/verify?token=" + token.getToken();
    }

    private String buildResetPasswordlink(ForgottenPasswordResetTokenEntity token) {
        return baseUrl + "/api/auth/reset-password?token=" + token.getToken();
    }

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final AppUserSettingsService appUserSettingsService;
    private final NotificationService notificationService;
    private final ForgottenPasswordResetTokenRepository forgottenPasswordResetTokenRepository;

    public AppUserServiceImpl(AppUserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder,
                              AppUserMapper appUserMapper,
                              EmailService emailService,
                              EmailVerificationTokenRepository tokenRepository,
                              AppUserSettingsService appUserSettingsService,
                              NotificationService notificationService,
                              ForgottenPasswordResetTokenRepository forgottenPasswordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.appUserSettingsService = appUserSettingsService;
        this.notificationService = notificationService;
        this.forgottenPasswordResetTokenRepository = forgottenPasswordResetTokenRepository;
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

        // ⬅ TADY ZÍSKÁŠ activationLink
        String activationLink = buildActivationLink(verificationToken);
        log.info("Aktivační odkaz pro {}: {}", user.getEmail(), activationLink);
        // 1) Pošleme aktivační email přes EmailService (stávající logika)
//        emailService.sendActivationEmail(
//                savedUser.getEmail(),
//                savedUser.getName(),         // nebo full name, jak to máš
//                activationLink
//        );

        // 2) Pošleme notifikaci přes NotificationService (user + manažeři)
        notificationService.notifyUser(
                savedUser,
                NotificationType.USER_CREATED,
                new UserActivationContext(savedUser, activationLink)
        );
    }

    /**
     * Aktivuje uživatelský účet na základě ověřovacího tokenu.
     *
     * @param token aktivační token z emailu
     * @return {@code true} pokud byl účet úspěšně aktivován,
     * {@code false} pokud je token neplatný nebo expirovaný
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
        boolean newlyActivated = false;

        if (!user.isEnabled()) {
            user.setEnabled(true);
            newlyActivated = true;

            // pokud user ještě nemá settings → vytvoř default
            if (user.getSettings() == null) {
                appUserSettingsService.createDefaultSettingsForUser(user);
            }
            userRepository.save(user);
        }
        // token vždy smažeme, pokud byl platný
        tokenRepository.delete(verificationToken);
        // pokud se opravdu podaří nově aktivoval, pošleme notifikaci USER_ACTIVATED
        if (newlyActivated) {
            notifyUser(user, NotificationType.USER_ACTIVATED);
        }
        return true;
    }

    /**
     * Aktivuje uživatelský účet na základě aktivace Administrátorem.
     */
    @Override
    public void activateUserByAdmin(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);
        if (user.isEnabled()) {
            throw new InvalidUserActivationException(
                    "BE - Aktivace účtu již byla provedena"
            );
        }
        boolean newlyActivated = false;

        // aktivace účtu
        if (!user.isEnabled()) {
            user.setEnabled(true);
            newlyActivated = true;

            // pokud user ještě nemá settings → vytvoř default
            if (user.getSettings() == null) {
                appUserSettingsService.createDefaultSettingsForUser(user);
            }

            userRepository.save(user);
        }

        // smazání všech tokenu uživatele
        tokenRepository.deleteByUser(user);
        // případně: tokenRepository.deleteByUserId(user.getId());

        // pokud se opravdu nově aktivoval, pošleme notifikaci USER_ACTIVATED
        // pokud se opravdu nově aktivoval, pošleme notifikaci USER_ACTIVATED
        if (newlyActivated) {
            notifyUser(user, NotificationType.USER_ACTIVATED);
        }

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
        notifyUser(user, NotificationType.USER_UPDATED);
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

        notifyUser(user, NotificationType.USER_CHANGE_PASSWORD);
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

        notifyUser(user, NotificationType.PASSWORD_RESET);
    }

    /**
     * Deaktivuj uživatelský účet na základě deaktivace Administrátorem.
     */
    @Override
    public void deactivateUserByAdmin(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);

        if (!user.isEnabled()) {
            throw new InvalidUserActivationException(
                    "BE - Deaktivace účtu již byla provedena"
            );
        }
        user.setEnabled(false);
        userRepository.save(user);
        notifyUser(user, NotificationType.USER_DEACTIVATED);

    }

    public AppUserDTO getUserById(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);
        return appUserMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void requestForgottenPasswordReset(String email) {

        AppUserEntity user = userRepository.findByEmail(email)
                .orElse(null);

        // Bezpečnost: i když user neexistuje, neřekneme to klientovi.
        if (user == null) {
            log.info("Požadavek na forgotten password reset pro neexistující email: {}", email);
            return;
        }

        // Smažeme staré reset tokeny
        forgottenPasswordResetTokenRepository.deleteByUser(user);

        // Vytvoříme nový reset token
        ForgottenPasswordResetTokenEntity forgottenPasswordToken = createResetPasswordToken(user);

//        ForgottenPasswordResetTokenEntity token = new ForgottenPasswordResetTokenEntity();
//        token.setToken(UUID.randomUUID().toString());
//        token.setUser(user);
//        token.setExpiresAt(LocalDateTime.now().plusHours(1));
//
//        ForgottenPasswordResetTokenEntity savedToken =
//                forgottenPasswordResetTokenRepository.save(token);

        // ⬅ TADY ZÍSKÁŠ activationLink
        String resetPasswordlink = buildResetPasswordlink(forgottenPasswordToken);

        log.info("Odkaz pro reset hesla {}: {}", user.getEmail(), resetPasswordlink);


        notifyUser(
                user,
                NotificationType.FORGOTTEN_PASSWORD_RESET_REQUEST,
                new ForgottenPasswordResetContext(user, resetPasswordlink)
        );
    }

    @Override
    @Transactional
    public String getForgottenPasswordResetEmail(String token) {

        ForgottenPasswordResetTokenEntity resetToken =
                forgottenPasswordResetTokenRepository.findByToken(token)
                        .orElseThrow(() -> new InvalidResetTokenException());

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("BE - Reset token expiroval.");
        }

        return resetToken.getUser().getEmail();
    }

    @Override
    @Transactional
    public void forgottenPasswordReset(ForgottenPasswordResetDTO dto) {

        ensurePasswordsMatch(
                dto.getNewPassword(),
                dto.getNewPasswordConfirm(),
                "BE - Nové heslo a potvrzení nového hesla se neshodují"
        );

        ForgottenPasswordResetTokenEntity resetToken =
                forgottenPasswordResetTokenRepository.findByToken(dto.getToken())
                        .orElseThrow(() -> new InvalidResetTokenException());

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("BE - Reset token expiroval.");
        }

        AppUserEntity user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        // token zneplatníme – smažeme
        forgottenPasswordResetTokenRepository.delete(resetToken);

        // Notifikace – heslo změněno (můžeš použít buď USER_CHANGE_PASSWORD, nebo speciální FORGOTTEN_PASSWORD_RESET_COMPLETED)
        //notifyUser(user, NotificationType.USER_CHANGE_PASSWORD);
        // nebo:
        notifyUser(user, NotificationType.FORGOTTEN_PASSWORD_RESET_COMPLETED);
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
     * Vytvoří a uloží emailový reset password token.
     */
    private ForgottenPasswordResetTokenEntity createResetPasswordToken(AppUserEntity user) {
        ForgottenPasswordResetTokenEntity token = new ForgottenPasswordResetTokenEntity();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        return forgottenPasswordResetTokenRepository.save(token);
    }


    // metody pro notifikaci
    private void notifyUser(AppUserEntity user, NotificationType type) {
        notificationService.notifyUser(user, type, null);
    }

    private void notifyUser(AppUserEntity user, NotificationType type, Object context) {
        notificationService.notifyUser(user, type, context);
    }
}

