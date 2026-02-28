package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builder pro sestavování obsahu in-app notifikací.
 *
 * Třída centralizuje texty krátkých zpráv zobrazovaných v aplikaci
 * (notifikační badge, přehled posledních událostí, panel notifikací).
 *
 * Odpovědnost třídy:
 * - sestavení titulku a zprávy podle NotificationType,
 * - využití kontextu (hráč, zápas, změna času) pro doplnění detailů,
 * - udržení jednotného a stručného stylu in-app textů.
 *
 * Třída neřeší:
 * - uložení do databáze,
 * - výběr cílového uživatele,
 * - oprávnění ani validaci vstupů.
 */
@Component
public class InAppNotificationBuilder {

    private final MatchRegistrationRepository registrationRepository;

    public InAppNotificationBuilder(MatchRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    /**
     * Datový nosič pro obsah in-app notifikace.
     *
     * @param title   krátký titulek notifikace
     * @param message stručný text zprávy
     */
    public record InAppNotificationContent(String title, String message) {
    }

    /**
     * Formátovač data a času zápasu používaný v in-app notifikacích.
     */
    private static final DateTimeFormatter MATCH_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy HH:mm", new Locale("cs", "CZ"));

    /**
     * Sestaví in-app notifikaci pro daný typ události.
     *
     * Předpokládá se, že cílový AppUserEntity (uživatel) je již
     * vyřešen volající službou. Hráč a context se používají pro
     * doplnění detailů (jméno hráče, zápas, změna času).
     */
    public InAppNotificationContent build(NotificationType type,
                                          AppUserEntity user,
                                          PlayerEntity player,
                                          Object context) {

        String userName = fullUserName(user);
        String playerName = fullPlayerName(player);

        MatchEntity match = extractMatch(context);
        String formattedDateTime = formatMatchDateTime(match);
        long registeredCount = countRegisteredPlayers(match);
        int maxPlayers = match != null ? match.getMaxPlayers() : 0;
        long freeSlots = maxPlayers > 0 ? (maxPlayers - registeredCount) : 0;
        String matchCancelReason = assignMatchCancelReason(match);

        MatchRegistrationEntity registration = extractMatchRegistration(context);
        String excuseReason = assignExcuseReason(registration);
        String excuseNote = (registration != null)
                ? safe(registration.getExcuseNote())
                : "";

        return switch (type) {

            // =====================================
            // PLAYER – vazba hráče na uživatele
            // =====================================

            case PLAYER_CREATED -> {
                String title = "Hráč vytvořen";
                String message = "Hráč %s byl vytvořen a čeká na schválení administrátorem."
                        .formatted(playerName);
                yield new InAppNotificationContent(title, message);
            }

            case PLAYER_UPDATED -> {
                String title = "Hráč upraven";
                String message = "Údaje hráče %s byly aktualizovány."
                        .formatted(playerName);
                yield new InAppNotificationContent(title, message);
            }

            case PLAYER_APPROVED -> {
                String title = "Hráč schválen";
                String message = "Hráč %s byl schválen administrátorem."
                        .formatted(playerName);
                yield new InAppNotificationContent(title, message);
            }

            case PLAYER_REJECTED -> {
                String title = "Hráč zamítnut";
                String message = "Hráč %s byl zamítnut administrátorem."
                        .formatted(playerName);
                yield new InAppNotificationContent(title, message);
            }

            case PLAYER_CHANGE_USER -> {
                String title = "Hráč přiřazen novému uživateli";
                String message = "Hráč %s byl přiřazen jinému uživatelskému účtu."
                        .formatted(playerName);
                yield new InAppNotificationContent(title, message);
            }

            // =====================================
            // USER – události kolem uživatelského účtu
            // =====================================

            case USER_CREATED -> {
                String title = "Uživatel vytvořen";
                String message = "Byl vytvořen nový uživatelský účet pro %s."
                        .formatted(userName);
                yield new InAppNotificationContent(title, message);
            }

            case USER_ACTIVATED -> {
                String title = "Účet aktivován";
                String message = "Váš uživatelský účet byl úspěšně aktivován.";
                yield new InAppNotificationContent(title, message);
            }

            case USER_UPDATED -> {
                String title = "Účet aktualizován";
                String message = "Údaje vašeho účtu byly aktualizovány.";
                yield new InAppNotificationContent(title, message);
            }

            case PASSWORD_RESET -> {
                String title = "Reset hesla";
                String message = "Heslo k vašemu účtu bylo resetováno.";
                yield new InAppNotificationContent(title, message);
            }

            case FORGOTTEN_PASSWORD_RESET_REQUEST -> {
                String title = "Žádost o obnovení hesla";
                String message = "Byla přijata žádost o obnovení zapomenutého hesla k vašemu účtu.";
                yield new InAppNotificationContent(title, message);
            }

            case FORGOTTEN_PASSWORD_RESET_COMPLETED -> {
                String title = "Heslo změněno";
                String message = "Heslo k vašemu účtu bylo úspěšně změněno.";
                yield new InAppNotificationContent(title, message);
            }

            case SECURITY_ALERT -> {
                String title = "Bezpečnostní upozornění";
                String message = "Na vašem účtu byla zaznamenána neobvyklá aktivita.";
                yield new InAppNotificationContent(title, message);
            }

            // =====================================
            // REGISTRACE NA ZÁPAS
            // =====================================

            case MATCH_REGISTRATION_CREATED -> {
                String title = "Přihlášení na zápas";
                String message = formattedDateTime.isBlank()
                        ? "Byl jste přihlášen na zápas. Hráč: %s."
                        .formatted(playerName)
                        : "Byl jste přihlášen na zápas %s. Hráč: %s."
                        .formatted(formattedDateTime, playerName);
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_REGISTRATION_UPDATED -> {
                MatchRegistrationEntity reg =
                        castContext(context, MatchRegistrationEntity.class);
                String newStatus = reg != null && reg.getStatus() != null
                        ? reg.getStatus().name()
                        : "neznámý stav";

                String title = "Registrace aktualizována";
                String message = formattedDateTime.isBlank()
                        ? "Vaše registrace na zápas byla změněna (%s)."
                        .formatted(newStatus)
                        : "Vaše registrace na zápas %s byla změněna (%s)."
                        .formatted(formattedDateTime, newStatus);
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_REGISTRATION_CANCELED -> {
                String title = "Odhlášení ze zápasu";
                String reasonPart = !excuseReason.isBlank()
                        ? " Důvod: %s - %s.".formatted(excuseReason, excuseNote)
                        : "";
                String message = formattedDateTime.isBlank()
                        ? "Byl jste odhlášen ze zápasu.%s"
                        .formatted(reasonPart)
                        : "Byl jste odhlášen ze zápasu %s.%s"
                        .formatted(formattedDateTime, reasonPart);
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_REGISTRATION_RESERVED -> {
                String title = "Přesunut mezi náhradníky";
                String messageBase = formattedDateTime.isBlank()
                        ? "Byl jste přesunut mezi náhradníky pro zápas."
                        : "Byl jste přesunut mezi náhradníky pro zápas %s."
                        .formatted(formattedDateTime);

                String capacityPart = (maxPlayers > 0)
                        ? " Kapacita zápasu: %d hráčů.".formatted(maxPlayers)
                        : "";

                String message = messageBase + capacityPart;
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_REGISTRATION_SUBSTITUTE -> {
                String title = "Možná účast (SUBSTITUTE)";
                String messageBase = formattedDateTime.isBlank()
                        ? "Vaše registrace na zápas je nastavena jako ‚možná‘."
                        : "Vaše registrace na zápas %s je nastavena jako ‚možná‘."
                        .formatted(formattedDateTime);

                String capacityPart = (maxPlayers > 0)
                        ? " Kapacita zápasu: %d hráčů.".formatted(maxPlayers)
                        : "";

                String message = messageBase + capacityPart;
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_WAITING_LIST_MOVED_UP -> {
                String title = "Přesun z čekací listiny";
                String freeSlotsPart = (maxPlayers > 0)
                        ? " Volná místa: %d z %d.".formatted(freeSlots, maxPlayers)
                        : "";
                String message = formattedDateTime.isBlank()
                        ? "Byl jste přesunut z čekací listiny mezi přihlášené hráče.%s"
                        .formatted(freeSlotsPart)
                        : "Byl jste přesunut z čekací listiny mezi přihlášené na zápas %s.%s"
                        .formatted(formattedDateTime, freeSlotsPart);
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_REGISTRATION_NO_RESPONSE -> {
                String title = "Bez reakce na zápas";
                String base = formattedDateTime.isBlank()
                        ? "Dosud jste nereagoval na zápas."
                        : "Dosud jste nereagoval na zápas %s."
                        .formatted(formattedDateTime);

                String countPart = (maxPlayers > 0)
                        ? " Přihlášeno: %d hráčů, volná místa: %d z %d."
                        .formatted(registeredCount, freeSlots, maxPlayers)
                        : "";

                String message = base + countPart;
                yield new InAppNotificationContent(title, message);
            }

            // =====================================
            // EXCUSE – omluvy a neomluvené absence
            // =====================================

            case PLAYER_EXCUSED -> {
                String title = "Omluva ze zápasu";
                String reasonPart = !excuseReason.isBlank()
                        ? " Důvod: %s - %s.".formatted(excuseReason, excuseNote)
                        : "";
                String message = formattedDateTime.isBlank()
                        ? "Vaše omluva ze zápasu byla zaznamenána.%s"
                        .formatted(reasonPart)
                        : "Vaše omluva ze zápasu %s byla zaznamenána.%s"
                        .formatted(formattedDateTime, reasonPart);
                yield new InAppNotificationContent(title, message);
            }

            case PLAYER_NO_EXCUSED -> {
                String title = "Neomluvená neúčast";
                String message = formattedDateTime.isBlank()
                        ? "Byl jste označen jako neomluvený na zápas."
                        : "Byl jste označen jako neomluvený na zápas %s."
                        .formatted(formattedDateTime);
                yield new InAppNotificationContent(title, message);
            }

            // =====================================
            // MATCH_INFO – informace o zápase
            // =====================================

            case MATCH_REMINDER -> {
                String title = "Připomenutí zápasu";
                String base = formattedDateTime.isBlank()
                        ? "Připomínáme vám nadcházející zápas."
                        : "Připomínáme vám nadcházející zápas %s."
                        .formatted(formattedDateTime);

                String countPart = (maxPlayers > 0)
                        ? " Přihlášeno: %d hráčů, volná místa: %d z %d."
                        .formatted(registeredCount, freeSlots, maxPlayers)
                        : "";

                String message = base + countPart;
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_CANCELED -> {
                String title = "Zápas zrušen";
                String reasonPart = !matchCancelReason.isBlank()
                        ? " Důvod: %s.".formatted(matchCancelReason)
                        : "";
                String message = formattedDateTime.isBlank()
                        ? "Plánovaný zápas byl zrušen.%s"
                        .formatted(reasonPart)
                        : "Zápas %s byl zrušen.%s"
                        .formatted(formattedDateTime, reasonPart);
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_UNCANCELED -> {
                String title = "Zápas obnoven";
                String message = formattedDateTime.isBlank()
                        ? "Původně zrušený zápas byl obnoven."
                        : "Původně zrušený zápas %s byl obnoven."
                        .formatted(formattedDateTime);
                yield new InAppNotificationContent(title, message);
            }

            case MATCH_TIME_CHANGED -> {
                String title = "Změna data/času zápasu";

                LocalDateTime oldDateTime = null;
                if (context instanceof MatchTimeChangeContext mtc) {
                    oldDateTime = mtc.oldDateTime();
                }

                String oldDateFormatted = "";
                if (oldDateTime != null) {
                    oldDateFormatted = oldDateTime.format(MATCH_DATETIME_FORMATTER);
                }

                String newPart = formattedDateTime.isBlank()
                        ? "Došlo ke změně data/času plánovaného zápasu."
                        : "Došlo ke změně data/času zápasu na nový termín %s."
                        .formatted(formattedDateTime);

                String oldPart = oldDateFormatted.isBlank()
                        ? ""
                        : " Původní termín: %s.".formatted(oldDateFormatted);

                String message = newPart + oldPart;
                yield new InAppNotificationContent(title, message);
            }

            // pro ostatní typy in-app notifikaci nesestavujeme
            default -> null;
        };
    }

    // Pomocné metody

    /**
     * Sestaví zobrazitelné jméno uživatele.
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
     * Z kontextu určí zápas relevantní pro notifikaci.
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

    // pomocná metoda pro důvod zrušení zápasu
    /**
     * Vrací čitelný popis důvodu zrušení zápasu.
     * Pokud není důvod nastaven, vrací prázdný řetězec.
     */
    private String assignMatchCancelReason(MatchEntity match) {
        if (match == null || match.getCancelReason() == null) {
            return "";
        }

        return switch (match.getCancelReason()) {
            case NOT_ENOUGH_PLAYERS -> "nedostatečný počet hráčů";
            case TECHNICAL_ISSUE -> "Technické problémy (led, hala…)";
            case WEATHER -> "Nepříznivé počasí";
            case ORGANIZER_DECISION -> "Rozhodnutí organizátora";
            case OTHER -> "Jiný důvod";
            default -> "neznámý důvod";
        };
    }
    /**
     * Vrací čitelný popis důvodu omluvy ze zápasu.
     * Pokud není důvod nastaven, vrací prázdný řetězec.
     */
    private String assignExcuseReason(MatchRegistrationEntity registration) {
        if (registration == null || registration.getExcuseReason() == null) {
            return "";
        }

        return switch (registration.getExcuseReason()) {
            case NEMOC -> "nemoc";
            case PRACE -> "pracovní povinnosti";
            case NECHE_SE_MI -> "nechce se mi";
            case JINE -> "jiný důvod";
            default -> "neznámý důvod";
        };
    }
}