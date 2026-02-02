package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.*;
import cz.phsoft.hokej.data.enums.*;
import cz.phsoft.hokej.data.repositories.*;
import cz.phsoft.hokej.models.services.AppUserSettingsService;
import cz.phsoft.hokej.models.services.PlayerSettingsService;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
/**
 * Komponenta pro inicializaci testovacích dat v databázi.
 *
 * Po startu aplikace vytváří výchozího administrátora, ukázkové hráče
 * s uživateli, nastavení uživatelů a hráčů, sezóny, zápasy, ukázkové
 * registrace a databázové triggery pro historii registrací.
 *
 * Třída je určena hlavně pro vývojové a testovací prostředí. V produkci
 * se má používat pouze po zvážení důsledků, aby nedošlo k nechtěnému
 * přepsání dat.
 */
public class DataInitializer {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final AppUserRepository appUserRepository;
    private final SeasonRepository seasonRepository;
    private final AppUserSettingsRepository appUserSettingsRepository;
    private final PlayerSettingsRepository playerSettingsRepository;
    private final AppUserSettingsService appUserSettingService;
    private final PlayerSettingsService playerSettingService;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(PlayerRepository playerRepository,
                           MatchRepository matchRepository,
                           MatchRegistrationRepository matchRegistrationRepository,
                           AppUserRepository appUserRepository,
                           SeasonRepository seasonRepository,
                           AppUserSettingsRepository appUserSettingsRepository,
                           PlayerSettingsRepository playerSettingsRepository,
                           AppUserSettingsService appUserSettingService,
                           PlayerSettingsService playerSettingService,
                           JdbcTemplate jdbcTemplate) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.appUserRepository = appUserRepository;
        this.seasonRepository = seasonRepository;
        this.appUserSettingsRepository = appUserSettingsRepository;
        this.playerSettingsRepository = playerSettingsRepository;
        this.appUserSettingService = appUserSettingService;
        this.playerSettingService = playerSettingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Spouští se po inicializaci Spring kontejneru.
     *
     * V definovaném pořadí volá metody inicializující jednotlivé oblasti
     * dat tak, aby byly zachovány závislosti mezi entitami.
     */
    @PostConstruct
    public void init() {
        initAdmin();
        initPlayersAndUsers();
        initUserSettings();
        initPlayerSettings();
        initSeasons();
        initMatches();
        initRegistrations();
        initTriggers();

        System.out.println("Data initialization completed.");
    }

    // Inicializace administrátorského účtu

    /**
     * Vytváří výchozího administrátora, pokud ještě neexistuje.
     */
    private void initAdmin() {
        appUserRepository.findByEmail("admin@example.com").ifPresentOrElse(
                existing -> System.out.println("Admin user already exists – skipping."),
                () -> {
                    AppUserEntity admin = new AppUserEntity();
                    admin.setName("admin");
                    admin.setSurname("admin");
                    admin.setEmail("admin@example.com");
                    admin.setPassword(encoder.encode("Administrator123"));
                    admin.setRole(Role.ROLE_ADMIN);
                    admin.setEnabled(true);
                    appUserRepository.save(admin);
                    System.out.println("Default admin user created.");
                }
        );
    }

    // Inicializace hráčů a jejich uživatelských účtů

    /**
     * Vytváří testovací hráče a k nim přiřazené uživatele.
     *
     * Pokud již v databázi existují hráči, inicializace se přeskočí.
     */
    private void initPlayersAndUsers() {
        if (playerRepository.count() > 0) {
            System.out.println("Players already exist – skipping player initialization.");
            return;
        }

        List<PlayerEntity> players = new ArrayList<>(List.of(
                new PlayerEntity("Hráč_1", "Jedna", "", PlayerType.VIP, "+420776609956", Team.DARK, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_2", "Dva", "", PlayerType.VIP, "+420776609956", Team.LIGHT, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_3", "Tři", "", PlayerType.VIP, "+420776609956", Team.LIGHT, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_4", "Čtyři", "", PlayerType.STANDARD, "+420776609956", Team.LIGHT, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_5", "Pět", "", PlayerType.STANDARD, "+420776609956", Team.LIGHT, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_6", "Šest", "", PlayerType.STANDARD, "+420776609956", Team.DARK, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_7", "Sedm", "", PlayerType.STANDARD, "+420776609956", Team.LIGHT, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_8", "Osum", "", PlayerType.BASIC, "+420776609956", Team.DARK, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_9", "Devět", "", PlayerType.BASIC, "+420776609956", Team.DARK, PlayerStatus.APPROVED),
                new PlayerEntity("Hráč_10", "Deset", "", PlayerType.BASIC, "+420776609956", Team.DARK, PlayerStatus.PENDING)
        ));

        int playerCounter = 1;

        for (PlayerEntity player : players) {
            String email = "player" + playerCounter + "@example.com";
            String password = "Player123";

            AppUserEntity user = new AppUserEntity();
            user.setName("Hráč" + playerCounter);
            user.setSurname("Číslo_" + playerCounter);
            user.setEmail(email);
            user.setPassword(encoder.encode(password));

            // TODO odmazat email
            switch (playerCounter) {
                case 1 -> {
                    user.setRole(Role.ROLE_MANAGER);
                    user.setEmail("petrhlista@seznam.cz");
                    user.setName("Petr");
                    user.setSurname("Hlista");
                }
                case 2 -> user.setRole(Role.ROLE_PLAYER);
                default -> user.setRole(Role.ROLE_PLAYER);
            }
            user.setEnabled(true);

            // Vztah user <-> player podle doménového modelu
            player.setUser(user);

            appUserRepository.save(user); // nebo playerRepository.save(player) – podle nastavené kaskády
            playerCounter++;
        }

        // Pokud kaskáda neřeší uložení hráčů, uloží se explicitně
        playerRepository.saveAll(players);

        System.out.println("Players and users initialized.");
    }

    // Inicializace uživatelských nastavení

    /**
     * Vytváří výchozí nastavení pro všechny uživatele, kteří je ještě nemají.
     */
    private void initUserSettings() {

        System.out.println("Initializing userSettings...");
        List<AppUserEntity> users = appUserRepository.findAll();

        for (AppUserEntity user : users) {

            boolean hasSettings = appUserSettingsRepository.existsByUser(user);
            // Případně lze použít existsByUserId

            if (hasSettings) {
                continue;
            }

            AppUserSettingsEntity settings =
                    appUserSettingService.createDefaultSettingsForUser(user);

            appUserSettingsRepository.save(settings);
        }

        System.out.println("User settings initialized.");
    }

    /**
     * Vytváří výchozí nastavení pro všechny hráče, kteří je ještě nemají.
     */
    private void initPlayerSettings() {
        System.out.println("Initializing player settings...");

        List<PlayerEntity> players = playerRepository.findAll();

        for (PlayerEntity player : players) {

            boolean hasSettings = playerSettingsRepository.existsByPlayer(player);
            // Případně existsByPlayerId(player.getId())

            if (hasSettings) {
                continue;
            }

            PlayerSettingsEntity settings =
                    playerSettingService.createDefaultSettingsForPlayer(player);

            playerSettingsRepository.save(settings);
        }

        System.out.println("Player settings initialized.");
    }

    // Inicializace sezón

    /**
     * Vytváří výchozí sezóny, pokud žádné neexistují.
     */
    private void initSeasons() {
        if (seasonRepository.count() > 0) {
            System.out.println("Seasons already exist – skipping match initialization.");
            return;
        }
        System.out.println("Initializing seasons...");

        SeasonEntity season2024_2025 = new SeasonEntity();
        season2024_2025.setName("2024/2025");
        season2024_2025.setStartDate(LocalDate.of(2024, 11, 20));
        season2024_2025.setEndDate(LocalDate.of(2025, 3, 31));
        season2024_2025.setActive(false);

        SeasonEntity season2025_2026 = new SeasonEntity();
        season2025_2026.setName("2025/2026");
        season2025_2026.setStartDate(LocalDate.of(2025, 11, 21));
        season2025_2026.setEndDate(LocalDate.of(2026, 3, 31));
        season2025_2026.setActive(true);

        SeasonEntity season2026_2027 = new SeasonEntity();
        season2026_2027.setName("2026/2027");
        season2026_2027.setStartDate(LocalDate.of(2026, 11, 1));
        season2026_2027.setEndDate(LocalDate.of(2027, 3, 31));
        season2026_2027.setActive(false);

        seasonRepository.saveAll(List.of(
                season2024_2025,
                season2025_2026,
                season2026_2027
        ));

        System.out.println("Seasons initialized.");
    }

    // Inicializace zápasů

    /**
     * Vytváří zápasy pro jednotlivé sezóny.
     *
     * Zápasy se generují po pátcích v rámci období každé sezóny.
     */
    private void initMatches() {
        if (matchRepository.count() > 0) {
            System.out.println("Matches already exist – skipping match initialization.");
            return;
        }

        List<SeasonEntity> seasons = seasonRepository.findAll();
        if (seasons.isEmpty()) {
            throw new IllegalStateException("BE - Nelze inicializovat zápasy, neexistuje žádná sezóna.");
        }

        System.out.println("Initializing matches...");
        for (int j = 0; j < 2; j++) {
            SeasonEntity actualSeason = seasons.get(j);
            System.out.println("Nastavuji aktuální sezónu " + j);
            LocalDate startSeasonDate = actualSeason.getStartDate();
            LocalDate endSeasonDate = actualSeason.getEndDate();
            LocalDateTime startDate = startSeasonDate
                    .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                    .atTime(18, 45);
            int fridaysCount = countFridays(startSeasonDate, endSeasonDate);
            System.out.println("Jdu vytvářet zápasy");
            for (int i = 0; i < fridaysCount; i++) {
                MatchEntity match = new MatchEntity();
                LocalDateTime dateTime = startDate.plusWeeks(i);
                match.setDateTime(dateTime);
                match.setLocation("WOODARÉNA");
                match.setDescription("");
                match.setMaxPlayers(12);
                match.setPrice(2200);
                match.setMatchStatus(null);
                match.setCancelReason(null);
                match.setSeason(actualSeason);
                matchRepository.save(match);

            }
            System.out.println("Zápasy v sezóně byly vytvořeny");
        }

        System.out.println("Zápasy vytvořeny");
    }

    /**
     * Pomocná metoda pro spočítání počtu pátků v daném období.
     *
     * @param from počáteční datum
     * @param to   koncové datum
     * @return počet pátků mezi daty včetně
     */
    private int countFridays(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            return 0;
        }
        LocalDate firstFriday = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        if (firstFriday.isAfter(to)) {
            return 0;
        }
        int count = 0;
        for (LocalDate date = firstFriday;
             !date.isAfter(to);
             date = date.plusWeeks(1)) {
            count++;
        }

        return count;
    }

    // Inicializace registrací

    /**
     * Vytváří ukázkové registrace hráčů na zápasy.
     *
     * Registrace se generují pouze pro omezený počet zápasů v blízké budoucnosti,
     * aby bylo možné testovat různé stavy registrací.
     */
    private void initRegistrations() {
        if (matchRegistrationRepository.count() > 0) {
            System.out.println("Match registrations already exist – skipping registration initialization.");
            return;
        }

        LocalDateTime finalDate = LocalDateTime.now().plusWeeks(1);

        List<MatchEntity> matches = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().isBefore(finalDate))
                .toList();

        List<PlayerEntity> players = playerRepository.findAll().stream()
                .filter(p -> p.getId() != null)
                .filter(p -> p.getPlayerStatus() == PlayerStatus.APPROVED)
                .toList();

        if (matches.isEmpty() || players.size() < 6) {
            System.out.println("Nedostatek dat pro vytvoření registrací – skipping.");
            return;
        }

        for (MatchEntity match : matches) {

            List<PlayerEntity> shuffledPlayers = new ArrayList<>(players);
            Collections.shuffle(shuffledPlayers);
            List<PlayerEntity> selectedPlayers = shuffledPlayers.subList(0, 8);

            List<Integer> indexes = new ArrayList<>();
            for (int i = 0; i < selectedPlayers.size(); i++) {
                indexes.add(i);
            }
            Collections.shuffle(indexes);

            int excusedIndex = indexes.get(0);
            int unregisteredIndex1 = indexes.get(1);
            int unregisteredIndex2 = indexes.get(2);

            for (int i = 0; i < selectedPlayers.size(); i++) {
                PlayerEntity player = selectedPlayers.get(i);

                MatchRegistrationEntity reg = new MatchRegistrationEntity();
                reg.setMatch(match);
                reg.setPlayer(player);

                if (i == excusedIndex) {
                    reg.setStatus(PlayerMatchStatus.EXCUSED);
                    reg.setExcuseReason(ExcuseReason.NEMOC);
                    reg.setExcuseNote("chřipka");
                } else if (i == unregisteredIndex1 || i == unregisteredIndex2) {
                    reg.setStatus(PlayerMatchStatus.UNREGISTERED);
                    reg.setExcuseReason(null);
                    reg.setExcuseNote(null);
                } else {
                    reg.setStatus(PlayerMatchStatus.REGISTERED);
                    reg.setExcuseReason(null);
                    reg.setExcuseNote(null);
                }

                // Tým hráče se přebírá z entity hráče
                reg.setTeam(player.getTeam());
                reg.setTimestamp(LocalDateTime.now());
                reg.setCreatedBy("initializer");

                matchRegistrationRepository.save(reg);
            }
        }

        System.out.println("Sample registrations initialized.");
    }

    // Inicializace databázových triggerů

    /**
     * Vytváří databázové triggery pro tabulku registrací.
     *
     * Triggery zajišťují automatický zápis do tabulky historie registrací
     * při vzniknutí, změně nebo smazání registrace.
     */
    private void initTriggers() {
        createTrigger("trg_match_reg_insert", """
                CREATE TRIGGER trg_match_reg_insert
                AFTER INSERT ON match_registrations
                FOR EACH ROW
                BEGIN
                    INSERT INTO match_registration_history
                    (match_registration_id, match_id, player_id, status, excuse_reason,
                     excuse_note, admin_note, team, original_timestamp, created_by,
                     action, changed_at)
                    VALUES
                    (NEW.id, NEW.match_id, NEW.player_id, NEW.status, NEW.excuse_reason,
                     NEW.excuse_note, NEW.admin_note, NEW.team, NEW.timestamp, NEW.created_by,
                     'INSERT', NOW());
                END
                """);

        createTrigger("trg_match_reg_update", """
                CREATE TRIGGER trg_match_reg_update
                AFTER UPDATE ON match_registrations
                FOR EACH ROW
                BEGIN
                    INSERT INTO match_registration_history
                    (match_registration_id, match_id, player_id, status, excuse_reason,
                     excuse_note, admin_note, team, original_timestamp, created_by,
                     action, changed_at)
                    VALUES
                    (NEW.id, NEW.match_id, NEW.player_id, NEW.status, NEW.excuse_reason,
                     NEW.excuse_note, NEW.admin_note, NEW.team, NEW.timestamp, NEW.created_by,
                     'UPDATE', NOW());
                END
                """);

        createTrigger("trg_match_reg_delete", """
                CREATE TRIGGER trg_match_reg_delete
                AFTER DELETE ON match_registrations
                FOR EACH ROW
                BEGIN
                    INSERT INTO match_registration_history
                    (match_registration_id, match_id, player_id, status, excuse_reason,
                     excuse_note, admin_note, team, original_timestamp, created_by,
                     action, changed_at)
                    VALUES
                    (OLD.id, OLD.match_id, OLD.player_id, OLD.status, OLD.excuse_reason,
                     OLD.excuse_note, OLD.admin_note, OLD.team, OLD.timestamp, OLD.created_by,
                     'DELETE', NOW());
                END
                """);
    }

    /**
     * Pomocná metoda pro vytvoření triggeru, pokud ještě neexistuje.
     *
     * Chyby při vytváření triggeru se vypisují do konzole, ale neblokují
     * start aplikace.
     *
     * @param name název triggeru
     * @param sql  SQL definice triggeru
     */
    private void createTrigger(String name, String sql) {
        try {
            jdbcTemplate.execute(sql);
            System.out.println("Trigger " + name + " created successfully.");
        } catch (Exception e) {
            System.out.println("Trigger " + name + " already exists or error: " + e.getMessage());
        }
    }
}
