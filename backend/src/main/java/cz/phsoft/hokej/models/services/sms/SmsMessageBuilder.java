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
 *
 * Odpovědnosti:
 * - sestavení lidsky čitelného obsahu SMS pro různé typy notifikací,
 * - centralizace formátování SMS zpráv (princip DRY),
 * - oddělení textové logiky od business logiky a schedulingu.
 *
 * Tato třída se používá zejména v NotificationServiceImpl a SmsService
 * jako zdroj výsledného textu SMS zpráv.
 *
 * Třída neřeší:
 * - odesílání SMS (to zajišťuje SmsService),
 * - změny v databázi,
 * - oprávnění ani validace vstupů (předpokládá se, že vstupy jsou zvalidované dříve).
 */
@Component
public class SmsMessageBuilder {

    /**
     * Repozitář registrací používaný pro read-only výpočty
     * (například aktuální počet hráčů se statusem REGISTERED).
     */
    private final MatchRegistrationRepository matchRegistrationRepository;

    /**
     * Repozitář zápasů používaný pro doplňující informace o zápase.
     *
     * Aktuálně se využívá minimálně, je zde ponechán pro možné rozšíření.
     */
    private final MatchRepository matchRepository;

    /**
     * Repozitář hráčů používaný pro případné rozšíření zpráv
     * o detailnější informace o hráčích.
     */
    private final PlayerRepository playerRepository;

    /**
     * Jednotný formát data používaný v SMS zprávách.
     *
     * Slouží k tomu, aby měly všechny SMS zprávy konzistentní formát data.
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
    // HLAVNÍ VSTUPNÍ METODA PRO NotificationServiceImpl
    // ====================================================

    /**
     * Sestaví SMS text pro hráče podle typu notifikace a kontextu.
     *
     * Metoda slouží jako hlavní vstupní bod pro NotificationServiceImpl,
     * kde se podle NotificationType a typu contextu rozhoduje,
     * který specializovaný builder se použije.
     *
     * Podporované scénáře:
     * - změny registrace hráče na zápas,
     * - obecné informace o zápase (zrušení, změna času, připomenutí).
     *
     * @param type    typ notifikace, pro kterou se SMS generuje
     * @param player  hráč, pro kterého se SMS generuje
     * @param context kontext notifikace, obvykle MatchRegistrationEntity nebo MatchEntity
     * @return hotový text SMS nebo {@code null}, pokud se pro daný typ notifikace SMS neposílá
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

            // ostatní typy se přes SMS neposílají
            default -> null;
        };
    }

    /**
     * Bezpečné přetypování contextu na očekávaný typ.
     *
     * Pokud context není zadaného typu, vrací se null.
     *
     * @param context      vstupní objekt kontextu
     * @param expectedType očekávaný typ kontextu
     * @param <T>          generický typ výsledku
     * @return přetypovaný objekt nebo null, pokud typ neodpovídá
     */
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
     * Zpráva popisuje:
     * - datum zápasu,
     * - aktuální obsazenost zápasu,
     * - jméno hráče,
     * - slovní popis změny stavu (přihlášení, odhlášení, omluva, náhradník).
     *
     * Použité stavy:
     * - REGISTERED,
     * - UNREGISTERED,
     * - EXCUSED,
     * - SUBSTITUTE,
     * - RESERVED.
     *
     * Text se liší podle toho, zda změnu provedl uživatel nebo systém.
     *
     * @param registration registrace hráče k zápasu
     * @return text SMS zprávy popisující změnu registrace
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
     * Vytvoří SMS zprávu pro hráče, kteří dosud nereagovali na zápas.
     *
     * Zpráva se používá typicky několik dní před zápasem
     * v rámci plánovače, který připomíná blížící se zápasy
     * a upozorňuje na volná místa.
     *
     * Obsah zprávy:
     * - datum zápasu,
     * - aktuální počet volných míst,
     * - upozornění, že hráč ještě nereagoval.
     *
     * @param player hráč, pro kterého se zpráva vytváří
     * @param match  zápas, ke kterému se připomínka vztahuje
     * @return text SMS zprávy pro nereagujícího hráče
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
     * Vytvoří SMS zprávu s informací o změně stavu zápasu.
     *
     * Typické scénáře:
     * - zápas byl zrušen,
     * - zápas byl obnoven nebo změněn.
     *
     * Zpráva obsahuje:
     * - datum zápasu,
     * - slovní popis stavu zápasu,
     * - slovní popis důvodu zrušení (pokud je k dispozici).
     *
     * Parametr type se používá na úrovni volající služby pro rozhodnutí,
     * kdy se zpráva generuje. V této metodě se stav určuje primárně
     * z vlastností MatchEntity.
     *
     * @param type  typ notifikace, pro kterou se zpráva vytváří
     * @param match zápas, jehož stav se oznamuje
     * @return text SMS zprávy popisující stav zápasu
     */
    public String buildMessageMatchInfo(NotificationType type, MatchEntity match) {
        MatchStatus matchStatus = match.getMatchStatus();
        MatchCancelReason cancelReason = match.getCancelReason();

        String statusText = switch (matchStatus) {
            case CANCELED -> "byl zrušen";
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
     * Zpráva shrnuje:
     * - datum zápasu,
     * - aktuální počet přihlášených hráčů a maximální kapacitu,
     * - orientační cenu na jednoho hráče (celková cena dělená počtem přihlášených).
     *
     * @param registration registrace hráče k zápasu, pro kterou se připomínka vytváří
     * @return text finální SMS zprávy v den zápasu
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
