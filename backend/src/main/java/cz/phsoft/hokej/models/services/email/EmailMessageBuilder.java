package cz.phsoft.hokej.models.services.email;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builder pro generování textů emailových zpráv.
 * <p>
 * Odpovědnosti:
 * <ul>
 *     <li>sestavení subjectu a těla emailu pro daný {@link NotificationType},</li>
 *     <li>určení, zda jde o HTML nebo plain text email,</li>
 *     <li>centralizace všech emailových textů (DRY).</li>
 * </ul>
 * <p>
 * Třída neřeší:
 * <ul>
 *     <li>odesílání emailů ({@link EmailService}),</li>
 *     <li>oprávnění, validace, ani načítání entit.</li>
 * </ul>
 */
@Component
public class EmailMessageBuilder {

    private final MatchRegistrationRepository registrationRepository;

    public EmailMessageBuilder(MatchRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    /**
     * Holder na data emailu.
     *
     * @param subject předmět emailu
     * @param body    tělo emailu (plain text nebo HTML)
     * @param html    true = body je HTML, false = plain text
     */
    public record EmailContent(String subject, String body, boolean html) {
    }

    private static final DateTimeFormatter MATCH_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy  HH:mm", new Locale("cs", "CZ"));

    // ----------------------------------------------------
    // EMAILY PRO UŽIVATELE (AppUser)
    // ----------------------------------------------------

    /**
     * Sestaví email pro uživatele (AppUser) podle typu notifikace.
     *
     * @param type      typ notifikace
     * @param player    hráč, kterého se notifikace týká (přes něj se dostaneme k AppUser)
     * @param userEmail email uživatele (z NotificationDecision)
     * @param context   případný kontext (pro budoucí rozšíření)
     */
    public EmailContent buildForUser(NotificationType type,
                                     PlayerEntity player,
                                     String userEmail,
                                     Object context) {

        AppUserEntity user = player != null ? player.getUser() : null;
        String userName = fullUserName(user);
        String playerName = fullPlayerName(player);
        String safeUserEmail = userEmail != null ? userEmail : (user != null ? user.getEmail() : "(neznámý email)");

        return switch (type) {
            case PLAYER_CREATED -> new EmailContent(
                    "Hráč vytvořen",
                    """
                            Dobrý den %s,
                            
                            hráč %s byl úspěšně vytvořen, 
                            počkejte na schválení administrátorem.
                            
                            Budete o schválení informován e-mailem.
                            
                            Vaše kontaktní údaje:
                            - Jméno a příjmení: %s
                            - Email: %s
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(
                            userName,
                            playerName,
                            userName,
                            safeUserEmail
                    ),
                    false
            );
            case PLAYER_UPDATED -> new EmailContent(
                    "Hráč upraven",
                    """
                            Dobrý den %s,
                            
                            údaje hráče %s byly aktualizovány.
                            
                            Vaše kontaktní údaje:
                            - Jméno a příjmení: %s
                            - Email: %s
                            
                            Pokud jste změny neprováděl(a) vy, kontaktujte prosím administrátora.
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(
                            userName,
                            playerName,
                            userName,
                            safeUserEmail
                    ),
                    false
            );
            case PLAYER_APPROVED -> new EmailContent(
                    "Hráč schválen",
                    """
                            Dobrý den %s,
                            
                            hráč %s byl schválen administrátorem.
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(userName, playerName),
                    false
            );
            case PLAYER_REJECTED -> new EmailContent(
                    "Hráč zamítnut",
                    """
                            Dobrý den %s,
                            
                            hráč %s byl zamítnut administrátorem.
                            
                            V případě dotazů prosím kontaktujte administrátora.
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(userName, playerName),
                    false
            );
            case USER_UPDATED -> new EmailContent(
                    "Účet byl aktualizován",
                    """
                            Dobrý den %s,
                            
                            údaje vašeho účtu byly aktualizovány.
                            
                            Aktuální email účtu:
                            - %s
                            
                            Pokud jste změny neprováděl(a) vy, kontaktujte prosím administrátora.
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(userName, safeUserEmail),
                    false
            );
            case PASSWORD_RESET -> new EmailContent(
                    "Reset hesla",
                    """
                            Dobrý den %s,
                            
                            byl proveden reset vašeho hesla pro účet %s.
                            Pokud jste o něj nežádal(a), kontaktujte prosím administrátora.
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(userName, safeUserEmail),
                    false
            );
            case SECURITY_ALERT -> new EmailContent(
                    "Bezpečnostní upozornění",
                    """
                            Dobrý den %s,
                            
                            byla zaznamenána neobvyklá aktivita na vašem účtu (%s).
                            Pokud jste to nebyl(a) vy, doporučujeme okamžitě změnit heslo
                            a kontaktovat administrátora.
                            
                            S pozdravem
                            App Hokej – Stará Garda
                            """.formatted(userName, safeUserEmail),
                    false
            );

            // pro ostatní typy user-email neposíláme
            default -> null;
        };
    }

    // ----------------------------------------------------
    // EMAILY PRO HRÁČE (Player kontakt)
    // ----------------------------------------------------

    /**
     * Sestaví email pro hráče (Player kontakt) podle typu notifikace.
     *
     * @param type        typ notifikace
     * @param player      hráč
     * @param playerEmail email, na který se má posílat (z PlayerSettings / decision)
     * @param context     kontext – typicky MatchRegistrationEntity
     */
    public EmailContent buildForPlayer(NotificationType type,
                                       PlayerEntity player,
                                       String playerEmail,
                                       Object context) {

        String playerName = fullPlayerName(player);

        return switch (type) {
            case MATCH_REGISTRATION_CREATED -> {
                MatchRegistrationEntity reg =
                        castContext(context, MatchRegistrationEntity.class);
                if (reg == null) {
                    yield null;
                }

                MatchEntity match = reg.getMatch();
                String subject = "Potvrzení přihlášení na zápas";

                String formattedDateTime = formatMatchDateTime(match);
                long registeredCount = countRegisteredPlayers(match);
                int maxPlayers = match != null ? match.getMaxPlayers() : 0;

                String html =
                        """
                                <p>Dobrý den %s,</p>
                                <p>byl jste <strong>přihlášen</strong> na zápas - <strong>%s</strong>.</p><br>
                                <p>Aktuálně přihlášeno: <strong>%d hráčů / z %d míst zbývá %d míst</strong></p><br>
                                <p><p>
                                <p>Hráč: %s<br/>
                                Email: %s</p>
                                <p>Těšíme se na vás.<br/>App Hokej – Stará Garda.</p>
                                """.formatted(
                                escape(playerName),
                                escape(formattedDateTime),
                                registeredCount,
                                maxPlayers,
                                maxPlayers - registeredCount,
                                escape(playerName),
                                escape(playerEmail != null ? playerEmail : "")
                        );

                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_CANCELED -> {
                MatchRegistrationEntity reg =
                        castContext(context, MatchRegistrationEntity.class);
                if (reg == null) {
                    yield null;
                }

                MatchEntity match = reg.getMatch();
                String subject = "Odhlášení ze zápasu";

                String formattedDateTime = formatMatchDateTime(match);
                long registeredCount = countRegisteredPlayers(match);
                int maxPlayers = match != null ? match.getMaxPlayers() : 0;

                String html =
                        """
                        <p>Dobrý den %s,</p>
                            <p>byl jste <strong>odhlášen</strong> ze zápasu - <strong>%s</strong>.</p><br>
                           <p>Aktuálně přihlášeno: <strong>%d hráčů / z %d míst zbývá %d míst</strong></p><br>
                            <p><p>
                            <p>Hráč: %s<br/>
                            Email: %s</p>
                            <p>Mrzí nás že nepříjdete.<br/>App Hokej – Stará Garda.</p>
                            ""                                                         );
                                """.formatted(
                                playerName,
                                formattedDateTime,
                                registeredCount,
                                maxPlayers,
                                maxPlayers - registeredCount,
                                playerName,
                                playerEmail != null ? playerEmail : "(neuvedeno)"
                        );

                yield new EmailContent(subject, html, true);
            }
// TODO DODĚLAT DALŠÍ ZPRÁVY
            // další typy (MATCH_REGISTRATION_UPDATED, MATCH_REMINDER, ...) můžeš doplnit později
            default -> null;
        };
    }

    // ----------------------------------------------------
    // Pomocné metody
    // ----------------------------------------------------

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
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    @SuppressWarnings("unchecked")
    private <T> T castContext(Object context, Class<T> expected) {
        if (context == null) {
            return null;
        }
        if (!expected.isInstance(context)) {
            return null;
        }
        return (T) context;
    }

    private String formatMatchDateTime(MatchEntity match) {
        if (match == null || match.getDateTime() == null) {
            return "";
        }
        return match.getDateTime().format(MATCH_DATETIME_FORMATTER);
    }

    private long countRegisteredPlayers(MatchEntity match) {
        if (match == null || match.getId() == null) {
            return 0;
        }
        return registrationRepository.countByMatchIdAndStatus(
                match.getId(),
                PlayerMatchStatus.REGISTERED
        );
    }
}
