package cz.phsoft.hokej.models.services.sms;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/**
 * Builder pro generování textů SMS zpráv.
 * <p>
 * Slouží jako centrální místo pro skládání lidsky čitelných SMS
 * používaných v aplikaci. Řeší pouze textovou podobu zprávy, nikoliv
 * odesílání nebo business logiku.
 * </p>
 *
 * Odpovědnost:
 * <ul>
 *     <li>sestavení lidsky čitelného obsahu SMS,</li>
 *     <li>centrální místo pro formátování zpráv (princip DRY),</li>
 *     <li>oddělení textové logiky od business logiky a schedulingu.</li>
 * </ul>
 *
 * Třída vytváří SMS texty pro:
 * <ul>
 *     <li>registraci / odhlášení / omluvu hráče,</li>
 *     <li>připomenutí hráčům, kteří nereagovali,</li>
 *     <li>finální připomínku v den zápasu,</li>
 *     <li>obecné info o zápasu (zrušení, změna času).</li>
 * </ul>
 *
 * Třída neřeší:
 * <ul>
 *     <li>odesílání SMS (to zajišťuje {@link SmsService}),</li>
 *     <li>změny v databázi,</li>
 *     <li>oprávnění ani validace (předpokládá validní vstup).</li>
 * </ul>
 */
@Component
public class SmsMessageBuilder {

    /**
     * Repozitář registrací – používá se pro read-only výpočty
     * (např. aktuální počet hráčů se statusem REGISTERED).
     */
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    /**
     * Jednotný formát data používaný v SMS zprávách.
     */
    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SmsMessageBuilder(MatchRegistrationRepository matchRegistrationRepository,
                             MatchRepository matchRepository,
                             PlayerRepository playerRepository) {
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    // ====================================================
    // HLAVNÍ METODA PRO NotificationServiceImpl
    // ====================================================

    /**
     * Sestaví SMS text pro hráče podle typu notifikace a kontextu.
     *
     * @param type    typ notifikace
     * @param player  hráč (aktuálně se používá hlavně pro jméno – pro rozšíření do budoucna)
     * @param context kontext – typicky {@link MatchRegistrationEntity} nebo {@link MatchEntity}
     * @return hotový text SMS nebo {@code null}, pokud pro daný typ nic neposíláme
     */
    public String buildForNotification(NotificationType type,
                                       PlayerEntity player,
                                       Object context) {

        return switch (type) {

            // Registrace / odhlášení / přesun ve frontě / omluvy
            case MATCH_REGISTRATION_CREATED,
                 MATCH_REGISTRATION_UPDATED,
                 MATCH_REGISTRATION_CANCELED,
                 MATCH_REGISTRATION_RESERVED,
                 MATCH_WAITING_LIST_MOVED_UP,
                 PLAYER_EXCUSED,
                 PLAYER_NO_EXCUSED -> {
                MatchRegistrationEntity reg =
                        castContext(context, MatchRegistrationEntity.class);
                if (reg == null) {
                    yield null;
                }
                yield buildMessageRegistration(reg);
            }

            // Obecné info / změny zápasu
            case MATCH_REMINDER,
                 MATCH_CANCELED,
                 MATCH_TIME_CHANGED -> {
                MatchEntity match = castContext(context, MatchEntity.class);
                if (match == null) {
                    yield null;
                }
                yield buildMessageMatchInfo(type, match);
            }

            // ostatní typy přes SMS neposíláme
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T castContext(Object context, Class<T> expectedType) {
        if (context == null) {
            return null;
        }
        if (!expectedType.isInstance(context)) {
            return null;
        }
        return (T) context;
    }

    // ====================================================
    // REGISTRACE / ODHLÁŠENÍ / OMLUVA
    // ====================================================

    /**
     * Vytvoří SMS zprávu po změně registrace hráče na zápas.
     *
     * Používá se pro stavy:
     * <ul>
     *     <li>{@link PlayerMatchStatus#REGISTERED},</li>
     *     <li>{@link PlayerMatchStatus#UNREGISTERED},</li>
     *     <li>{@link PlayerMatchStatus#EXCUSED},</li>
     *     <li>{@link PlayerMatchStatus#SUBSTITUTE},</li>
     *     <li>{@link PlayerMatchStatus#RESERVED}.</li>
     * </ul>
     *
     * @param registration registrace hráče k zápasu
     * @return text SMS zprávy
     */
    public String buildMessageRegistration(MatchRegistrationEntity registration) {

        PlayerMatchStatus status = registration.getStatus();
        boolean createdByUser = "user".equals(registration.getCreatedBy());

        String statusText;

        if (createdByUser) {
            statusText = switch (status) {
                case REGISTERED -> "přihlásil se";
                case UNREGISTERED -> "odhlásil se";
                case EXCUSED -> "omluvil se";
                case SUBSTITUTE -> "možná bude";
                case RESERVED -> "byl z důvodu snížení kapacity přesunut mezi náhradníky";
                default -> "neznámý stav";
            };
        } else {
            statusText = switch (status) {
                case REGISTERED -> "byl systémem po uvolnění kapacity přihlášen";
                case UNREGISTERED -> "byl systémem odhlášen";
                case EXCUSED -> "byl systémem omluven";
                case SUBSTITUTE -> "byl systémem nastaven že možná bude";
                case RESERVED -> "byl z důvodu snížení kapacity přesunut mezi náhradníky";
                default -> "neznámý stav";
            };
        }

        Long registeredCount = matchRegistrationRepository
                .countByMatchIdAndStatus(
                        registration.getMatch().getId(),
                        PlayerMatchStatus.REGISTERED
                );

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - datum: ")
                .append(registration.getMatch().getDateTime().toLocalDate().format(dateFormatter));

        // Obsazenost se neuvádí u EXCUSED – hráč se nepočítá mezi přihlášené.
        if (status != PlayerMatchStatus.EXCUSED) {
            sb.append(", ")
                    .append(registeredCount)
                    .append("/")
                    .append(registration.getMatch().getMaxPlayers());
        }

        sb.append(", hráč: ")
                .append(registration.getPlayer().getFullName())
                .append(", status: ")
                .append(statusText);

        return sb.toString();
    }

    // ====================================================
    // NO RESPONSE – HRÁČ JEŠTĚ NEREAGOVAL
    // ====================================================

    /**
     * Vytvoří SMS zprávu pro hráče, kteří dosud nereagovali
     * na zápas (nemají žádnou registraci).
     *
     * Používá se typicky několik dní před zápasem v rámci
     * scheduleru, který připomíná blížící se zápasy.
     */
    public String buildMessageNoResponse(PlayerDTO player, MatchEntity match) {

        Long registeredCount = matchRegistrationRepository
                .countByMatchIdAndStatus(
                        match.getId(),
                        PlayerMatchStatus.REGISTERED
                );

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - upozornění: zápas ")
                .append(match.getDateTime().format(dateFormatter))
                .append(" - volná místa: ")
                .append(match.getMaxPlayers() - registeredCount)
                .append(". Ještě jste nereagoval.");

        return sb.toString();
    }

    // ====================================================
    // ZMĚNY STAVU ZÁPASU
    // ====================================================

    /**
     * Vytvoří SMS zprávu s informací o změně stavu zápasu
     * (zrušen / obnoven apod.).
     *
     * @param type  typ notifikace (např. MATCH_CANCELED, MATCH_TIME_CHANGED)
     * @param match zápas
     */
    public String buildMessageMatchInfo(NotificationType type, MatchEntity match) {
        MatchStatus matchStatus = match.getMatchStatus();
        MatchCancelReason cancelReason = match.getCancelReason();

        String statusText = switch (matchStatus) {
            case CANCELLED -> "byl zrušen";
            case UNCANCELED -> "byl obnoven";
            default -> "neznámý stav";
        };

        String cancelReasonText = switch (cancelReason) {
            case NOT_ENOUGH_PLAYERS -> "málo hráčů";
            case TECHNICAL_ISSUE -> "technické problémy (led, hala)";
            case WEATHER -> "počasí";
            case ORGANIZER_DECISION -> "rozhodnutí organizátora";
            case OTHER -> "jiný důvod";
            default -> "neznámý důvod";
        };

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - UPOZORNĚNÍ: zápas ")
                .append(match.getDateTime().format(dateFormatter))
                .append(" - ")
                .append(statusText)
                .append(" (důvod: ")
                .append(cancelReasonText)
                .append(")");

        return sb.toString();
    }

    // ====================================================
    // FINÁLNÍ SMS – DEN ZÁPASU
    // ====================================================

    /**
     * Vytvoří finální SMS zprávu v den zápasu
     * pro již přihlášené hráče.
     *
     * Obsah:
     * <ul>
     *     <li>datum zápasu,</li>
     *     <li>aktuální počet přihlášených hráčů / maximální kapacita,</li>
     *     <li>cena na jednoho hráče (celková cena / počet přihlášených).</li>
     * </ul>
     *
     * @param registration registrace hráče k zápasu
     * @return text finální SMS zprávy
     */
    public String buildMessageFinal(MatchRegistrationEntity registration) {

        MatchEntity match = registration.getMatch();

        Long registeredCount = matchRegistrationRepository
                .countByMatchIdAndStatus(
                        match.getId(),
                        PlayerMatchStatus.REGISTERED
                );

        double pricePerPlayer =
                match.getPrice() / Math.max(registeredCount, 1);

        StringBuilder sb = new StringBuilder();
        sb.append("app_hokej - připomínka zápasu ")
                .append(match.getDateTime().format(dateFormatter))
                .append(", přihlášeno: ")
                .append(registeredCount)
                .append("/")
                .append(match.getMaxPlayers())
                .append(", cena na hráče: ")
                .append(String.format("%.2f Kč", pricePerPlayer));

        return sb.toString();
    }
}
