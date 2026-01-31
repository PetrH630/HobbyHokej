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
    // EMAILY PRO MANAŽERA (kopie zprávy pro user/player)
    // ----------------------------------------------------

    /**
     * Vytvoří kopii emailu pro manažera:
     * <ul>
     *     <li>nejdřív se pokusí použít šablonu pro USER ({@link #buildForUser}),</li>
     *     <li>když není, použije šablonu pro PLAYER ({@link #buildForPlayer}),</li>
     *     <li>subject se prefixuje "[Kopie pro manažera]",</li>
     *     <li>tělo se obalí hlavičkou "Zpráva pro manažera – {jméno manažera}".</li>
     * </ul>
     */
    public EmailContent buildForManager(NotificationType type,
                                        PlayerEntity player,
                                        AppUserEntity manager,
                                        Object context) {

        if (manager == null) {
            return null;
        }

        String managerName = fullUserName(manager);

        // 1) Zkusíme použít email, který by šel uživateli (AppUser)
        EmailContent base = buildForUser(type, player, null, context);

        // 2) Když není definovaný pro uživatele, zkusíme email, který by šel hráči
        if (base == null) {
            base = buildForPlayer(type, player, null, context);
        }

        // 3) Pro daný typ není definována žádná šablona ani pro user, ani pro player → manažer nic nedostane
        if (base == null) {
            return null;
        }

        // 4) Subjekt – přidáme prefix
        String subject = "[Kopie pro manažera] " + base.subject();

        // 5) Tělo – obalíme původní tělo hlavičkou "Zpráva pro manažera ..."
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

    // ----------------------------------------------------
    // EMAILY PRO UŽIVATELE (AppUser)
    // ----------------------------------------------------

    /**
     * Sestaví email pro uživatele (AppUser) podle typu notifikace.
     *
     * @param type      typ notifikace
     * @param player    hráč, kterého se notifikace týká (přes něj se dostaneme k AppUser)
     * @param userEmail email uživatele (z NotificationDecision nebo přímo z AppUser)
     * @param context   kontext (např. {@link UserActivationContext} nebo {@link ForgottenPasswordResetContext} nebo přímo AppUserEntity)
     */
    public EmailContent buildForUser(NotificationType type,
                                     PlayerEntity player,
                                     String userEmail,
                                     Object context) {

        // vytáhneme AppUserEntity z kontextu / hráče
        AppUserEntity user = resolveUser(player, context);

        // bezpečný email – z parametru, nebo z usera, nebo placeholder
        String safeUserEmail =
                userEmail != null
                        ? userEmail
                        : (user != null && user.getEmail() != null
                        ? user.getEmail()
                        : "(neznámý email)");

        // jméno uživatele – pokud není, použijeme email jako fallback
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

        // případný aktivační odkaz (použijeme u USER_CREATED)
        String activationLink = resolveActivationLink(context);
        String greeting = "Dobrý den " + escape(userName) + ",";

        return switch (type) {

            // =====================================
            // PLAYER – vazba hráče na uživatele
            // =====================================
            case PLAYER_CREATED -> {
                String subject = "Hráč vytvořen";

                String main = """
                        <p>hráč <strong>%s</strong> byl úspěšně vytvořen.</p>
                        <p>Počkejte prosím na schválení administrátorem.</p>
                        <p>Budete o schválení informován e-mailem.</p>
                        <p>Vaše kontaktní údaje:</p>
                        <ul>
                            <li>Jméno a příjmení: %s</li>
                            <li>Email: %s</li>
                        </ul>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(playerName),
                        escape(userName),
                        escape(safeUserEmail)
                );

                String footer = "Tento e-mail byl vygenerován automaticky, neodpovídejte prosím na něj.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case PLAYER_UPDATED -> {
                String subject = "Hráč upraven";

                String main = """
                        <p>údaje hráče <strong>%s</strong> byly aktualizovány.</p>
                        <p>Vaše kontaktní údaje:</p>
                        <ul>
                            <li>Jméno a příjmení: %s</li>
                            <li>Email: %s</li>
                        </ul>
                        <p>Pokud jste změny neprováděl(a) vy, kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(playerName),
                        escape(userName),
                        escape(safeUserEmail)
                );

                String footer = "Pokud jste změnu neprováděl(a) vy, kontaktujte prosím administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case PLAYER_APPROVED -> {
                String subject = "Hráč schválen";

                String main = """
                        <p>hráč <strong>%s</strong> byl schválen administrátorem.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(playerName)
                );

                String footer = "Tento e-mail má pouze informační charakter.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case PLAYER_REJECTED -> {
                String subject = "Hráč zamítnut";

                String main = """
                        <p>hráč <strong>%s</strong> byl zamítnut administrátorem.</p>
                        <p>V případě dotazů prosím kontaktujte administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(playerName)
                );

                String footer = "Pokud máte otázky k zamítnutí hráče, kontaktujte administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case PLAYER_CHANGE_USER -> {
                String subject = "Hráč přiřazen novému uživateli";

                String main = """
                        <p>hráč <strong>%s</strong> byl přiřazen novému uživateli.</p>
                        <p>Nový uživatel:</p>
                        <ul>
                            <li>Jméno a příjmení: %s</li>
                            <li>Email: %s</li>
                        </ul>
                        <p>Pokud jste změny neprováděl(a) vy, kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(playerName),
                        escape(userName),
                        escape(safeUserEmail)
                );

                String footer = "Pokud jste změnu neprováděl(a) vy, kontaktujte prosím administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            // =====================================
            // USER – události kolem uživatelského účtu
            // =====================================

            case USER_CREATED -> {
                String subject = "Uživatel vytvořen";

                // blok s aktivačním odkazem, pokud existuje
                String activationBlock = "";
                if (activationLink != null && !activationLink.isBlank()) {
                    activationBlock = """
                            <p>Pro dokončení registrace klikněte na následující odkaz:</p>
                            <p><a href="%1$s">%1$s</a></p>
                            <p>Odkaz je platný 24 hodin.</p>
                            """.formatted(escape(activationLink));
                }

                String main = """
                        <p>byl pro vás vytvořen uživatelský účet v aplikaci <strong>Hokej – Stará Garda</strong>.</p>
                        <p>Přihlašovací email:</p>
                        <ul>
                            <li>%s</li>
                        </ul>
                        %s
                        <p>Pokud jste o vytvoření účtu nežádal(a), kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(safeUserEmail),
                        activationBlock
                );

                String footer = "Pokud jste o vytvoření účtu nežádal(a), neprodleně kontaktujte administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case USER_ACTIVATED -> {
                String subject = "Účet byl aktivován";

                String main = """
                        <p>váš uživatelský účet byl úspěšně <strong>aktivován</strong>.</p>
                        <p>Nyní se můžete přihlásit do aplikace Hokej – Stará Garda a spravovat své hráče a přihlášky na zápasy.</p>
                        <p>Přihlašovací email:</p>
                        <ul>
                            <li>%s</li>
                        </ul>
                        <p>Pokud jste o aktivaci účtu nežádal(a), kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(safeUserEmail)
                );

                String footer = "Pokud jste o aktivaci účtu nežádal(a), změňte si heslo a kontaktujte administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case USER_UPDATED -> {
                String subject = "Účet byl aktualizován";

                String main = """
                        <p>údaje vašeho účtu byly <strong>aktualizovány</strong>.</p>
                        <p>Aktuální email účtu:</p>
                        <ul>
                            <li>%s</li>
                        </ul>
                        <p>Pokud jste změny neprováděl(a) vy, kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(safeUserEmail)
                );

                String footer = "Pokud jste změny neprováděl(a) vy, co nejdříve změňte heslo a kontaktujte administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case PASSWORD_RESET -> {
                String subject = "Reset hesla";

                String main = """
                        <p>byl proveden <strong>reset vašeho hesla</strong> pro účet %s.</p>
                        <p>Pokud jste o reset hesla nežádal(a), kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(safeUserEmail)
                );

                String footer = "Pokud jste o reset hesla nežádal(a), ihned kontaktujte administrátora a zkontrolujte zabezpečení účtu.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case FORGOTTEN_PASSWORD_RESET_REQUEST -> {
                ForgottenPasswordResetContext ctx =
                        castContext(context, ForgottenPasswordResetContext.class);

                String resetLink = ctx != null ? ctx.resetLink() : null;

                String subject = "Obnovení zapomenutého hesla";

                String linkBlock = "";
                if (resetLink != null && !resetLink.isBlank()) {
                    linkBlock = """
                            <p>Pro nastavení nového hesla klikněte na následující odkaz:</p>
                            <p><a href="%1$s">%1$s</a></p>
                            <p>Odkaz je platný po omezenou dobu. Pokud vyprší, požádejte prosím o nový reset hesla.</p>
                            """.formatted(escape(resetLink));
                }

                String main = """
                        <p>obdrželi jsme žádost o <strong>obnovení zapomenutého hesla</strong> k vašemu účtu v aplikaci <strong>Hokej – Stará Garda</strong>.</p>
                        %s
                        <p>Pokud jste o obnovení hesla nežádal(a) vy, můžete tento e-mail ignorovat.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(linkBlock);

                String footer = "Pokud jste o reset hesla nežádal(a), zvažte prosím změnu hesla k vašemu e-mailu a zkontrolujte zabezpečení účtů.";

                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case FORGOTTEN_PASSWORD_RESET_COMPLETED -> {
                String subject = "Heslo bylo úspěšně změněno";

                String main = """
                        <p>vaše heslo bylo <strong>úspěšně změněno</strong> na základě žádosti o obnovení zapomenutého hesla.</p>
                        <p>Pokud jste tuto změnu neprováděl(a) vy, neprodleně kontaktujte administrátora a změňte své heslo.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """;

                String footer = "Pro zvýšení bezpečnosti doporučujeme používat silné heslo a nepoužívat stejné heslo pro více služeb.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case SECURITY_ALERT -> {
                String subject = "Bezpečnostní upozornění";

                String main = """
                        <p>byla zaznamenána <strong>neobvyklá aktivita</strong> na vašem účtu (%s).</p>
                        <p>Pokud jste to nebyl(a) vy, doporučujeme okamžitě změnit heslo a kontaktovat administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(safeUserEmail)
                );

                String footer = "Pro zvýšení bezpečnosti doporučujeme používat silné heslo a dvoufaktorové ověření (pokud je dostupné).";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            // pro ostatní typy user-email neposíláme
            default -> null;
        };
    }

    // ----------------------------------------------------
    // EMAILY PRO HRÁČE (Player kontakt)
    // ----------------------------------------------------

    public EmailContent buildForPlayer(NotificationType type,
                                       PlayerEntity player,
                                       String playerEmail,
                                       Object context) {

        // Nový uživatel při PLAYER_CHANGE_USER – ošetřeno proti null
        AppUserEntity user = (player != null) ? player.getUser() : null;
        String newUserFullName;
        String newUserEmail;

        if (user != null) {
            String first = safe(user.getName());
            String last = safe(user.getSurname());
            String full = (first + " " + last).trim();
            if (!full.isEmpty()) {
                newUserFullName = full;
            } else if (user.getEmail() != null && !user.getEmail().isBlank()) {
                newUserFullName = user.getEmail();
            } else {
                newUserFullName = "(neznámý uživatel)";
            }
            newUserEmail = (user.getEmail() != null && !user.getEmail().isBlank())
                    ? user.getEmail()
                    : "(neuvedeno)";
        } else {
            newUserFullName = "(neznámý uživatel)";
            newUserEmail = "(neuvedeno)";
        }

        // Registrace z contextu – může být null, proto safe přístup
        MatchRegistrationEntity registration = extractMatchRegistration(context);
        String excuseReason = (registration != null && registration.getExcuseReason() != null)
                ? registration.getExcuseReason().toString()
                : "";
        String excuseNote = (registration != null)
                ? safe(registration.getExcuseNote())
                : "";

        String playerName = fullPlayerName(player);
        String greeting = "Dobrý den " + escape(playerName) + ",";
        MatchEntity match = extractMatch(context);
        String formattedDateTime = formatMatchDateTime(match);
        long registeredCount = countRegisteredPlayers(match);
        int maxPlayers = match != null ? match.getMaxPlayers() : 0;
        long freeSlots = maxPlayers > 0 ? (maxPlayers - registeredCount) : 0;
        String matchCancelReason =
                (match != null && match.getCancelReason() != null)
                        ? match.getCancelReason().toString()
                        : "";

        String safeEmail = (playerEmail != null && !playerEmail.isBlank())
                ? playerEmail
                : "(neuvedeno)";

        return switch (type) {

            // =====================================
            // REGISTRATION
            // =====================================
            case PLAYER_CHANGE_USER -> {
                String subject = "Hráč přiřazen novému uživateli";

                String main = """
                        <p>hráč <strong>%s</strong> byl přiřazen novému uživateli.</p>
                        <p>Nový uživatel:</p>
                        <ul>
                            <li>Jméno a příjmení: %s</li>
                            <li>Email: %s</li>
                        </ul>
                        <p>Pokud jste změny neprováděl(a) vy, kontaktujte prosím administrátora.</p>
                        <p>S pozdravem<br/>App Hokej – Stará Garda</p>
                        """.formatted(
                        escape(playerName),
                        escape(newUserFullName),
                        escape(newUserEmail)
                );

                String footer = "Pokud jste změnu neprováděl(a) vy, kontaktujte prosím administrátora.";
                String html = buildSimpleHtml(subject, greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_CREATED -> {
                String subject = "Potvrzení přihlášení na zápas";

                String main = """
                        <p>byl jste <strong>přihlášen</strong> na zápas.</p>
                        %s
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        <p>Hráč: %s<br/>
                        Email: %s</p>
                        <p>Těšíme se na vás.<br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / z %d míst zbývá %d míst", maxPlayers, freeSlots))
                                : "",
                        escape(playerName),
                        escape(safeEmail)
                );

                String footer = "Tento e-mail byl vygenerován automaticky, neodpovídejte prosím na něj.";
                String html = buildSimpleHtml("Přihlášení na zápas", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_UPDATED -> {
                MatchRegistrationEntity reg =
                        castContext(context, MatchRegistrationEntity.class);
                String newStatus = reg != null && reg.getStatus() != null
                        ? reg.getStatus().name()
                        : "neznámý";

                String subject = "Aktualizace registrace na zápas";

                String main = """
                        <p>vaše registrace na zápas byla <strong>aktualizována</strong>.</p>
                        %s
                        <p>Aktuální stav vaší registrace: <strong>%s</strong></p>
                        <p>Hráč: %s<br/>
                        Email: %s</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        escape(newStatus),
                        escape(playerName),
                        escape(safeEmail)
                );

                String footer = "Pokud jste změnu neprováděl(a) vy, kontaktujte prosím manažera nebo administrátora.";
                String html = buildSimpleHtml("Aktualizace registrace", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_CANCELED -> {
                String subject = "Odhlášení ze zápasu";

                String main = """
                        <p>byl jste <strong>odhlášen</strong> ze zápasu.</p>
                        %s
                        <p>Důvod: %s</p>
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        <p>Hráč: %s<br/>
                        Email: %s</p>
                        <p>Mrzí nás, že nepřijdete.</p>
                        <p>Těšíme se na vás.<br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        !excuseReason.isBlank()
                                ? escape(String.format("  %s - %s", excuseReason, excuseNote))
                                : "",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / z %d míst zbývá %d míst", maxPlayers, freeSlots))
                                : "",
                        escape(playerName),
                        escape(safeEmail)
                );

                String footer = "V případě, že se situace změní, můžete se na zápas zkusit znovu přihlásit, pokud bude volné místo.";
                String html = buildSimpleHtml("Odhlášení ze zápasu", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_RESERVED -> {
                String subject = "Přesunutí mezi náhradníky";

                String main = """
                        <p>byl jste <strong>přesunut mezi náhradníky</strong> pro tento zápas.</p>
                        %s
                        <p>k tomuto došlo v důsledku snížení kapacity - maximálního počtu hráčů</p>
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        <p>Pokud se uvolní místo, budete automaticky přesunut mezi přihlášené hráče a obdržíte další e-mail.</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / kapacita %d hráčů", maxPlayers))
                                : ""
                );

                String footer = "Status náhradníka znamená, že zatím nejste v hlavní sestavě, ale můžete být dodatečně potvrzen.";
                String html = buildSimpleHtml("Přesunutí mezi náhradníky", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_SUBSTITUTE -> {
                String subject = "Registrace – možná účast (SUBSTITUTE)";

                String main = """
                        <p>vaše registrace na zápas je nastavena jako <strong>„možná“ (SUBSTITUTE)</strong>.</p>
                        %s
                        <p>Můžete se kdykoliv přihlásit nebo omluvit.</p>
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / kapacita %d hráčů", maxPlayers))
                                : ""
                );

                String footer = "Tento e-mail byl vygenerován automaticky, neodpovídejte prosím na něj.";
                String html = buildSimpleHtml("Možná účast na zápase", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_WAITING_LIST_MOVED_UP -> {
                String subject = "Přihlášení na zápas z náhradníků";

                String main = """
                        <p>dobrá zpráva – byli jste <strong>přesunut</strong> z čekací listiny mezi <strong>přihlášené hráče</strong>.</p>
                        %s
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        <p>Těšíme se na vás.</p><br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / volná místa: %d z %d", freeSlots, maxPlayers))
                                : ""
                );

                String footer = "Pokud se nemůžete zúčastnit, prosím co nejdříve se odhlaste, aby se uvolnilo místo pro dalšího hráče.";
                String html = buildSimpleHtml("Přesun z čekací listiny", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_REGISTRATION_NO_RESPONSE -> {
                String subject = "Připomenutí – nereagoval jste na zápas";

                String main = """
                        <p>zatím jste <strong>nereagoval</strong> na zápas.</p>
                        %s
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        <p>Prosíme, potvrďte co nejdříve, zda se zápasu zúčastníte, aby bylo možné sestavit týmy.</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / volná místa: %d z %d", freeSlots, maxPlayers))
                                : ""
                );

                String footer = "Registraci můžete změnit po přihlášení do aplikace Hokej – Stará Garda.";
                String html = buildSimpleHtml("Nereagovaná pozvánka", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            // =====================================
            // EXCUSE
            // =====================================

            case PLAYER_EXCUSED -> {
                String subject = "Omluva ze zápasu potvrzena";

                String main = """
                        <p>vaše <strong>omluva ze zápasu</strong> byla zaznamenána.</p>
                        %s
                        <p>Důvod: %s</p>
                        <p>Děkujeme, že dáváte vědět včas.</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        !excuseReason.isBlank()
                                ? escape(String.format("  %s - %s", excuseReason, excuseNote))
                                : ""
                );

                String footer = "Omluva pomáhá lépe plánovat sestavu na zápas.";
                String html = buildSimpleHtml("Omluva ze zápasu", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case PLAYER_NO_EXCUSED -> {
                String subject = "Neomluvená neúčast na zápase";

                String main = """
                        <p>byl jste označen jako <strong>neomluvený</strong> na zápas.</p>
                        %s
                        <p>Pokud se jedná o nedorozumění, kontaktujte prosím manažera nebo administrátora.</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>"
                );

                String footer = "Opakované neomluvené absence mohou ovlivnit vaši prioritu při sestavování týmů.";
                String html = buildSimpleHtml("Neomluvená neúčast", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            // =====================================
            // MATCH_INFO
            // =====================================

            case MATCH_REMINDER -> {
                String subject = "Připomenutí zápasu";

                String pricePerPlayer =
                        (match != null && registeredCount > 0 && match.getPrice() != null)
                                ? escape(String.format("%d", match.getPrice() / registeredCount))
                                : "";

                String main = """
                        <p>připomínáme vám nadcházející <strong>zápas</strong>.</p>
                        %s
                        <p>Aktuálně přihlášeno: <strong>%d hráčů</strong>%s</p>
                        %s
                        <p>Prosíme, dorazte včas.</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        registeredCount,
                        maxPlayers > 0
                                ? escape(String.format(" / volná místa: %d z %d", freeSlots, maxPlayers))
                                : "",
                        !pricePerPlayer.isBlank()
                                ? "<p>Zatím je cena za hráče: " + pricePerPlayer + " Kč.</p>"
                                : ""
                );

                String footer = "Pokud se nemůžete zúčastnit, co nejdříve se prosím odhlaste v aplikaci.";
                String html = buildSimpleHtml("Připomenutí zápasu", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_CANCELED -> {
                String subject = "Zápas zrušen";

                String main = """
                        <p>omlouváme se, ale plánovaný <strong>zápas byl zrušen</strong>.</p>
                        %s
                        <p>Důvod: %s</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín původně plánovaného zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        !matchCancelReason.isBlank()
                                ? escape(matchCancelReason)
                                : "Důvod neuveden"
                );

                String footer = "Děkujeme za pochopení. O případném náhradním termínu budete informováni.";
                String html = buildSimpleHtml("Zápas zrušen", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_UNCANCELED -> {
                String subject = "Zápas obnoven";

                String main = """
                        <p>Původně zrušený <strong>zápas byl obnoven</strong>.</p>
                        %s
                        <p>Zkontrolujte si prosím vaši registraci k zápasu v aplikaci, </p>
                        <p>kde ji můžete případně ještě změnit</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Termín původně plánovaného zápasu:</strong> " + escape(formattedDateTime) + "</p>"
                );

                String footer = "Děkujeme za pochopení. O případném náhradním termínu budete informováni.";
                String html = buildSimpleHtml("Zápas obnoven", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            case MATCH_TIME_CHANGED -> {
                String subject = "Změna času nebo data zápasu";

                LocalDateTime oldDateTime = null;
                if (context instanceof MatchTimeChangeContext mtc) {
                    oldDateTime = mtc.oldDateTime();
                }

                String oldDateFormatted = "";
                if (oldDateTime != null) {
                    oldDateFormatted = oldDateTime.format(MATCH_DATETIME_FORMATTER);
                }

                String main = """
                        <p>došlo ke <strong>změně času</strong> plánovaného zápasu.</p>
                        %s
                        %s
                        <p>Prosíme, zkontrolujte si nový termín v aplikaci a případně upravte svou účast.</p>
                        <br/>App Hokej – Stará Garda.</p>
                        """.formatted(
                        formattedDateTime.isBlank()
                                ? ""
                                : "<p><strong>Nový termín zápasu:</strong> " + escape(formattedDateTime) + "</p>",
                        oldDateFormatted.isBlank()
                                ? ""
                                : "<p><strong>Původní termín:</strong> " + escape(oldDateFormatted) + "</p>"
                );

                String footer = "Změna času může být způsobena úpravou rozpisu ledu nebo jinými organizačními důvody.";
                String html = buildSimpleHtml("Změna času zápasu", greeting, main, footer);
                yield new EmailContent(subject, html, true);
            }

            default -> null;
        };
    }

    // ----------------------------------------------------
    // pomocné metody
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

    private String buildSimpleHtml(String title,
                                   String greeting,
                                   String mainBody,
                                   String footer) {

        return """
                <!doctype html>
                <html lang="cs">
                <head>
                    <meta charset="utf-8">
                    <title>%s</title>
                </head>
                <body style="font-family: arial, sans-serif; font-size: 14px; color: #333333;">
                    <p>%s</p>
                    %s
                    <hr>
                    <p style="font-size: 12px; color: #777777;">%s</p>
                </body>
                </html>
                """.formatted(
                escape(title),
                greeting,
                mainBody,
                footer
        );
    }

    /**
     * Z kontextu vytáhne MatchEntity – buď přímo,
     * nebo z MatchRegistrationEntity.
     */
    private MatchEntity extractMatch(Object context) {
        if (context instanceof MatchRegistrationEntity reg) {
            return reg.getMatch();
        }
        if (context instanceof MatchEntity match) {
            return match;
        }
        if (context instanceof MatchTimeChangeContext mtc) {
            return mtc.match();
        }
        return null;
    }

    private MatchRegistrationEntity extractMatchRegistration(Object context) {
        if (context instanceof MatchRegistrationEntity reg) {
            return reg;
        }
        return null;
    }

    /**
     * Zjistí AppUserEntity:
     * 1) z UserActivationContext (USER_CREATED / USER_ACTIVATED),
     * 2) z ForgottenPasswordResetContext (forgotten password reset),
     * 3) přímo z AppUserEntity v contextu,
     * 4) nebo z player.getUser().
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
     * Zjistí aktivační odkaz z UserActivationContext (pokud je kontext tohoto typu).
     */
    private String resolveActivationLink(Object context) {
        if (context instanceof UserActivationContext uac) {
            return uac.activationLink();
        }
        return null;
    }
}
