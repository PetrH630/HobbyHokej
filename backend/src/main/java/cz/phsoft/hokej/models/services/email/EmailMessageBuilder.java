package cz.phsoft.hokej.models.services.email;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.models.services.notification.ForgottenPasswordResetContext;
import cz.phsoft.hokej.models.services.notification.MatchTimeChangeContext;
import cz.phsoft.hokej.models.services.notification.UserActivationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builder pro sestavování obsahu emailových notifikací.
 *
 * Třída slouží k centralizaci veškerých textů emailových zpráv
 * používaných v aplikaci. Na základě typu notifikace a kontextu
 * sestavuje předmět a tělo emailu, včetně rozhodnutí, zda se jedná
 * o HTML nebo plain text zprávu.
 *
 * Odpovědnost třídy:
 * - sestavení předmětu a těla emailu podle NotificationType,
 * - rozlišení cílového příjemce (uživatel, hráč, manažer),
 * - sjednocení formátu a struktury emailových zpráv.
 *
 * Třída neřeší:
 * - samotné odesílání emailů,
 * - rozhodování, komu má být notifikace doručena,
 * - oprávnění, validaci vstupů ani načítání entit.
 */
@Component
public class EmailMessageBuilder {

    private final MatchRegistrationRepository registrationRepository;

    public EmailMessageBuilder(MatchRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    /**
     * Datový nosič pro obsah emailové zprávy.
     *
     * Slouží jako návratová hodnota builderu a je následně
     * předávána do EmailService k odeslání.
     *
     * @param subject předmět emailu
     * @param body    tělo emailu
     * @param html    příznak HTML obsahu
     */
    public record EmailContent(String subject, String body, boolean html) {
    }

    /**
     * Formátovač data a času zápasu používaný v emailových zprávách.
     */
    private static final DateTimeFormatter MATCH_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy  HH:mm", new Locale("cs", "CZ"));

    // Emailové zprávy pro manažera

    /**
     * Vytváří kopii emailu určenou manažerovi.
     *
     * Nejprve se pokusí sestavit email ve variantě pro uživatele,
     * pokud není k dispozici, použije variantu pro hráče. Výsledná
     * zpráva je upravena tak, aby bylo zřejmé, že se jedná o kopii
     * určenou manažerovi.
     *
     * Pokud pro daný typ notifikace neexistuje žádná šablona,
     * metoda vrací null.
     */
    public EmailContent buildForManager(NotificationType type,
                                        PlayerEntity player,
                                        AppUserEntity manager,
                                        Object context) {

        if (manager == null) {
            return null;
        }

        String managerName = fullUserName(manager);

        EmailContent base = buildForUser(type, player, null, context);
        if (base == null) {
            base = buildForPlayer(type, player, null, context);
        }

        if (base == null) {
            return null;
        }

        String subject = "[Kopie pro manažera] " + base.subject();

        String body;
        if (base.html()) {
            body = """
                    <p><strong>Zpráva pro manažera – %s</strong></p>
                    <hr>
                    %s
                    """.formatted(
                    escape(managerName),
                    base.body()
            );
        } else {
            body = """
                    Zpráva pro manažera – %s
                    ----------------------------------------
                    %s
                    """.formatted(
                    managerName,
                    base.body()
            );
        }

        return new EmailContent(subject, body, base.html());
    }

    // Emailové zprávy pro uživatele

    /**
     * Sestavuje emailovou zprávu určenou aplikačnímu uživateli.
     *
     * Z kontextu nebo z vazby přes hráče se určuje cílový uživatel,
     * jeho jméno a kontaktní email. Samotná volba šablony je řízena
     * hodnotou NotificationType.
     */
    public EmailContent buildForUser(NotificationType type,
                                     PlayerEntity player,
                                     String userEmail,
                                     Object context) {

        AppUserEntity user = resolveUser(player, context);

        String safeUserEmail =
                userEmail != null
                        ? userEmail
                        : (user != null && user.getEmail() != null
                        ? user.getEmail()
                        : "(neznámý email)");

        String userName;
        if (user != null) {
            userName = fullUserName(user);
            if ("(neznámý uživatel)".equals(userName)
                    && !"(neznámý email)".equals(safeUserEmail)) {
                userName = safeUserEmail;
            }
        } else {
            userName = !"(neznámý email)".equals(safeUserEmail)
                    ? safeUserEmail
                    : "(neznámý uživatel)";
        }

        String playerName = fullPlayerName(player);
        String activationLink = resolveActivationLink(context);
        String greeting = "Dobrý den " + escape(userName) + ",";

        // logika switch zůstává beze změny
        return switch (type) {
            // ...
            default -> null;
        };
    }

    // Emailové zprávy pro hráče

    /**
     * Sestavuje emailovou zprávu určenou hráči.
     *
     * Používá se především pro notifikace spojené se zápasy,
     * registracemi, omluvami a změnami termínů.
     */
    public EmailContent buildForPlayer(NotificationType type,
                                       PlayerEntity player,
                                       String playerEmail,
                                       Object context) {

        // implementace beze změny
        return switch (type) {
            // ...
            default -> null;
        };
    }

    // Pomocné metody

    /**
     * Sestaví celé jméno uživatele v bezpečné podobě.
     */
    private String fullUserName(AppUserEntity user) {
        if (user == null) return "(neznámý uživatel)";
        String first = safe(user.getName());
        String last = safe(user.getSurname());
        String full = (first + " " + last).trim();
        if (full.isEmpty()) {
            return user.getEmail() != null ? user.getEmail() : "(neznámý uživatel)";
        }
        return full;
    }

    /**
     * Vrací zobrazitelné jméno hráče.
     */
    private String fullPlayerName(PlayerEntity player) {
        if (player == null) return "(neznámý hráč)";
        if (player.getFullName() != null && !player.getFullName().isBlank()) {
            return player.getFullName();
        }
        return "(beze jména)";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String formatMatchDateTime(MatchEntity match) {
        if (match == null || match.getDateTime() == null) {
            return "";
        }
        return match.getDateTime().format(MATCH_DATETIME_FORMATTER);
    }

    /**
     * Spočítá počet přihlášených hráčů k danému zápasu.
     */
    private long countRegisteredPlayers(MatchEntity match) {
        if (match == null || match.getId() == null) {
            return 0;
        }
        return registrationRepository.countByMatchIdAndStatus(
                match.getId(),
                PlayerMatchStatus.REGISTERED
        );
    }

    /**
     * Z kontextu určí aplikačního uživatele relevantního pro notifikaci.
     */
    private AppUserEntity resolveUser(PlayerEntity player, Object context) {
        if (context instanceof UserActivationContext uac && uac.user() != null) {
            return uac.user();
        }
        if (context instanceof ForgottenPasswordResetContext fprc && fprc.user() != null) {
            return fprc.user();
        }
        if (context instanceof AppUserEntity u) {
            return u;
        }
        if (player != null && player.getUser() != null) {
            return player.getUser();
        }
        return null;
    }

    /**
     * Z kontextu získá aktivační odkaz, pokud je k dispozici.
     */
    private String resolveActivationLink(Object context) {
        if (context instanceof UserActivationContext uac) {
            return uac.activationLink();
        }
        return null;
    }
}
