package cz.phsoft.hokej.user.services;

import cz.phsoft.hokej.demo.DemoModeOperationNotAllowedException;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.entities.EmailVerificationTokenEntity;
import cz.phsoft.hokej.user.entities.ForgottenPasswordResetTokenEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.user.enums.Role;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import cz.phsoft.hokej.user.repositories.EmailVerificationTokenRepository;
import cz.phsoft.hokej.user.repositories.ForgottenPasswordResetTokenRepository;
import cz.phsoft.hokej.user.dto.AppUserDTO;
import cz.phsoft.hokej.user.dto.ForgottenPasswordResetDTO;
import cz.phsoft.hokej.user.dto.RegisterUserDTO;
import cz.phsoft.hokej.user.mappers.AppUserMapper;
import cz.phsoft.hokej.notifications.email.EmailService;
import cz.phsoft.hokej.notifications.services.ForgottenPasswordResetContext;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.notifications.services.UserActivationContext;
import cz.phsoft.hokej.demo.DemoModeGuard;
import cz.phsoft.hokej.user.exceptions.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import java.time.Clock;
import java.time.Instant;

/**
 * Implementace servisní vrstvy pro správu aplikačních uživatelských účtů.
 *
 * Zajišťuje registraci uživatele, aktivaci a deaktivaci účtu,
 * změnu a reset hesla a aktualizaci základních údajů.
 * Součástí odpovědnosti je bezpečné uložení hesel pomocí BCrypt
 * a správa ověřovacích a resetovacích tokenů.
 *
 * Notifikace o událostech jsou odesílány prostřednictvím NotificationService.
 * Autentizace a autorizace jsou řešeny v rámci Spring Security
 * a nejsou součástí odpovědnosti této třídy.
 */
@Service
public class AppUserServiceImpl implements AppUserService {

    @Value("${app.frontend-base-url}")
    private String frontendBasUrl;
    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    private static final Logger log = LoggerFactory.getLogger(AppUserServiceImpl.class);

    /**
     * Výchozí heslo používané při resetu účtu administrátorem.
     */
    private static final String DEFAULT_RESET_PASSWORD = "Player123";

    /**
     * Základní URL aplikace používaná pro sestavení odkazů
     * v aktivačních a resetovacích notifikacích.
     */
    @Value("${app.base-url}")
    private String baseUrl;

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository tokenRepository;
    private final AppUserSettingsService appUserSettingsService;
    private final NotificationService notificationService;
    private final ForgottenPasswordResetTokenRepository forgottenPasswordResetTokenRepository;
    private final DemoModeGuard demoModeGuard;
    private final Clock clock;

    /**
     * Vytvoří instanci servisní třídy.
     *
     * Závislosti jsou předány pomocí konstruktoru a jsou používány
     * pro práci s databází, mapování dat a odesílání notifikací.
     *
     * @param userRepository repozitář pro práci s uživatelskými účty
     * @param passwordEncoder encoder pro hashování a ověření hesel
     * @param appUserMapper mapper pro převod mezi entitami a DTO
     * @param emailService servis pro odesílání e-mailů
     * @param tokenRepository repozitář pro ověřovací tokeny
     * @param appUserSettingsService servis pro správu uživatelských nastavení
     * @param notificationService servis pro odesílání notifikací
     * @param forgottenPasswordResetTokenRepository repozitář pro resetovací tokeny
     */
    public AppUserServiceImpl(AppUserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              AppUserMapper appUserMapper,
                              EmailService emailService,
                              EmailVerificationTokenRepository tokenRepository,
                              AppUserSettingsService appUserSettingsService,
                              NotificationService notificationService,
                              ForgottenPasswordResetTokenRepository forgottenPasswordResetTokenRepository,
                              DemoModeGuard demoModeGuard,
                              Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
        this.emailService = emailService;
        this.tokenRepository = tokenRepository;
        this.appUserSettingsService = appUserSettingsService;
        this.notificationService = notificationService;
        this.forgottenPasswordResetTokenRepository = forgottenPasswordResetTokenRepository;
        this.demoModeGuard = demoModeGuard;
        this.clock = clock;
    }

    /**
     * Zaregistruje nového uživatele.
     *
     * Před vytvořením účtu se ověřuje shoda zadaných hesel
     * a jedinečnost e-mailové adresy. Účet je vytvořen jako neaktivní,
     * je vygenerován ověřovací token a je odeslána notifikace
     * obsahující aktivační odkaz.
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
     * Token je vyhledán a je ověřena jeho platnost. Pokud je token neplatný
     * nebo expirovaný, je vrácena hodnota false. Při úspěšné aktivaci
     * je účet povolen a v případě chybějící konfigurace jsou vytvořena
     * výchozí uživatelská nastavení. Použitý token je odstraněn.
     *
     * Po úspěšné aktivaci je odeslána notifikace o aktivaci účtu.
     *
     * @param token aktivační token
     * @return true, pokud byla aktivace provedena, jinak false
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
     * Ověřuje se, zda se nejedná o administrátorský účet a zda účet již není aktivní.
     * Při úspěšné aktivaci jsou případná chybějící uživatelská nastavení vytvořena
     * a všechny ověřovací tokeny uživatele jsou odstraněny.
     *
     * Po úspěšné aktivaci je odeslána notifikace o aktivaci účtu.
     *
     * @param id identifikátor uživatele
     */
    @Override
    public void activateUserByAdmin(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);

        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new InvalidAdminActivateDeactivateException(
                    "BE - Administrátorský účet nelze deaktivovat"
            );
        }

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
     * Aktualizuje základní údaje uživatele podle e-mailové adresy.
     *
     * Při změně e-mailu se ověřuje, že nová e-mailová adresa není používána
     * jiným účtem. V demo režimu je operace před zápisem do databáze zakázána.
     * Po úspěšné aktualizaci je odeslána notifikace o změně údajů.
     *
     * @param email e-mailová adresa aktuálně přihlášeného uživatele
     * @param dto aktualizovaná data účtu
     */
    @Override
    @Transactional
    public void updateUser(String email, AppUserDTO dto) {
        AppUserEntity user = findUserByEmailOrThrow(email);

        if (!user.getEmail().equals(dto.getEmail())) {
            ensureEmailNotUsed(dto.getEmail(), user.getId());
        }

        demoModeGuard.write(
                user.getId(),
                "Uživatel, který byl vytvořen aplikací, nebude změněn. " +
                        "Aplikace běží v DEMO režimu. Změny budou skutečně provedeny " +
                        "pouze u vámi vytvořených uživatelů."
        );

        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());

        userRepository.save(user);
        notifyUser(user, NotificationType.USER_UPDATED);
    }

    /**
     * Vrátí detail aktuálně přihlášeného uživatele.
     *
     * Uživatel je vyhledán podle e-mailové adresy a je převeden na DTO.
     *
     * @param email e-mailová adresa uživatele
     * @return DTO reprezentace uživatele
     */
    @Override
    public AppUserDTO getCurrentUser(String email) {
        AppUserEntity user = findUserByEmailOrThrow(email);
        return appUserMapper.toDTO(user);
    }

    /**
     * Vrátí seznam všech uživatelů systému.
     *
     * Záznamy jsou načteny z databáze a jsou mapovány na DTO.
     * Metoda se používá v administraci pro přehled a správu účtů.
     *
     * @return seznam uživatelů ve formě DTO
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
     * Ověřuje se shoda nového hesla a jeho potvrzení a následně se ověřuje
     * původní heslo. V demo režimu je operace před zápisem do databáze zakázána.
     * Po úspěšné změně hesla je odeslána notifikace.
     *
     * @param email e-mailová adresa uživatele
     * @param oldPassword původní heslo
     * @param newPassword nové heslo
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

        demoModeGuard.write(
                user.getId(),
                "Heslo u uživatele, který byl vytvořen aplikací, nebude změněno. " +
                        "Aplikace běží v DEMO režimu. Změna hesla bude skutečně provedena " +
                        "pouze u vámi vytvořených uživatelů."
        );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        notifyUser(user, NotificationType.USER_CHANGE_PASSWORD);
    }

    /**
     * Resetuje heslo uživatele na výchozí hodnotu.
     *
     * Metoda se používá v administraci při ručním resetu hesla.
     * V demo režimu je operace před zápisem do databáze zakázána.
     * Po úspěšném resetu je odeslána notifikace.
     *
     * @param userId identifikátor uživatele
     */
    @Override
    @Transactional
    public void resetPassword(Long userId) {
        AppUserEntity user = findUserByIdOrThrow(userId);

        if (isDemoMode) {
            throw new DemoModeOperationNotAllowedException(
                    "Heslo nebude resetováno. Aplikace běží v DEMO režimu."
            );
        }

        user.setPassword(passwordEncoder.encode(DEFAULT_RESET_PASSWORD));
        userRepository.save(user);

        notifyUser(user, NotificationType.PASSWORD_RESET);
    }

    /**
     * Deaktivuje uživatelský účet v administraci.
     *
     * Ověřuje se, zda se nejedná o administrátorský účet a zda účet již není deaktivovaný.
     * Po úspěšné deaktivaci je odeslána notifikace o deaktivaci účtu.
     *
     * @param id identifikátor uživatele
     */
    @Override
    public void deactivateUserByAdmin(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);

        if (user.getRole() == Role.ROLE_ADMIN) {
            throw new InvalidAdminActivateDeactivateException(
                    "BE - Administrátorský účet nelze deaktivovat"
            );
        }

        if (!user.isEnabled()) {
            throw new InvalidUserActivationException(
                    "BE - Deaktivace účtu již byla provedena"
            );
        }

        demoModeGuard.write(
                user.getId(),
                "Uživatel, který byl vytvořen aplikací, nebude deaktivován. " +
                        "Aplikace běží v DEMO režimu. Deaktivace bude skutečně provedena " +
                        "pouze u vámi vytvořených uživatelů."
        );

        user.setEnabled(false);
        userRepository.save(user);

        notifyUser(user, NotificationType.USER_DEACTIVATED);
    }


    /**
     * Vrátí uživatele podle identifikátoru ve formě DTO.
     *
     * Metoda se používá v administraci při zobrazení detailu účtu.
     *
     * @param id identifikátor uživatele
     * @return DTO reprezentace uživatele
     */
    public AppUserDTO getUserById(Long id) {
        AppUserEntity user = findUserByIdOrThrow(id);
        return appUserMapper.toDTO(user);
    }

    /**
     * Vytvoří požadavek na reset zapomenutého hesla.
     *
     * Pokud uživatel pro daný e-mail neexistuje, není vyhozena chyba
     * a metoda se ukončí, aby nebylo možné odvodit existenci účtu.
     * Před vygenerováním nového tokenu jsou odstraněny případné staré tokeny.
     * Následně je odeslána notifikace obsahující odkaz pro nastavení nového hesla.
     *
     * @param email e-mailová adresa uživatele
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
     * Vrátí e-mailovou adresu uživatele svázanou se zadaným resetovacím tokenem.
     *
     * Token je vyhledán a je ověřena jeho platnost. Metoda se používá
     * při načítání formuláře pro zadání nového hesla.
     *
     * @param token resetovací token
     * @return e-mailová adresa uživatele
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
     * Ověřuje se shoda nového hesla a jeho potvrzení a platnost tokenu.
     * Nové heslo je uloženo v zahashované podobě a použitý token je odstraněn.
     * Po úspěšném dokončení je odeslána notifikace o dokončeném resetu hesla.
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


        demoModeGuard.writeWithFinalize(
                user.getId(),
                "Heslo u uživatele, který byl vytvořen aplikací, nebude ve skutečnosti resetováno. " +
                        "Aplikace běží v DEMO režimu. Reset a změna zapomenutého hesla bude skutečně provedena " +
                        "pouze u vámi vytvořených uživatelů.",
                () -> {
                    user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
                    userRepository.save(user);
                    forgottenPasswordResetTokenRepository.delete(resetToken);
                    notifyUser(user, NotificationType.FORGOTTEN_PASSWORD_RESET_COMPLETED);
                },
                () -> {
                    forgottenPasswordResetTokenRepository.delete(resetToken);
                    notifyUser(user, NotificationType.FORGOTTEN_PASSWORD_RESET_COMPLETED);
                }
        );
    }

    /**
     * Aktualizuje časová razítka přihlášení uživatele.
     *
     * Při úspěšném přihlášení se původní hodnota currentLoginAt
     * uloží do lastLoginAt a currentLoginAt se nastaví na aktuální čas.
     * Metoda se používá v bezpečnostní vrstvě po úspěšné autentizaci.
     *
     * @param email e-mailová adresa přihlášeného uživatele
     */
    @Transactional
    public void onSuccessfulLogin(String email) {
        AppUserEntity user = findUserByEmailOrThrow(email);

        Instant now = Instant.now(clock);

        user.setLastLoginAt(user.getCurrentLoginAt());
        user.setCurrentLoginAt(now);

        userRepository.save(user);

        log.info("Aktualizována přihlášení uživatele {}: lastLoginAt={}, currentLoginAt={}",
                email, user.getLastLoginAt(), user.getCurrentLoginAt());
    }

    // ==================================================
    // HELPER METODY
    // ==================================================

    private String buildActivationLink(EmailVerificationTokenEntity token) {
        return frontendBasUrl + "/verify?token=" + token.getToken();
    }

    private String buildResetPasswordlink(ForgottenPasswordResetTokenEntity token) {
        return baseUrl + "/api/auth/reset-password?token=" + token.getToken();
    }

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
     * Pokud se hodnoty neshodují, je vyhozena výjimka PasswordsDoNotMatchException.
     * Pokud je předán vlastní text, je použit jako chybová zpráva výjimky.
     *
     * @param password heslo
     * @param confirm potvrzení hesla
     * @param customMessage volitelná vlastní chybová zpráva
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
     * Ověří, že e-mailová adresa není používána jiným uživatelem.
     *
     * Při registraci je currentUserId null. Při aktualizaci účtu se kontroluje,
     * zda případně nalezený uživatel není totožný s aktualizovaným účtem.
     *
     * @param email e-mailová adresa určená ke kontrole
     * @param currentUserId identifikátor aktuálního uživatele nebo null při registraci
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
     * Vytvoří nového uživatele na základě registračního DTO.
     *
     * Heslo je uloženo v zahashované podobě. Účet je vytvořen jako neaktivní
     * a je mu nastavena výchozí role ROLE_PLAYER.
     *
     * @param dto registrační data
     * @return nová entita uživatele připravená k uložení
     */
    private AppUserEntity createUserFromRegisterDto(RegisterUserDTO dto) {
        AppUserEntity user = appUserMapper.fromRegisterDto(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_PLAYER);
        user.setEnabled(false);
        return user;
    }

    /**
     * Vytvoří a uloží ověřovací token pro aktivaci účtu.
     *
     * Token má omezenou platnost a je používán při aktivaci účtu přes odkaz.
     *
     * @param user uživatel, pro kterého se token vytváří
     * @return uložený ověřovací token
     */
    private EmailVerificationTokenEntity createVerificationToken(AppUserEntity user) {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        return tokenRepository.save(token);
    }

    /**
     * Vytvoří a uloží resetovací token pro proces resetu zapomenutého hesla.
     *
     * Token má omezenou platnost a je používán pro autorizaci nastavení nového hesla.
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
     * Odešle notifikaci uživateli bez kontextu.
     *
     * Volání je delegováno do NotificationService.
     *
     * @param user uživatel, kterému se notifikace odesílá
     * @param type typ notifikace
     */
    private void notifyUser(AppUserEntity user, NotificationType type) {
        notificationService.notifyUser(user, type, null);
    }

    /**
     * Odešle notifikaci uživateli s volitelným kontextem.
     *
     * Volání je delegováno do NotificationService.
     *
     * @param user uživatel, kterému se notifikace odesílá
     * @param type typ notifikace
     * @param context kontextová data pro šablonu notifikace
     */
    private void notifyUser(AppUserEntity user, NotificationType type, Object context) {
        notificationService.notifyUser(user, type, context);
    }
}
