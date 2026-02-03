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
 *
 * Zajišťuje se registrace, aktivace a deaktivace účtů,
 * změna a reset hesla a aktualizace základních údajů uživatele.
 * Třída se stará o bezpečné uložení hesel, práci s ověřovacími
 * a resetovacími tokeny a napojení na notifikační systém.
 *
 * Autentizace a autorizace se předpokládá v Spring Security,
 * nikoliv v této třídě.
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserServiceImpl.class);

    /**
     * Výchozí heslo při resetu účtu administrátorem.
     */
    private static final String DEFAULT_RESET_PASSWORD = "Player123";

    /**
     * Základní URL aplikace používaná pro generování odkazů
     * v aktivačních a resetovacích emailech.
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
     *
     * Provádí se kontrola shody hesel a jedinečnosti emailu.
     * Uživatel se vytvoří jako neaktivní, vygeneruje se ověřovací token
     * a odešle se notifikační zpráva s aktivačním odkazem.
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

        String activationLink = buildActivationLink(verificationToken);
        log.info("Aktivační odkaz pro {}: {}", user.getEmail(), activationLink);

        notificationService.notifyUser(
                savedUser,
                NotificationType.USER_CREATED,
                new UserActivationContext(savedUser, activationLink)
        );
    }

    /**
     * Aktivuje uživatelský účet na základě ověřovacího tokenu.
     *
     * Token se ověří, zkontroluje se jeho platnost a případně
     * se účet označí jako povolený. Pokud uživatel nemá nastavení,
     * vytvoří se pro něj výchozí konfigurace.
     *
     * @param token aktivační token
     * @return true při úspěšné aktivaci, jinak false
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

            if (user.getSettings() == null) {
                appUserSettingsService.createDefaultSettingsForUser(user);
            }
            userRepository.save(user);
        }

        tokenRepository.delete(verificationToken);

        if (newlyActivated) {
            notifyUser(user, NotificationType.USER_ACTIVATED);
        }
        return true;
    }

    /**
     * Aktivuje uživatelský účet v administraci.
     *
     * Kontroluje se, zda není účet již aktivní. Pokud nemá uživatel
     * nastavení, vytvoří se pro něj výchozí AppUserSettingsEntity.
     * Všechny aktivační tokeny se odstraní a odešle se notifikace
     * o úspěšné aktivaci.
     *
     * @param id ID uživatele
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

        if (!user.isEnabled()) {
            user.setEnabled(true);
            newlyActivated = true;

            if (user.getSettings() == null) {
                appUserSettingsService.createDefaultSettingsForUser(user);
            }

            userRepository.save(user);
        }

        tokenRepository.deleteByUser(user);

        if (newlyActivated) {
            notifyUser(user, NotificationType.USER_ACTIVATED);
        }

    }

    /**
     * Aktualizuje základní údaje uživatele podle emailu.
     *
     * Při změně emailu se ověřuje, že nový email není obsazen
     * jiným účtem. Po úspěšné aktualizaci se odešle notifikace.
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
     * Vrací detail aktuálně přihlášeného uživatele.
     *
     * @param email email uživatele
     * @return DTO reprezentace uživatele
     */
    @Override
    public AppUserDTO getCurrentUser(String email) {
        AppUserEntity user = findUserByEmailOrThrow(email);
        return appUserMapper.toDTO(user);
    }

    /**
     * Vrací všechny uživatele systému.
     *
     * Výsledek se mapuje na DTO a používá se v administraci
     * pro přehled a správu uživatelských účtů.
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
     * Změní heslo aktuálního uživatele.
     *
     * Ověří se shoda nového hesla a potvrzení, poté se zkontroluje
     * původní heslo pomocí BCrypt. Po úspěšné změně se odešle
     * notifikace o změně hesla.
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
     *
     * Používá se v administraci při ručním resetu hesla. Po změně
     * se odešle notifikace o resetu hesla.
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
     * Deaktivuje uživatelský účet v administraci.
     *
     * Pokud je účet již deaktivovaný, vyhodí se výjimka.
     * Po deaktivaci se odešle notifikace.
     *
     * @param id ID uživatele
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

    /**
     * Vrací uživatele podle ID ve formě DTO.
     *
     * Používá se v administraci při zobrazení detailu účtu.
     *
     * @param id ID uživatele
     * @return DTO reprezentace uživatele
     */
    public AppUserDTO getUserById(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);
        return appUserMapper.toDTO(user);
    }

    /**
     * Vytvoří požadavek na reset zapomenutého hesla.
     *
     * Případné staré tokeny se odstraní a vygeneruje se nový
     * resetovací token. Navenek se neprozrazuje, zda email
     * v systému existuje, kvůli bezpečnosti.
     *
     * @param email email uživatele
     */
    @Override
    @Transactional
    public void requestForgottenPasswordReset(String email) {

        AppUserEntity user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            log.info("Požadavek na forgotten password reset pro neexistující email: {}", email);
            return;
        }

        forgottenPasswordResetTokenRepository.deleteByUser(user);

        ForgottenPasswordResetTokenEntity forgottenPasswordToken = createResetPasswordToken(user);

        String resetPasswordlink = buildResetPasswordlink(forgottenPasswordToken);

        log.info("Odkaz pro reset hesla {}: {}", user.getEmail(), resetPasswordlink);

        notifyUser(
                user,
                NotificationType.FORGOTTEN_PASSWORD_RESET_REQUEST,
                new ForgottenPasswordResetContext(user, resetPasswordlink)
        );
    }

    /**
     * Vrací email uživatele svázaný s daným resetovacím tokenem.
     *
     * Ověřuje se platnost a neexpirovanost tokenu. Metoda se používá
     * při načítání formuláře pro zadání nového hesla.
     *
     * @param token resetovací token
     * @return email uživatele
     */
    @Override
    @Transactional
    public String getForgottenPasswordResetEmail(String token) {

        ForgottenPasswordResetTokenEntity resetToken =
                forgottenPasswordResetTokenRepository.findByToken(token)
                        .orElseThrow(InvalidResetTokenException::new);

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("BE - Reset token expiroval.");
        }

        return resetToken.getUser().getEmail();
    }

    /**
     * Nastaví nové heslo na základě resetovacího tokenu.
     *
     * Provádí se kontrola shody nového hesla a jeho potvrzení,
     * ověření platnosti tokenu a následné uložení nového hesla
     * v zahashované podobě. Token se poté odstraní
     * a odešle se notifikace o dokončení resetu hesla.
     *
     * @param dto data pro reset zapomenutého hesla
     */
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
                        .orElseThrow(InvalidResetTokenException::new);

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidResetTokenException("BE - Reset token expiroval.");
        }

        AppUserEntity user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        forgottenPasswordResetTokenRepository.delete(resetToken);

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
     * Ověří shodu hesla a jeho potvrzení.
     *
     * Pokud se hodnoty neshodují, vyhodí se výjimka
     * PasswordsDoNotMatchException s případnou vlastním textem.
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
     * @param currentUserId ID aktuálního uživatele nebo null při registraci
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
     *
     * Uživatel se nastaví jako neaktivní s rolí hráče.
     *
     * @param dto registrační data
     * @return nová entita uživatele
     */
    private AppUserEntity createUserFromRegisterDto(RegisterUserDTO dto) {
        AppUserEntity user = appUserMapper.fromRegisterDto(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_PLAYER);
        user.setEnabled(false);
        return user;
    }

    /**
     * Vytvoří a uloží aktivační token pro uživatele.
     *
     * Token je platný omezenou dobu a používá se
     * při aktivaci účtu přes email.
     *
     * @param user uživatel, pro kterého se token vytváří
     * @return uložený aktivační token
     */
    private EmailVerificationTokenEntity createVerificationToken(AppUserEntity user) {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return tokenRepository.save(token);
    }

    /**
     * Vytvoří a uloží resetovací token pro zapomenuté heslo.
     *
     * Token se používá v procesu resetu hesla a má omezenou platnost.
     *
     * @param user uživatel, pro kterého se token vytváří
     * @return uložený resetovací token
     */
    private ForgottenPasswordResetTokenEntity createResetPasswordToken(AppUserEntity user) {
        ForgottenPasswordResetTokenEntity token = new ForgottenPasswordResetTokenEntity();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        return forgottenPasswordResetTokenRepository.save(token);
    }

    /**
     * Odesílá notifikaci uživateli bez kontextu.
     */
    private void notifyUser(AppUserEntity user, NotificationType type) {
        notificationService.notifyUser(user, type, null);
    }

    /**
     * Odesílá notifikaci uživateli s volitelným kontextem.
     */
    private void notifyUser(AppUserEntity user, NotificationType type, Object context) {
        notificationService.notifyUser(user, type, context);
    }
}
