package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.player.repositories.PlayerSettingsRepository;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.user.exceptions.UserNotFoundException;
import cz.phsoft.hokej.notifications.dto.SpecialNotificationTargetDTO;
import cz.phsoft.hokej.notifications.dto.SpecialNotificationRequestDTO;
import cz.phsoft.hokej.notifications.email.EmailMessageBuilder;
import cz.phsoft.hokej.notifications.email.EmailService;
import cz.phsoft.hokej.notifications.sms.SmsMessageBuilder;
import cz.phsoft.hokej.notifications.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implementace služby pro odesílání speciálních zpráv administrátorem.
 *
 * Odpovědnost třídy spočívá v orkestraci více komunikačních kanálů
 * pro speciální administrátorské zprávy. Pro definované příjemce
 * se vytvářejí in-app notifikace a podle požadavku se odesílají
 * e-maily a SMS zprávy.
 *
 * Všechny akce se provádějí bez ohledu na uživatelská notifikační
 * nastavení, protože speciální zprávy představují nadřazenou,
 * administrativní nebo systémovou komunikaci.
 *
 * V DEMO režimu se e-maily a SMS fyzicky neodesílají. Jsou ukládány
 * do DemoNotificationStore a zpřístupňovány na frontendu přes
 * speciální demo endpointy.
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

    /**
     * Vytváří instanci služby pro speciální notifikace.
     *
     * Závislosti na repository, službách a message builder utilitách
     * jsou předávány konstruktorem, aby bylo možné službu snadno
     * testovat a spravovat v rámci Spring kontextu a konfigurovat
     * chování DEMO režimu.
     *
     * @param appUserRepository repository pro práci s entitami uživatelů
     * @param playerRepository repository pro práci s entitami hráčů
     * @param playerSettingsRepository repository pro nastavení hráčů
     * @param inAppNotificationService služba pro ukládání in-app notifikací
     * @param emailService služba pro fyzické odeslání e-mailů
     * @param smsService služba pro fyzické odeslání SMS zpráv
     * @param emailMessageBuilder builder pro sestavení obsahu e-mailů
     * @param smsMessageBuilder builder pro sestavení textu SMS zpráv
     * @param demoModeService služba určující, zda je aplikace v DEMO režimu
     * @param demoNotificationStore úložiště pro e-maily a SMS v DEMO režimu
     */
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

    /**
     * Odesílá speciální zprávu všem definovaným příjemcům.
     *
     * Pro každý cíl definovaný ve vstupním DTO se provádí:
     * vytvoření in-app notifikace typu SPECIAL_MESSAGE a podle
     * nastavení také odeslání e-mailu a SMS zprávy. E-mail i SMS
     * jsou získávány z preferencí uživatele a případných nastavení
     * hráče, přičemž se ignorují běžná uživatelská notifikační
     * nastavení.
     *
     * Selhání odeslání e-mailu nebo SMS neblokuje uložení in-app
     * notifikace. V DEMO režimu se zprávy ukládají do
     * DemoNotificationStore místo fyzického odeslání.
     *
     * @param request definice zprávy a seznam cílů pro speciální notifikaci
     * @throws NullPointerException pokud je request null
     * @throws UserNotFoundException pokud některý z cílových uživatelů neexistuje
     * @throws PlayerNotFoundException pokud některý z cílových hráčů neexistuje
     */
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

            String emailTo = null;
            String smsTo = null;

            // EMAIL
            if (request.isSendEmail()) {
                emailTo = resolveEmail(user, playerSettings);
                if (emailTo != null && !emailTo.isBlank()) {
                    sendSpecialEmail(emailTo, user, player, request);
                } else {
                    log.debug(
                            "SpecialNotificationService.sendSpecialNotification: není k dispozici email pro playerId={} (userId={})",
                            player != null ? player.getId() : null,
                            user.getId()
                    );
                    emailTo = null;
                }
            }

            // SMS
            if (request.isSendSms() && playerSettings != null) {
                smsTo = resolvePhoneNumber(playerSettings);
                if (smsTo != null && !smsTo.isBlank()) {
                    sendSpecialSms(smsTo, player, request);
                } else {
                    log.debug(
                            "SpecialNotificationService.sendSpecialNotification: chybí telefonní číslo pro playerId={} (userId={})",
                            player != null ? player.getId() : null,
                            user.getId()
                    );
                    smsTo = null;
                }
            }

            // IN-APP – SPECIAL_MESSAGE + audit kanálů
            inAppNotificationService.storeSpecialMessage(
                    user,
                    player,
                    request.getTitle(),
                    request.getMessage(),
                    emailTo,
                    smsTo
            );
        });
    }

    /**
     * Načítá možné cíle pro speciální notifikaci.
     *
     * Zahrnuti jsou:
     * hráči s přiřazeným aktivním uživatelem a dále aktivní
     * uživatelé bez přiřazených hráčů. Seznam je složen do DTO
     * určeného pro výběr v uživatelském rozhraní a následně
     * setříděn podle zobrazovaného jména.
     *
     * Aktuálně se neschvaluje podle statusu hráče. Filtrování
     * podle stavu, například PlayerStatus.APPROVED, může být
     * doplněno v budoucnu po doplnění vlastnosti do PlayerEntity.
     *
     * @return seznam cílů pro speciální notifikaci připravený pro UI
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

            // Pokud budeš mít na PlayerEntity něco jako getStatus() == PlayerStatus.APPROVED,
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
     * Odesílá e-mail se speciální zprávou na zadanou adresu.
     *
     * Obsah e-mailu se sestavuje pomocí EmailMessageBuilder
     * tak, aby byl v souladu s jednotným formátováním systému.
     * Odeslání se provádí prostřednictvím EmailService, přičemž
     * se ignorují individuální notifikační preference uživatelů.
     *
     * V DEMO režimu není e-mail fyzicky odeslán, ale je uložen
     * do DemoNotificationStore pro zobrazení v demo rozhraní.
     *
     * @param to cílová e-mailová adresa
     * @param user uživatel, kterému je zpráva určena
     * @param player hráč, pokud je zpráva vázána na konkrétního hráče; může být null
     * @param request definice speciální zprávy včetně titulku a textu
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
     * Určuje telefonní číslo použití pro odeslání SMS.
     *
     * Telefonní číslo se získává z PlayerSettingsEntity.contactPhone.
     * Nastavení smsEnabled se pro speciální zprávy ignoruje, protože
     * speciální komunikace má být doručena bez ohledu na běžné
     * SMS preference.
     *
     * @param playerSettings nastavení hráče, ze kterého se telefonní číslo čte
     * @return telefonní číslo připravené k použití nebo null, pokud není dostupné
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
     * Odesílá SMS se speciální zprávou na zadané telefonní číslo.
     *
     * Text zprávy se sestavuje pomocí SmsMessageBuilder tak,
     * aby odpovídal jednotnému formátu používanému v aplikaci.
     * V případě prázdného nebo chybějícího textu se zpráva neodesílá.
     *
     * V DEMO režimu není SMS fyzicky odeslána. Místo toho je
     * uložena do DemoNotificationStore a dostupná v rámci
     * demo funkčností.
     *
     * @param phoneNumber cílové telefonní číslo
     * @param player hráč, kterého se zpráva týká; může být null
     * @param request definice speciální zprávy včetně titulku a textu
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
     * Určuje preferovaný e-mail pro odeslání speciální zprávy.
     *
     * Pravidla:
     * pokud hráč nemá vlastní e-mail, použije se e-mail uživatele;
     * pokud hráčův e-mail existuje a je shodný s e-mailem uživatele
     * (bez ohledu na velikost písmen), použije se e-mail uživatele;
     * pokud je hráčův e-mail odlišný, použije se hráčský e-mail.
     *
     * Pokud není k dispozici žádný nepustý e-mail, vrací se null.
     *
     * @param user uživatel, ke kterému je hráč přiřazen
     * @param playerSettings nastavení hráče, ze kterého se čte případný hráčský e-mail
     * @return vybraný e-mail pro speciální notifikaci nebo null, pokud není k dispozici
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
     * Sestavuje celé jméno uživatele pro zobrazovací účely.
     *
     * Jméno je vytvořeno spojením jména a příjmení, případně
     * je použita pouze jedna z hodnot. Pokud jsou obě hodnoty
     * prázdné, použije se e-mail uživatele jako zástupná hodnota.
     *
     * @param user uživatel, jehož zobrazované jméno se sestavuje
     * @return celé jméno uživatele nebo jeho e-mail, pokud jméno není k dispozici
     */
    private String buildUserFullName(AppUserEntity user) {
        String name = user.getName() != null ? user.getName() : "";
        String surname = user.getSurname() != null ? user.getSurname() : "";
        String fullName = (name + " " + surname).trim();
        return fullName.isEmpty() ? user.getEmail() : fullName;
    }
}