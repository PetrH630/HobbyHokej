package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.data.repositories.PlayerSettingsRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.exceptions.UserNotFoundException;
import cz.phsoft.hokej.models.dto.SpecialNotificationTargetDTO;
import cz.phsoft.hokej.models.dto.requests.SpecialNotificationRequestDTO;
import cz.phsoft.hokej.models.services.email.EmailMessageBuilder;
import cz.phsoft.hokej.models.services.email.EmailService;
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;
import cz.phsoft.hokej.models.services.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implementace služby pro odesílání speciálních zpráv.
 *
 * Služba kombinuje:
 * - uložení in-app notifikací (InAppNotificationService),
 * - odeslání emailů (EmailService),
 * - odeslání SMS (SmsService).
 *
 * Všechny akce se provádějí bez ohledu na uživatelská
 * notifikační nastavení. Selhání emailu nebo SMS
 * neblokuje uložení in-app notifikace.
 *
 * V DEMO režimu se e-maily a SMS fyzicky neodesílají.
 * Místo toho se ukládají do DemoNotificationStore a
 * vrací se na frontend přes demo endpoint.
 */
@Service
public class SpecialNotificationServiceImpl implements SpecialNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SpecialNotificationServiceImpl.class);

    private final AppUserRepository appUserRepository;
    private final PlayerRepository playerRepository;
    private final PlayerSettingsRepository playerSettingsRepository;
    private final InAppNotificationService inAppNotificationService;
    private final EmailService emailService;
    private final SmsService smsService;
    private final EmailMessageBuilder emailMessageBuilder;
    private final SmsMessageBuilder smsMessageBuilder;

    // DEMO režim
    private final DemoModeService demoModeService;
    private final DemoNotificationStore demoNotificationStore;

    public SpecialNotificationServiceImpl(AppUserRepository appUserRepository,
                                          PlayerRepository playerRepository,
                                          PlayerSettingsRepository playerSettingsRepository,
                                          InAppNotificationService inAppNotificationService,
                                          EmailService emailService,
                                          SmsService smsService,
                                          EmailMessageBuilder emailMessageBuilder,
                                          SmsMessageBuilder smsMessageBuilder,
                                          DemoModeService demoModeService,
                                          DemoNotificationStore demoNotificationStore) {
        this.appUserRepository = appUserRepository;
        this.playerRepository = playerRepository;
        this.playerSettingsRepository = playerSettingsRepository;
        this.inAppNotificationService = inAppNotificationService;
        this.emailService = emailService;
        this.smsService = smsService;
        this.emailMessageBuilder = emailMessageBuilder;
        this.smsMessageBuilder = smsMessageBuilder;
        this.demoModeService = demoModeService;
        this.demoNotificationStore = demoNotificationStore;
    }

    @Override
    public void sendSpecialNotification(SpecialNotificationRequestDTO request) {
        Objects.requireNonNull(request, "request must not be null");

        if (request.getTargets() == null || request.getTargets().isEmpty()) {
            log.debug("SpecialNotificationService.sendSpecialNotification: prázdný seznam příjemců, nic se neprovádí");
            return;
        }

        request.getTargets().forEach(target -> {
            AppUserEntity user = appUserRepository.findById(target.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(target.getUserId()));

            PlayerEntity player = null;
            PlayerSettingsEntity playerSettings = null;

            if (target.getPlayerId() != null) {
                player = playerRepository.findById(target.getPlayerId())
                        .orElseThrow(() -> new PlayerNotFoundException(target.getPlayerId()));

                playerSettings = playerSettingsRepository.findByPlayer(player)
                        .orElse(null);
            }

            // 1) IN-APP notifikace
            inAppNotificationService.storeSpecialMessage(
                    user,
                    player,
                    request.getTitle(),
                    request.getMessage()
            );

            // 2) EMAIL (pokud je povoleno a existuje nějaký email podle pravidel resolveEmail)
            if (request.isSendEmail()) {
                String to = resolveEmail(user, playerSettings);
                if (to != null && !to.isBlank()) {
                    sendSpecialEmail(to, user, player, request);
                } else {
                    log.debug(
                            "SpecialNotificationService.sendSpecialNotification: není k dispozici email pro playerId={} (userId={})",
                            player != null ? player.getId() : null,
                            user.getId()
                    );
                }
            }

            // 3) SMS (pokud je povoleno a máme telefon v PlayerSettings)
            if (request.isSendSms() && playerSettings != null) {
                String phone = resolvePhoneNumber(playerSettings);
                if (phone != null && !phone.isBlank()) {
                    sendSpecialSms(phone, player, request);
                } else {
                    log.debug(
                            "SpecialNotificationService.sendSpecialNotification: chybí telefonní číslo pro playerId={} (userId={})",
                            player != null ? player.getId() : null,
                            user.getId()
                    );
                }
            }
        });
    }

    /**
     * Načítá možné cíle pro speciální notifikaci.
     *
     * Zahrnuje:
     * - (do budoucna) schválené hráče s přiřazeným aktivním uživatelem,
     * - aktivní uživatele bez přiřazených hráčů.
     *
     * Aktuálně se neschvaluje podle PlayerStatus, pouze podle existence
     * uživatele a příznaku enabled. Filtrování podle "APPROVED" můžeš
     * doplnit v případě, že budeš mít na PlayerEntity vhodnou property.
     */
    @Override
    public List<SpecialNotificationTargetDTO> getSpecialNotificationTargets() {

        List<SpecialNotificationTargetDTO> result = new ArrayList<>();

        // 1) Hráči s přiřazeným aktivním uživatelem
        List<PlayerEntity> allPlayers = playerRepository.findAll();

        for (PlayerEntity player : allPlayers) {

            AppUserEntity user = player.getUser();
            if (user == null || !user.isEnabled()) {
                // hráč bez uživatele nebo uživatel není aktivní – přeskočit
                continue;
            }

            // TODO: pokud budeš mít na PlayerEntity něco jako getStatus() == PlayerStatus.APPROVED,
            // můžeš tady přidat filtr:
            // if (player.getStatus() != PlayerStatus.APPROVED) continue;

            String userName = buildUserFullName(user);
            String displayName = player.getFullName() + " (" + userName + ", " + user.getEmail() + ")";

            SpecialNotificationTargetDTO dto = new SpecialNotificationTargetDTO();
            dto.setUserId(user.getId());
            dto.setPlayerId(player.getId());
            dto.setDisplayName(displayName);
            dto.setType("PLAYER");

            result.add(dto);
        }

        // 2) Aktivní uživatelé bez hráčů
        List<AppUserEntity> allUsers = appUserRepository.findAll();

        allUsers.stream()
                .filter(AppUserEntity::isEnabled)
                .filter(user -> user.getPlayers() == null || user.getPlayers().isEmpty())
                .forEach(user -> {
                    String userName = buildUserFullName(user);
                    String displayName = userName + " (" + user.getEmail() + ")";

                    SpecialNotificationTargetDTO dto = new SpecialNotificationTargetDTO();
                    dto.setUserId(user.getId());
                    dto.setPlayerId(null);
                    dto.setDisplayName(displayName);
                    dto.setType("USER");

                    result.add(dto);
                });

        // setřídit podle displayName
        result.sort(Comparator.comparing(
                SpecialNotificationTargetDTO::getDisplayName,
                String.CASE_INSENSITIVE_ORDER
        ));

        log.debug("SpecialNotificationService.getSpecialNotificationTargets: vráceno {} cílů", result.size());

        return result;
    }

    /**
     * Odesílá email se speciální zprávou.
     *
     * Implementace používá EmailMessageBuilder pro sestavení
     * obsahu emailu v jednotném formátu. Email se odesílá
     * prostřednictvím EmailService bez ohledu na uživatelská
     * nastavení notifikací.
     *
     * V DEMO režimu se email neodesílá, ale ukládá se
     * do DemoNotificationStore.
     */
    private void sendSpecialEmail(String to,
                                  AppUserEntity user,
                                  PlayerEntity player,
                                  SpecialNotificationRequestDTO request) {

        if (to == null || to.isBlank()) {
            return;
        }

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildSpecialMessage(
                        user,
                        player,
                        request.getTitle(),
                        request.getMessage()
                );

        if (content == null) {
            log.debug("SpecialNotificationService.sendSpecialEmail: EmailContent je null, nic se neodesílá");
            return;
        }

        // DEMO režim – uložit do demo úložiště a nevolat SMTP
        if (demoModeService.isDemoMode()) {
            demoNotificationStore.addEmail(
                    to,
                    content.subject(),
                    content.body(),
                    content.html(),
                    NotificationType.SPECIAL_MESSAGE,
                    "SPECIAL"
            );
            log.debug("DEMO MODE: speciální e-mail uložen do DemoNotificationStore, nic se neodesílá (to={})", to);
            return;
        }

        try {
            if (content.html()) {
                emailService.sendHtmlEmail(to, content.subject(), content.body());
            } else {
                emailService.sendSimpleEmail(to, content.subject(), content.body());
            }
        } catch (Exception ex) {
            log.warn("Nepodařilo se odeslat speciální email na {}: {}", to, ex.getMessage());
        }
    }

    /**
     * Vrací telefonní číslo pro odeslání SMS.
     *
     * Telefonní číslo je uloženo v PlayerSettingsEntity.contactPhone.
     * Nastavení smsEnabled se u speciálních zpráv ignoruje.
     */
    private String resolvePhoneNumber(PlayerSettingsEntity playerSettings) {
        if (playerSettings == null) {
            return null;
        }

        String phone = playerSettings.getContactPhone();
        if (phone != null && !phone.isBlank()) {
            return phone;
        }
        return null;
    }

    /**
     * Odesílá SMS se speciální zprávou.
     *
     * Implementace používá SmsMessageBuilder pro sestavení
     * textu SMS tak, aby byl v jednotném formátu se zbytkem systému.
     *
     * V DEMO režimu se SMS neodesílá, ale ukládá se
     * do DemoNotificationStore.
     */
    private void sendSpecialSms(String phoneNumber,
                                PlayerEntity player,
                                SpecialNotificationRequestDTO request) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return;
        }

        String text = smsMessageBuilder.buildSpecialMessage(
                request.getTitle(),
                request.getMessage(),
                player
        );

        if (text == null || text.isBlank()) {
            log.debug("SpecialNotificationService.sendSpecialSms: prázdný text SMS, nic se neodesílá");
            return;
        }

        // DEMO režim – uložit do demo úložiště
        if (demoModeService.isDemoMode()) {
            demoNotificationStore.addSms(
                    phoneNumber,
                    text,
                    NotificationType.SPECIAL_MESSAGE
            );
            log.debug("DEMO MODE: speciální SMS uložena do DemoNotificationStore, nic se neodesílá (phone={})", phoneNumber);
            return;
        }

        try {
            smsService.sendSms(phoneNumber, text);
        } catch (Exception ex) {
            log.warn("Nepodařilo se odeslat speciální SMS na {}: {}", phoneNumber, ex.getMessage());
        }
    }

    /**
     * Vrací preferovaný email pro speciální zprávu.
     *
     * Pravidla:
     * - pokud playerSettings.contactEmail je prázdný → použije se email uživatele,
     * - pokud contactEmail je stejný jako email uživatele (case-insensitive) → použije se email uživatele,
     * - pokud contactEmail je různý → použije se contactEmail (hráčský).
     *
     * Pokud žádný email není k dispozici, vrací null.
     */
    private String resolveEmail(AppUserEntity user, PlayerSettingsEntity playerSettings) {
        String userEmail = (user != null && user.getEmail() != null && !user.getEmail().isBlank())
                ? user.getEmail()
                : null;

        String playerEmail = (playerSettings != null
                && playerSettings.getContactEmail() != null
                && !playerSettings.getContactEmail().isBlank())
                ? playerSettings.getContactEmail()
                : null;

        // Hráč nemá vlastní email → použije se email uživatele
        if (playerEmail == null) {
            return userEmail;
        }

        // Hráč má email, ale je stejný jako uživatel → použije se email uživatele
        if (userEmail != null && userEmail.equalsIgnoreCase(playerEmail)) {
            return userEmail;
        }

        // Hráč má vlastní odlišný email → použije se hráčský email
        return playerEmail;
    }

    /**
     * Pomocná metoda pro sestavení celého jména uživatele.
     */
    private String buildUserFullName(AppUserEntity user) {
        String name = user.getName() != null ? user.getName() : "";
        String surname = user.getSurname() != null ? user.getSurname() : "";
        String fullName = (name + " " + surname).trim();
        return fullName.isEmpty() ? user.getEmail() : fullName;
    }
}