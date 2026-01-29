package cz.phsoft.hokej.models.services.sms;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import org.springframework.stereotype.Component;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Builder pro generování textů SMS zpráv.
 * <p>
 * Slouží jako centrální místo pro skládání lidsky čitelných SMS
 * používaných v aplikaci. Řeší pouze textovou podobu zprávy, nikoliv
 * odesílání nebo business logiku.
 * </p>
 * <p>
 * Odpovědnost:
 * <ul>
 *     <li>sestavení lidsky čitelného obsahu SMS,</li>
 *     <li>centrální místo pro formátování zpráv (princip DRY),</li>
 *     <li>oddělení textové logiky od business logiky a schedulingu.</li>
 * </ul>
 * <p>
 * Třída vytváří SMS texty pro:
 * <ul>
 *     <li>registraci / odhlášení / omluvu hráče,</li>
 *     <li>připomenutí hráčům, kteří nereagovali,</li>
 *     <li>finální připomínku v den zápasu.</li>
 * </ul>
 * <p>
 * Třída neřeší:
 * <ul>
 *     <li>odesílání SMS (to zajišťuje {@link SmsService}),</li>
 *     <li>změny v databázi,</li>
 *     <li>oprávnění ani validace (předpokládá validní vstup).</li>
 * </ul>
 * <p>
 * Architektura:
 * <ul>
 *     <li>je anotována jako {@link Component} → lze ji snadno injektovat,</li>
 *     <li>používá repository pouze pro read-only výpočty (počty hráčů).</li>
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
    // REGISTRACE / ODHLÁŠENÍ / OMLUVA
    // ====================================================

    /**
     * Vytvoří SMS zprávu po změně registrace hráče na zápas.
     * <p>
     * Používá se pro tyto stavy:
     * </p>
     * <ul>
     *     <li>{@link PlayerMatchStatus#REGISTERED},</li>
     *     <li>{@link PlayerMatchStatus#EXCUSED}.</li>
     * </ul>
     * <p>
     * Logika:
     * <ul>
     *     <li>podle statusu se zvolí text (přihlásil / odhlásil / omluven),</li>
     *     <li>vždy se zobrazí datum zápasu,</li>
     *     <li>u REGISTERED / UNREGISTERED se doplní obsazenost
     *     (počet přihlášených / maximální počet hráčů).</li>
     * </ul>
     *
     * @param registration registrace hráče k zápasu
     * @return text SMS zprávy
     */
    public String buildMessageRegistration(MatchRegistrationEntity registration) {

        // TODO - KONKRÉTNÍ ZPRÁVY S KONKRÉTNÍM TEXTEM FULL NAME + DATUM ZÁPASU + POČET HRÁČŮ

        PlayerMatchStatus status = registration.getStatus();
        boolean createdByUser = registration.getCreatedBy().equals("user");

        String statusText = "";

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
     * <p>
     * Používá se typicky několik dní před zápasem v rámci
     * scheduleru, který připomíná blížící se zápasy.
     * </p>
     * <p>
     * Obsah zprávy:
     * <ul>
     *     <li>datum zápasu,</li>
     *     <li>informace o počtu volných míst,</li>
     *     <li>stručná výzva k reakci („Ještě jste nereagoval.“).</li>
     * </ul>
     *
     * @param player hráč, kterému se SMS posílá
     * @param match  zápas, ke kterému se zpráva vztahuje
     * @return text SMS zprávy
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


    public String buildMessageMatchInfo(Object type, MatchEntity match) {
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
        sb.append("app_hokej - UPOZORNĚNÍ : zápas ")
                .append(match.getDateTime().format(dateFormatter))
                .append(" - byl -")
                .append(statusText)
                .append(cancelReasonText);

        return sb.toString();

    }
    // ====================================================
    // FINÁLNÍ SMS – DEN ZÁPASU
    // ====================================================

    /**
     * Vytvoří finální SMS zprávu v den zápasu
     * pro již přihlášené hráče.
     * <p>
     * Zpráva slouží jako závěrečná připomínka a obsahuje
     * aktuální informace o obsazenosti a orientační cenu
     * na jednoho hráče.
     * </p>
     * <p>
     * Obsah:
     * <ul>
     *     <li>datum zápasu,</li>
     *     <li>aktuální počet přihlášených hráčů / maximální kapacita,</li>
     *     <li>cena na jednoho hráče (celková cena / počet přihlášených).</li>
     * </ul>
     * <p>
     * Ochrana:
     * <ul>
     *     <li>při výpočtu ceny na hráče se používá ochrana proti dělení nulou
     *     – pokud není nikdo přihlášen, bere se hodnota 1.</li>
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

        // ochrana proti dělení nulou
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
