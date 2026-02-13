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
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

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
     * <p>
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

                    // standardní uložení – fungovalo doteď
                    appUserRepository.save(admin);

                    // přepsání timestampu na 1.11.2024 08:00 přes JdbcTemplate
                    LocalDateTime fixedTimestamp = LocalDateTime.of(2024, 11, 1, 8, 0);

                    jdbcTemplate.update(
                            "UPDATE app_users SET timestamp = ? WHERE id = ?",
                            fixedTimestamp,           // nebo Timestamp.valueOf(fixedTimestamp)
                            admin.getId()
                    );

                    System.out.println("Default admin user created.");
                }
        );
    }

    // Inicializace hráčů a jejich uživatelských účtů

    /**
     * Vytváří testovací hráče a k nim přiřazené uživatele.
     * <p>
     * Pokud již v databázi existují hráči, inicializace se přeskočí.
     */
    private void initPlayersAndUsers() {
        if (playerRepository.count() > 0) {
            System.out.println("Players already exist – skipping player initialization.");
            return;
        }

        String[] names = {
                "Jan",
                "Petr",
                "Jiří",
                "Josef",
                "Pavel",
                "Martin",
                "Tomáš",
                "Jaroslav",
                "Miroslav",
                "Zdeněk"
        };
        String[] surnames = {
                "Novák",
                "Svoboda",
                "Novotný",
                "Dvořák",
                "Černý",
                "Procházka",
                "Kučera",
                "Veselý",
                "Horák",
                "Němec"
        };

        for (int i = 0; i < 10; i++) {
            PlayerEntity player = new PlayerEntity();
            AppUserEntity user = new AppUserEntity();

            player.setName(names[i]);
            user.setName(names[i]);
            player.setSurname(surnames[i].toUpperCase());
            user.setSurname(surnames[i].toUpperCase());

            String email = "player" + (i + 1) + "@example.com";
            String password = "Heslo123";
            user.setEmail(email);
            user.setPassword(encoder.encode(password));

            switch (i) {
                case 0, 1, 2 -> player.setType(PlayerType.VIP);
                case 3, 4, 5, 6 -> player.setType(PlayerType.STANDARD);
                default -> player.setType(PlayerType.BASIC);
            }

            player.setPhoneNumber("");
            if (i < 5) {
                player.setTeam(Team.DARK);
            } else {
                player.setTeam(Team.LIGHT);
            }
            if (i < 8) {
                player.setPlayerStatus(PlayerStatus.APPROVED);
            } else {
                player.setPlayerStatus(PlayerStatus.PENDING);
            }

            switch (i) {
                case 0 -> user.setRole(Role.ROLE_MANAGER);
                default -> user.setRole(Role.ROLE_PLAYER);
            }

            user.setEnabled(true);
            player.setUser(user);

            // náhodný timestamp pro dvojici (user + player)
            LocalDateTime randomTs = randomTimestampForDemoData();

            // hráči nastavíme timestamp rovnou (PrePersist ho nepřepíše,
            // protože metoda v entitě PlayerEntity nastavuje jen když je null)
            player.setTimestamp(randomTs);

            // nejdřív uložíme uživatele a hráče
            appUserRepository.save(user);
            playerRepository.save(player);

            // uživateli přepíšeme timestamp přímo v DB,
            // protože AppUserEntity @PrePersist nastavuje vždy now()
            jdbcTemplate.update(
                    "UPDATE app_users SET timestamp = ? WHERE id = ?",
                    randomTs,
                    user.getId()
            );
        }

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
        season2024_2025.setStartDate(LocalDate.of(2024, 11, 01));
        season2024_2025.setEndDate(LocalDate.of(2025, 3, 31));
        season2024_2025.setActive(false);
        season2024_2025.setCreatedByUserId(2L);

        SeasonEntity season2025_2026 = new SeasonEntity();
        season2025_2026.setName("2025/2026");
        season2025_2026.setStartDate(LocalDate.of(2025, 11, 01));
        season2025_2026.setEndDate(LocalDate.of(2026, 3, 31));
        season2025_2026.setActive(true);
        season2025_2026.setCreatedByUserId(2L);

        SeasonEntity season2026_2027 = new SeasonEntity();
        season2026_2027.setName("2026/2027");
        season2026_2027.setStartDate(LocalDate.of(2026, 11, 01));
        season2026_2027.setEndDate(LocalDate.of(2027, 3, 31));
        season2026_2027.setActive(false);
        season2026_2027.setCreatedByUserId(2L);

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
     * <p>
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
                match.setLocation("NĚJAKÁ HALA");
                match.setDescription("");
                match.setMaxPlayers(12);
                match.setPrice(2200);
                match.setMatchStatus(null);
                match.setCancelReason(null);
                match.setSeason(actualSeason);
                match.setCreatedByUserId(2L);
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
     * <p>
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
     * <p>
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
        /**
         * Vytváří databázové triggery pro tabulku hráčů.
         * <p>
         * Triggery zajišťují automatický zápis do tabulky historie hráče
         * při vzniku, změně nebo smazání hráče.
         */

        createTrigger("trg_player_insert", """
                CREATE TRIGGER trg_player_insert
                AFTER INSERT ON player_entity
                FOR EACH ROW
                BEGIN
                    INSERT INTO player_entity_history
                        (player_id, name, surname, nickname, type, full_name, phone_number,
                         team, player_status, user_id, original_timestamp, action, changed_at)
                    VALUES
                        (NEW.id, NEW.name, NEW.surname, NEW.nickname, NEW.type, NEW.full_name, NEW.phone_number,
                         NEW.team, NEW.player_status, NEW.user_id, NEW.timestamp, 'INSERT', NOW());
                END
                """);

        createTrigger("trg_player_update", """
                CREATE TRIGGER trg_player_update
                AFTER UPDATE ON player_entity
                FOR EACH ROW
                BEGIN
                    INSERT INTO player_entity_history
                        (player_id, name, surname, nickname, type, full_name, phone_number,
                         team, player_status, user_id, original_timestamp, action, changed_at)
                    VALUES
                        (NEW.id, NEW.name, NEW.surname, NEW.nickname, NEW.type, NEW.full_name, NEW.phone_number,
                         NEW.team, NEW.player_status, NEW.user_id, NEW.timestamp, 'UPDATE', NOW());
                END
                """);


        createTrigger("trg_player_delete", """
                CREATE TRIGGER trg_player_delete
                AFTER DELETE ON player_entity
                FOR EACH ROW
                BEGIN
                    INSERT INTO player_entity_history
                        (player_id, name, surname, nickname, type, full_name, phone_number,
                         team, player_status, user_id, original_timestamp, action, changed_at)
                    VALUES
                        (OLD.id, OLD.name, OLD.surname, OLD.nickname, OLD.type, OLD.full_name, OLD.phone_number,
                         OLD.team, OLD.player_status, OLD.user_id, OLD.timestamp, 'DELETE', NOW());
                END
                """);

        createTrigger("trg_match_insert", """
                CREATE TRIGGER trg_match_insert
                AFTER INSERT ON matches
                FOR EACH ROW
                BEGIN
                    INSERT INTO matches_history
                        (match_id,
                         original_timestamp,
                         action,
                         changed_at,
                         date_time,
                         location,
                         description,
                         max_players,
                         price,
                         match_status,
                         cancel_reason,
                         season_id,
                         created_by_user_id,
                         last_modified_by_user_id)
                    VALUES
                        (NEW.id,
                         NEW.timestamp,
                         'INSERT',
                         NOW(),
                         NEW.date_time,
                         NEW.location,
                         NEW.description,
                         NEW.max_players,
                         NEW.price,
                         NEW.match_status,
                         NEW.cancel_reason,
                         NEW.season_id,
                         NEW.created_by_user_id,
                         NEW.last_modified_by_user_id);
                END
                """);

        createTrigger("trg_match_update", """
                CREATE TRIGGER trg_match_update
                AFTER UPDATE ON matches
                FOR EACH ROW
                BEGIN
                    INSERT INTO matches_history
                        (match_id,
                         original_timestamp,
                         action,
                         changed_at,
                         date_time,
                         location,
                         description,
                         max_players,
                         price,
                         match_status,
                         cancel_reason,
                         season_id,
                         created_by_user_id,
                         last_modified_by_user_id)
                    VALUES
                        (NEW.id,
                         NEW.timestamp,
                         'UPDATE',
                         NOW(),
                         NEW.date_time,
                         NEW.location,
                         NEW.description,
                         NEW.max_players,
                         NEW.price,
                         NEW.match_status,
                         NEW.cancel_reason,
                         NEW.season_id,
                         NEW.created_by_user_id,
                         NEW.last_modified_by_user_id);
                END
                """);

        createTrigger("trg_match_delete", """
                CREATE TRIGGER trg_match_delete
                AFTER DELETE ON matches
                FOR EACH ROW
                BEGIN
                    INSERT INTO matches_history
                        (match_id, original_timestamp, action, changed_at, date_time,
                         location, description,max_players, price, match_status, cancel_reason,
                         season_id, created_by_user_id, last_modified_by_user_id)
                    VALUES
                        (OLD.id,
                         OLD.timestamp,
                         'DELETE',
                         NOW(),
                         OLD.date_time,
                         OLD.location,
                         OLD.description,
                         OLD.max_players,
                         OLD.price,
                         OLD.match_status,
                         OLD.cancel_reason,
                         OLD.season_id,
                         OLD.created_by_user_id,
                         OLD.last_modified_by_user_id);
                END
                """);

        createTrigger("trg_app_user_insert", """
                CREATE TRIGGER trg_app_user_insert
                AFTER INSERT ON app_users
                FOR EACH ROW
                BEGIN
                    INSERT INTO app_users_history
                        (user_id, name, surname, email, role, enabled,
                         original_timestamp, action, changed_at)
                    VALUES
                        (NEW.id, NEW.name, NEW.surname, NEW.email, NEW.role, NEW.enabled,
                         NEW.timestamp, 'INSERT', NOW());
                END
                """);

        createTrigger("trg_app_user_update", """
                CREATE TRIGGER trg_app_user_update
                AFTER UPDATE ON app_users
                FOR EACH ROW
                BEGIN
                    INSERT INTO app_users_history
                        (user_id, name, surname, email, role, enabled,
                         original_timestamp, action, changed_at)
                    VALUES
                        (NEW.id, NEW.name, NEW.surname, NEW.email, NEW.role, NEW.enabled,
                         NEW.timestamp, 'UPDATE', NOW());
                END
                """);

        createTrigger("trg_app_user_delete", """
                CREATE TRIGGER trg_app_user_delete
                AFTER DELETE ON app_users
                FOR EACH ROW
                BEGIN
                    INSERT INTO app_users_history
                        (user_id, name, surname, email, role, enabled,
                         original_timestamp, action, changed_at)
                    VALUES
                        (OLD.id, OLD.name, OLD.surname, OLD.email, OLD.role, OLD.enabled,
                         OLD.timestamp, 'DELETE', NOW());
                END
                """);
        createTrigger("trg_season_insert", """
                CREATE TRIGGER trg_season_insert
                AFTER INSERT ON season
                FOR EACH ROW
                BEGIN
                    INSERT INTO season_history
                        (season_id, original_timestamp, action, changed_at,
                         name, start_date, end_date, active, created_by_user_id)
                    VALUES
                        (NEW.id, NEW.timestamp, 'INSERT', NOW(),
                         NEW.name, NEW.start_date, NEW.end_date, NEW.active, NEW.created_by_user_id);
                END
                """);

        createTrigger("trg_season_update", """
                CREATE TRIGGER trg_season_update
                AFTER UPDATE ON season
                FOR EACH ROW
                BEGIN
                    INSERT INTO season_history
                        (season_id, original_timestamp, action, changed_at,
                         name, start_date, end_date, active, created_by_user_id)
                    VALUES
                        (NEW.id, NEW.timestamp, 'UPDATE', NOW(),
                         NEW.name, NEW.start_date, NEW.end_date, NEW.active, NEW.created_by_user_id);
                END
                """);

        createTrigger("trg_season_delete", """
                CREATE TRIGGER trg_season_delete
                AFTER DELETE ON season
                FOR EACH ROW
                BEGIN
                    INSERT INTO season_history
                        (season_id, original_timestamp, action, changed_at,
                         name, start_date, end_date, active, created_by_user_id)
                    VALUES
                        (OLD.id, OLD.timestamp, 'DELETE', NOW(),
                         OLD.name, OLD.start_date, OLD.end_date, OLD.active, OLD.created_by_user_id);
                END
                """);

    }

    /**
     * Pomocná metoda pro vytvoření triggeru, pokud ještě neexistuje.
     * <p>
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

    /**
     * Vygeneruje náhodný timestamp v rozsahu
     * 1. 11. 2024 00:00:00 až 20. 12. 2025 23:59:59.
     */
    private LocalDateTime randomTimestampForDemoData() {
        LocalDateTime from = LocalDateTime.of(2024, 11, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2025, 2, 10, 23, 59);

        long fromEpoch = from.toEpochSecond(ZoneOffset.UTC);
        long toEpoch = to.toEpochSecond(ZoneOffset.UTC);

        long randomEpoch = ThreadLocalRandom.current()
                .nextLong(fromEpoch, toEpoch + 1);

        return LocalDateTime.ofEpochSecond(randomEpoch, 0, ZoneOffset.UTC);
    }

}
