package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.user.entities.AppUserEntity;
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

    /**
     * Repository pro práci s registracemi hráčů na zápasy.
     *
     * Používá se pro zjišťování agregovaných informací, například
     * počtu přihlášených hráčů na konkrétní zápas.
     */
    private final MatchRegistrationRepository registrationRepository;

    /**
     * Vytváří instanci builderu in-app notifikací.
     *
     * Repository je předáváno z kontextu Springu a používá se
     * pro dotazy nad registracemi k zápasům.
     *
     * @param registrationRepository repository pro práci s registracemi na zápasy
     */
    public InAppNotificationBuilder(MatchRegistrationRepository registrationRepository) {
        this.registrationRepository = registrationRepository;
    }

    /**
     * Datový nosič pro obsah in-app notifikace.
     *
     * Slouží jako jednoduchá struktura pro předání titulku a textu
     * zprávy do vrstvy, která notifikaci zobrazí uživateli.
     *
     * @param title   krátký titulek notifikace
     * @param message stručný text zprávy
     */
    public record InAppNotificationContent(String title, String message) {
    }

    /**
     * Formátovač data a času zápasu používaný v in-app notifikacích.
     *
     * Formátuje datum a čas v české lokalizaci včetně názvu dne
     * a přesného času, aby byla notifikace pro uživatele srozumitelná.
     */
    private static final DateTimeFormatter MATCH_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy HH:mm", new Locale("cs", "CZ"));

    /**
     * Sestaví obsah in-app notifikace pro daný typ události.
     *
     * Typ notifikace určuje strukturu a text zprávy. Uživatel a hráč
     * slouží jako základní kontext pro zobrazení jmen, objekt context
     * může obsahovat další relevantní údaje (například zápas, registraci
     * nebo informaci o změně času).
     *
     * Předpokládá se, že cílový AppUserEntity (uživatel) je již
     * určen volající službou. Metoda pouze provádí sestavení textu
     * notifikace na základě dostupných údajů.
     *
     * @param type    typ notifikace určující formu a obsah zprávy
     * @param user    uživatel, kterému je notifikace určena
     * @param player  hráč, kterého se notifikace týká
     * @param context doplňující kontext (zápas, registrace, změna času)
     * @return sestavený obsah in-app notifikace nebo null, pokud se pro daný typ nevytváří
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
        String playerPositionInMatch = assignPositionInMatch(registration);

        return switch (type) {


            // PLAYER – vazba hráče na uživatele


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


            // USER – události kolem uživatelského účtu


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


            // REGISTRACE NA ZÁPAS


            case MATCH_REGISTRATION_CREATED -> {
                String title = "Přihlášení na zápas";
                String message = formattedDateTime.isBlank()
                        ? "Byl jste přihlášen na zápas jako - %s. Hráč: %s."
                        .formatted(playerPositionInMatch, playerName)
                        : "Byl jste přihlášen na zápas %s jako - %s. Hráč: %s."
                        .formatted(formattedDateTime, playerPositionInMatch, playerName);
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


            // EXCUSE – omluvy a neomluvené absence


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


            // MATCH_INFO – informace o zápase


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
     *
     * Upřednostňuje se kombinace jména a příjmení. Pokud jméno
     * nebo příjmení není k dispozici, použije se e-mail. Pokud
     * není k dispozici ani ten, vrací se zástupný text.
     *
     * @param user uživatel, pro kterého se jméno sestavuje
     * @return zobrazitelné jméno uživatele nebo zástupný text
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
     * Sestaví zobrazitelné jméno hráče.
     *
     * Použije se plné jméno hráče, pokud je k dispozici. V opačném
     * případě se vrací zástupný text.
     *
     * @param player hráč, pro kterého se jméno sestavuje
     * @return zobrazitelné jméno hráče nebo zástupný text
     */
    private String fullPlayerName(PlayerEntity player) {
        if (player == null) return "(neznámý hráč)";
        if (player.getFullName() != null && !player.getFullName().isBlank()) {
            return player.getFullName();
        }
        return "(beze jména)";
    }

    /**
     * Vrátí neprázdný řetězec pro bezpečnou práci s texty.
     *
     * Pokud je předaný řetězec null, vrací se prázdný řetězec.
     * Tato metoda zjednodušuje práci s volitelnými textovými poli.
     *
     * @param s vstupní řetězec
     * @return původní řetězec nebo prázdný řetězec, pokud byl vstup null
     */
    private String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Bezpečně přetypuje kontext na očekávaný typ.
     *
     * Pokud kontext není instance očekávané třídy, vrací se null.
     * Díky tomu se předchází ClassCastException a volání mohou
     * s výsledkem pracovat bezpečně.
     *
     * @param context  objekt kontextu, který má být přetypován
     * @param expected očekávaný typ kontextu
     * @param <T>      typový parametr odpovídající očekávané třídě
     * @return přetypovaný kontext nebo null, pokud typ neodpovídá
     */
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

    /**
     * Zformátuje datum a čas zápasu pro potřeby notifikace.
     *
     * Použije se předdefinovaný formátovač s českou lokalizací.
     * Pokud zápas nebo jeho datum není k dispozici, vrací se prázdný řetězec.
     *
     * @param match zápas, jehož datum a čas se mají formátovat
     * @return naformátované datum a čas zápasu nebo prázdný řetězec
     */
    private String formatMatchDateTime(MatchEntity match) {
        if (match == null || match.getDateTime() == null) {
            return "";
        }
        return match.getDateTime().format(MATCH_DATETIME_FORMATTER);
    }

    /**
     * Spočítá počet přihlášených hráčů k danému zápasu.
     *
     * Počet se zjišťuje voláním repository nad entitou registrace
     * s filtrem na stav REGISTERED. Pokud není zápas nebo jeho identifikátor
     * k dispozici, vrací se nula.
     *
     * @param match zápas, pro který se počet přihlášených hráčů zjišťuje
     * @return počet hráčů s registací ve stavu REGISTERED
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
     *
     * Kontext může být různých typů (registrace, zápas, informace o změně
     * času zápasu). Metoda se pokusí z těchto objektů získat instanci
     * MatchEntity. Pokud ji nelze získat, vrací se null.
     *
     * @param context kontext, ze kterého se má zápas odvodit
     * @return entita zápasu nebo null, pokud nebude nalezena
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

    /**
     * Z kontextu získá entitu registrace na zápas.
     *
     * Pokud není kontext instancí MatchRegistrationEntity, vrací se null.
     *
     * @param context kontext, ze kterého se registrace získává
     * @return entita registrace nebo null, pokud typ neodpovídá
     */
    private MatchRegistrationEntity extractMatchRegistration(Object context) {
        if (context instanceof MatchRegistrationEntity reg) {
            return reg;
        }
        return null;
    }

    /**
     * Vrací čitelný popis důvodu zrušení zápasu.
     * Pokud není důvod nastaven, vrací prázdný řetězec.
     *
     * @param match zápas, ze kterého se důvod zrušení získává
     * @return slovní popis důvodu zrušení nebo prázdný řetězec
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
     *
     * @param registration registrace hráče na zápas s případným důvodem omluvy
     * @return slovní popis důvodu omluvy nebo prázdný řetězec
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

    /**
     * Vrací čitelný popis pozice hráče v zápase.
     * Pokud není nastaven, vrací prázdný řetězec.
     *
     * @param registration registrace hráče na zápas s případnou pozici
     * @return slovní popis pozice v zápase nebo prázdný řetězec
     */
    private String assignPositionInMatch(MatchRegistrationEntity registration) {
        if (registration == null || registration.getPositionInMatch() == null) {
            return "";
        }

        return switch (registration.getPositionInMatch()) {
            case GOALIE -> "brankář";
            case DEFENSE_LEFT -> "levý obránce";
            case DEFENSE_RIGHT -> "pravý obránce";
            case DEFENSE -> "obránce";
            case CENTER -> "centr";
            case WING_LEFT -> "levé křídlo";
            case WING_RIGHT -> "pravé křídlo";
            case FORWARD -> "utočník";
            case ANY -> "hráč";
            default -> "nezjištěno";
        };

    }
}