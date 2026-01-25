package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.*;
import cz.phsoft.hokej.data.enums.*;
import cz.phsoft.hokej.data.repositories.*;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer {


    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final AppUserRepository appUserRepository;
    private final SeasonRepository seasonRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(PlayerRepository playerRepository,
                           MatchRepository matchRepository,
                           MatchRegistrationRepository matchRegistrationRepository,
                           AppUserRepository appUserRepository,
                           SeasonRepository seasonRepository,
                           JdbcTemplate jdbcTemplate) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.appUserRepository = appUserRepository;
        this.seasonRepository = seasonRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        initAdmin();
        initPlayersAndUsers();
        initSeasons();
        initMatches();
        initRegistrations();
        initTriggers();

        System.out.println("Data initialization completed.");
    }

    // =====================
    // ADMIN
    // =====================
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

    // =====================
    // PLAYERS + USERS
    // =====================
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
                new PlayerEntity("Hráč_5", "Pět", "", PlayerType.STANDARD, "+420776609956", Team.LIGHT, PlayerStatus.PENDING),
                new PlayerEntity("Hráč_6", "Šest", "", PlayerType.STANDARD, "+420776609956", Team.DARK, PlayerStatus.PENDING),
                new PlayerEntity("Hráč_7", "Sedm", "", PlayerType.STANDARD, "+420776609956", Team.LIGHT, PlayerStatus.PENDING),
                new PlayerEntity("Hráč_8", "Osum", "", PlayerType.BASIC, "+420776609956", Team.DARK, PlayerStatus.PENDING),
                new PlayerEntity("Hráč_9", "Devět", "", PlayerType.BASIC, "+420776609956", Team.DARK, PlayerStatus.PENDING),
                new PlayerEntity("Hráč_10", "Deset", "", PlayerType.BASIC, "+420776609956", Team.DARK, PlayerStatus.PENDING)
        ));

        for (PlayerEntity player : players) {
            player.setNotifyByEmail(true);
            player.setNotifyBySms(true);
        }

        int playerCounter = 1;
        for (PlayerEntity player : players) {
            String email = "player" + playerCounter + "@example.com";
            String password = "Player123";

            AppUserEntity user = new AppUserEntity();
            user.setName("Hráč" + playerCounter);
            user.setSurname("Číslo_" + playerCounter);
            user.setEmail(email);
            user.setPassword(encoder.encode(password));

            switch (playerCounter) {
                case 1 -> user.setRole(Role.ROLE_ADMIN);
                case 2 -> user.setRole(Role.ROLE_MANAGER);
                default -> user.setRole(Role.ROLE_PLAYER);
            }
            user.setEnabled(true);

            // vztah user <-> player podle tvého modelu
            player.setUser(user);

            appUserRepository.save(user); // nebo playerRepository.save(player) – podle cascade
            playerCounter++;
        }

        // pokud cascade není, můžeš explicitně uložit i hráče:
        playerRepository.saveAll(players);

        System.out.println("Players and users initialized.");
    }

    // =====================
    // SEASONS
    // =====================

    private void initSeasons() {
        if (seasonRepository.count() > 0) {
            System.out.println("Seasons already exist – skipping match initialization.");
            return;
        }
        System.out.println("Initializing seasons...");

        // Sezóna 2024/2025: 1.11.2024 – 31.3.2025
        SeasonEntity season2024_2025 = new SeasonEntity();
        season2024_2025.setName("2024/2025");
        season2024_2025.setStartDate(LocalDate.of(2024, 11, 1));
        season2024_2025.setEndDate(LocalDate.of(2025, 3, 31));
        season2024_2025.setActive(false);

        // Sezóna 2025/2026: 1.11.2025 – 31.3.2026 (aktuální – nastavíme jako active)
        SeasonEntity season2025_2026 = new SeasonEntity();
        season2025_2026.setName("2025/2026");
        season2025_2026.setStartDate(LocalDate.of(2025, 11, 1));
        season2025_2026.setEndDate(LocalDate.of(2026, 3, 31));
        season2025_2026.setActive(true);

        // Sezóna 2026/2027: 1.11.2026 – 31.3.2027
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

    // =====================
    // MATCHES
    // =====================
    private void initMatches() {
        // Pokud už nějaké zápasy existují, nic nevytváříme
        if (matchRepository.count() > 0) {
            System.out.println("Matches already exist – skipping match initialization.");
            return;
        }

        // Sezóny MUSÍ existovat, jinak nemáme co přiřadit
        java.util.List<SeasonEntity> seasons = seasonRepository.findAll();
        if (seasons.isEmpty()) {
            throw new IllegalStateException("BE - Nelze inicializovat zápasy, neexistuje žádná sezóna.");
        }

        System.out.println("Initializing matches...");

        // výchozí datum prvního zápasu
        java.time.LocalDateTime startDate = java.time.LocalDateTime.of(2025, 11, 21, 18, 45);

        for (int i = 0; i < 15; i++) {
            MatchEntity match = new MatchEntity();

            java.time.LocalDateTime dateTime = startDate.plusWeeks(i);

            match.setDateTime(dateTime);
            match.setLocation("WOODARÉNA");
            match.setDescription("");
            match.setMaxPlayers(12);
            match.setPrice(2200);
            match.setMatchStatus(null);
            match.setCancelReason(null);

            // 🔹 TADY je KLÍČ: vždy najdeme sezónu a nastavíme ji
            SeasonEntity season = findSeasonForDate(dateTime.toLocalDate(), seasons);
            if (season == null) {
                // Tohle by za normálních okolností nemělo nastat, ale když jo, chceme failnout srozumitelně
                throw new IllegalStateException(
                        "BE - Nepodařilo se najít sezónu pro datum zápasu " + dateTime.toLocalDate()
                );
            }
            match.setSeason(season);

            // uložíme zápas
            matchRepository.save(match);
        }

        System.out.println("Matches initialized.");
    }

    // POMOCNÁ METODA PRO INIT MATCHES - Nastavení sezony
    private SeasonEntity findSeasonForDate(
            java.time.LocalDate date,
            java.util.List<SeasonEntity> seasons
    ) {
        // 1) Zkusíme najít sezónu, do které datum spadá (startDate <= date <= endDate)
        for (SeasonEntity season : seasons) {
            boolean startsBeforeOrSame = !date.isBefore(season.getStartDate()); // date >= start
            boolean endsAfterOrSame = !date.isAfter(season.getEndDate());       // date <= end

            if (startsBeforeOrSame && endsAfterOrSame) {
                return season;
            }
        }

        // 2) Pokud žádná nesedí intervalem, vezmeme aktivní sezónu (pokud nějaká je)
        for (SeasonEntity season : seasons) {
            if (season.isActive()) {
                return season;
            }
        }

        // 3) Jako úplný fallback vezmeme první sezónu v seznamu
        //    (k tomuhle by se to nemělo moc dostávat, ale je to bezpečná pojistka)
        return seasons.get(0);
    }



    // =====================
    // REGISTRATIONS
    // =====================
    private void initRegistrations() {
        if (matchRegistrationRepository.count() > 0) {
            System.out.println("Match registrations already exist – skipping registration initialization.");
            return;
        }

        List<MatchEntity> matches = matchRepository.findAll();
        List<PlayerEntity> players = playerRepository.findAll();

        if (matches.isEmpty() || players.size() < 6) {
            System.out.println("Not enough data to create registrations – skipping.");
            return;
        }

        MatchEntity match = matches.get(2); // „třetí“ vytvořený zápas
        for (int i = 0; i < 6; i++) {
            PlayerEntity player = players.get(i);

            MatchRegistrationEntity reg = new MatchRegistrationEntity();
            reg.setMatch(match);
            reg.setPlayer(player);
            reg.setStatus(PlayerMatchStatus.REGISTERED);
            reg.setTeam(i < 3 ? Team.DARK : Team.LIGHT);
            reg.setTimestamp(LocalDateTime.now());
            reg.setCreatedBy("initializer");

            matchRegistrationRepository.save(reg);
        }

        System.out.println("Sample registrations initialized.");
    }

    // =====================
    // TRIGGERS
    // =====================
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

    private void createTrigger(String name, String sql) {
        try {
            jdbcTemplate.execute(sql);
            System.out.println("Trigger " + name + " created successfully.");
        } catch (Exception e) {
            System.out.println("Trigger " + name + " already exists or error: " + e.getMessage());
        }
    }
}
