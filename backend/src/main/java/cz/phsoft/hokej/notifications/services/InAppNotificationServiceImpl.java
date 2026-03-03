package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.notifications.entities.NotificationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Implementace služby pro ukládání aplikačních (in-app) notifikací.
 *
 * Třída neřeší odesílání e-mailů ani SMS. Vytváří zjednodušené i detailní
 * notifikace v databázi pro zobrazení v uživatelském rozhraní.
 *
 * Text notifikací se sestavuje pomocí InAppNotificationBuilder, tato třída
 * zajišťuje:
 * - nalezení cílového uživatele podle hráče,
 * - případnou deduplikaci notifikací podle uživatele, zápasu a typu,
 * - vytvoření a uložení entit NotificationEntity.
 */
@Service
public class InAppNotificationServiceImpl implements InAppNotificationService {

    /**
     * Logger pro výpis diagnostických a ladicích informací
     * týkajících se ukládání in-app notifikací.
     */
    private static final Logger log = LoggerFactory.getLogger(InAppNotificationServiceImpl.class);

    /**
     * Repository pro práci s entitami NotificationEntity.
     *
     * Používá se pro ukládání, aktualizaci a vyhledávání existujících
     * notifikací v databázi.
     */
    private final NotificationRepository notificationRepository;

    /**
     * Builder pro sestavení textového obsahu in-app notifikací.
     *
     * Je používán pro generování krátkých i plných textů na základě
     * typu notifikace a poskytnutého kontextu.
     */
    private final InAppNotificationBuilder inAppNotificationBuilder;

    /**
     * Hodiny poskytující aktuální čas.
     *
     * Umožňují nadefinovat časové razítko uložené notifikace,
     * zároveň usnadňují testování díky možnosti použít fixní Clock.
     */
    private final Clock clock;

    /**
     * Vytváří instanci implementace služby in-app notifikací.
     *
     * Všechny závislosti jsou injektovány prostřednictvím konstruktoru.
     *
     * @param notificationRepository repository pro práci s notifikacemi
     * @param inAppNotificationBuilder builder pro sestavení obsahu notifikací
     * @param clock hodiny používané pro získání aktuálního času
     */
    public InAppNotificationServiceImpl(NotificationRepository notificationRepository,
                                        InAppNotificationBuilder inAppNotificationBuilder,
                                        Clock clock) {
        this.notificationRepository = notificationRepository;
        this.inAppNotificationBuilder = inAppNotificationBuilder;
        this.clock = clock;
    }

    /**
     * Uloží notifikaci související s hráčem bez doplňkových údajů
     * o e-mailové a SMS komunikaci.
     *
     * Jedná se o zjednodušenou variantu volání, která pouze deleguje
     * na rozšířenou metodu s null hodnotami pro emailTo a smsTo.
     *
     * @param player  hráč, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu notifikace
     */
    @Override
    public void storeForPlayer(PlayerEntity player,
                               NotificationType type,
                               Object context) {
        storeForPlayer(player, type, context, null, null);
    }

    /**
     * Uloží nebo aktualizuje notifikaci související s hráčem.
     *
     * Metoda:
     * - ověří, zda je k hráči přiřazen uživatel,
     * - sestaví obsah notifikace pomocí InAppNotificationBuilder,
     * - pokusí se z kontextu odvodit zápas pro případnou deduplikaci,
     * - při existenci notifikace pro kombinaci uživatel, zápas a typ ji aktualizuje,
     * - pokud neexistuje, vytvoří nový záznam NotificationEntity.
     *
     * E-mail a SMS adresáti se ukládají pro případné auditní nebo
     * informační účely.
     *
     * @param player  hráč, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu notifikace
     * @param emailTo cílová e-mailová adresa použitá při odeslání e-mailu
     * @param smsTo   cílové telefonní číslo použité při odeslání SMS
     */
    @Override
    public void storeForPlayer(PlayerEntity player,
                               NotificationType type,
                               Object context,
                               String emailTo,
                               String smsTo) {

        if (player == null) {
            log.debug("InAppNotificationService.storeForPlayer: player is null, nic se neukládá");
            return;
        }

        AppUserEntity owner = player.getUser();
        if (owner == null) {
            log.debug(
                    "InAppNotificationService.storeForPlayer: player {} nemá přiřazeného uživatele, nic se neukládá",
                    player.getId()
            );
            return;
        }

        InAppNotificationBuilder.InAppNotificationContent content =
                buildContent(type, owner, player, context);

        String messageShort = content != null && content.title() != null && !content.title().isBlank()
                ? content.title()
                : type.name();

        String messageFull = content != null && content.message() != null && !content.message().isBlank()
                ? content.message()
                : type.name();

        // Pokus o navázání notifikace na konkrétní zápas.
        // Pokud je context instanceof MatchEntity, využije se pro deduplikaci.
        MatchEntity match = resolveMatchFromContext(context);

        if (match != null) {
            // Deduplikace podle user + match + type.
            Optional<NotificationEntity> existingOpt =
                    notificationRepository.findByUserAndMatchAndType(owner, match, type);

            if (existingOpt.isPresent()) {
                NotificationEntity existing = existingOpt.get();

                existing.setMessageShort(messageShort);
                existing.setMessageFull(messageFull);
                existing.setEmailTo(emailTo);
                existing.setSmsTo(smsTo);
                existing.setCreatedAt(Instant.now(clock));

                notificationRepository.save(existing);

                log.debug(
                        "InAppNotificationService.storeForPlayer: aktualizována existující notifikace type={} userId={} playerId={} matchId={} emailTo={} smsTo={}",
                        type,
                        owner.getId(),
                        player.getId(),
                        match.getId(),
                        emailTo,
                        smsTo
                );
                return;
            }
        }

        // Pokud není match, nebo neexistuje záznam pro kombinaci (user, match, type),
        // vytvoří se nová notifikace.
        NotificationEntity entity = new NotificationEntity();
        entity.setUser(owner);
        entity.setPlayer(player);
        if (match != null) {
            entity.setMatch(match);
        }
        entity.setType(type);
        entity.setMessageShort(messageShort);
        entity.setMessageFull(messageFull);
        entity.setCreatedAt(Instant.now(clock));

        entity.setEmailTo(emailTo);
        entity.setSmsTo(smsTo);

        notificationRepository.save(entity);

        log.debug(
                "InAppNotificationService.storeForPlayer: uložena notifikace type={} userId={} playerId={} matchId={} emailTo={} smsTo={}",
                type,
                owner.getId(),
                player.getId(),
                match != null ? match.getId() : null,
                emailTo,
                smsTo
        );
    }

    /**
     * Uloží notifikaci související s uživatelem bez doplňkových údajů
     * o e-mailovém příjemci.
     *
     * Jedná se o zjednodušenou variantu volání, která pouze deleguje
     * na rozšířenou metodu s null hodnotou pro emailTo.
     *
     * @param user    uživatel, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu notifikace
     */
    @Override
    public void storeForUser(AppUserEntity user,
                             NotificationType type,
                             Object context) {
        storeForUser(user, type, context, null);
    }

    /**
     * Uloží notifikaci související s uživatelem.
     *
     * Notifikace není navázána na konkrétní zápas ani hráče. Text
     * notifikace je sestaven pomocí InAppNotificationBuilder. E-mailová
     * adresa se ukládá pro případné auditní nebo informační účely.
     *
     * @param user    uživatel, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu notifikace
     * @param emailTo cílová e-mailová adresa použitá při odeslání e-mailu
     */
    @Override
    public void storeForUser(AppUserEntity user,
                             NotificationType type,
                             Object context,
                             String emailTo) {

        if (user == null) {
            log.debug("InAppNotificationService.storeForUser: user is null, nic se neukládá");
            return;
        }

        InAppNotificationBuilder.InAppNotificationContent content =
                buildContent(type, user, null, context);

        String messageShort = content != null && content.title() != null && !content.title().isBlank()
                ? content.title()
                : type.name();

        String messageFull = content != null && content.message() != null && !content.message().isBlank()
                ? content.message()
                : type.name();

        NotificationEntity entity = new NotificationEntity();
        entity.setUser(user);
        entity.setType(type);
        entity.setMessageShort(messageShort);
        entity.setMessageFull(messageFull);
        entity.setCreatedAt(Instant.now(clock));

        // Uživatelské notifikace nejsou vázány na match – match se zde nenastavuje.
        entity.setEmailTo(emailTo);
        entity.setSmsTo(null);

        notificationRepository.save(entity);

        log.debug("InAppNotificationService.storeForUser: uložena notifikace type={} userId={} emailTo={}",
                type, user.getId(), emailTo);
    }

    /**
     * Uloží speciální zprávu typu SPECIAL_MESSAGE bez doplňkových údajů
     * o e-mailové a SMS komunikaci.
     *
     * Jedná se o zjednodušenou variantu volání, která deleguje na
     * rozšířenou metodu s null hodnotami pro emailTo a smsTo.
     *
     * @param user         uživatel, ke kterému je notifikace přiřazena
     * @param player       hráč, kterého se notifikace týká (může být null)
     * @param messageShort stručný text notifikace pro seznam
     * @param messageFull  plný text notifikace pro detail
     */
    @Override
    public void storeSpecialMessage(AppUserEntity user,
                                    PlayerEntity player,
                                    String messageShort,
                                    String messageFull) {
        storeSpecialMessage(user, player, messageShort, messageFull, null, null);
    }

    /**
     * Uloží speciální zprávu typu SPECIAL_MESSAGE včetně údajů o e-mailovém
     * a SMS příjemci.
     *
     * Texty zprávy jsou předávány přímo z volající vrstvy. Pokud některý
     * z textů není vyplněn, použije se jako záloha název typu notifikace
     * SPECIAL_MESSAGE. Notifikace není navázána na konkrétní zápas.
     *
     * @param user         uživatel, ke kterému je notifikace přiřazena
     * @param player       hráč, kterého se notifikace týká (může být null)
     * @param messageShort stručný text notifikace pro seznam
     * @param messageFull  plný text notifikace pro detail
     * @param emailTo      cílová e-mailová adresa použitá při odeslání e-mailu
     * @param smsTo        cílové telefonní číslo použité při odeslání SMS
     */
    @Override
    public void storeSpecialMessage(AppUserEntity user,
                                    PlayerEntity player,
                                    String messageShort,
                                    String messageFull,
                                    String emailTo,
                                    String smsTo) {

        if (user == null) {
            log.debug("InAppNotificationService.storeSpecialMessage: user is null, nic se neukládá");
            return;
        }

        String shortText = (messageShort != null && !messageShort.isBlank())
                ? messageShort
                : NotificationType.SPECIAL_MESSAGE.name();

        String fullText = (messageFull != null && !messageFull.isBlank())
                ? messageFull
                : NotificationType.SPECIAL_MESSAGE.name();

        NotificationEntity entity = new NotificationEntity();
        entity.setUser(user);
        if (player != null) {
            entity.setPlayer(player);
        }
        entity.setType(NotificationType.SPECIAL_MESSAGE);
        entity.setMessageShort(shortText);
        entity.setMessageFull(fullText);
        entity.setCreatedAt(Instant.now(clock));

        // Speciální zprávy nenvážeme na konkrétní zápas – jedná se
        // o obecnou administrátorskou komunikaci.
        entity.setEmailTo(emailTo);
        entity.setSmsTo(smsTo);

        notificationRepository.save(entity);

        log.debug(
                "InAppNotificationService.storeSpecialMessage: uložena SPECIAL_MESSAGE userId={} playerId={} emailTo={} smsTo={}",
                user.getId(),
                player != null ? player.getId() : null,
                emailTo,
                smsTo
        );
    }

    /**
     * Sestavuje obsah in-app notifikace pomocí InAppNotificationBuilder.
     *
     * Metoda obaluje volání builderu a zachycuje případné výjimky,
     * aby neblokovaly uložení notifikace. Pokud builder vrátí null
     * nebo dojde k chybě, vrací se null a volající použije fallback
     * typu type.name().
     *
     * @param type    typ notifikace
     * @param user    uživatel, kterého se notifikace týká
     * @param player  hráč, kterého se notifikace týká (může být null)
     * @param context volitelný kontext pro sestavení textu
     * @return sestavený obsah notifikace nebo null při chybě či nedefinovaném typu
     */
    private InAppNotificationBuilder.InAppNotificationContent buildContent(NotificationType type,
                                                                           AppUserEntity user,
                                                                           PlayerEntity player,
                                                                           Object context) {
        try {
            return inAppNotificationBuilder.build(type, user, player, context);
        } catch (Exception ex) {
            log.debug("InAppNotificationService.buildContent: chyba při sestavování notifikace pro type {}: {}",
                    type, ex.getMessage());
            return null;
        }
    }

    /**
     * Pokusí se z kontextu vyčíst zápas pro navázání notifikace.
     *
     * V aktuální implementaci se podporuje varianta, kdy je context
     * přímo instancí MatchEntity. Pokud context neobsahuje odpovídající
     * typ, vrací se null.
     *
     * Metoda je připravena pro budoucí rozšíření o další typy kontextů.
     *
     * @param context kontext předaný volajícím
     * @return entita zápasu, pokud je v kontextu přímo obsažena, jinak null
     */
    private MatchEntity resolveMatchFromContext(Object context) {
        if (context instanceof MatchEntity) {
            return (MatchEntity) context;
        }
        return null;
    }
}