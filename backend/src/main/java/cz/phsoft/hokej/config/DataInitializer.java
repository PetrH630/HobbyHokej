package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.*;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final AppUserRepository appUserRepository;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(PlayerRepository playerRepository,
                           MatchRepository matchRepository,
                           MatchRegistrationRepository matchRegistrationRepository,
                           AppUserRepository appUserRepository,
                           JdbcTemplate jdbcTemplate) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.appUserRepository = appUserRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        // Pokud existuje alespoň 1 hráč, DB už byla inicializovaná
        if (playerRepository.count() > 0) {
            System.out.println("Data already initialized – skipping DataInitializer.");
            return;
        }
        System.out.println("Initializing default data...");

        // --- Seznam hráčů ---
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
                // ... případně další hráči
        ));

        // --- Vytvoření uživatelů ke každému hráči ---

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // --- Default admin ---
        if (appUserRepository.findByEmail("admin@example.com").isEmpty()) {
            AppUserEntity admin = new AppUserEntity();
            admin.setName("admin");
            admin.setSurname("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(encoder.encode("Administrator123"));
            admin.setRole(Role.ROLE_ADMIN);
            appUserRepository.save(admin);
            System.out.println("Default admin user created.");
        } else {
            System.out.println("Admin user already exists – skipping.");
        }

        System.out.println("Data initialization completed.");

        int playerCounter = 1;
        for (PlayerEntity player : players) {
            // vytvoření uživatele
            String email = "player" + playerCounter + "@example.com";
            String password = "Player123";

            AppUserEntity user = new AppUserEntity();
            user.setName("Hráč" + playerCounter);
            user.setSurname("Číslo_" + playerCounter);
            user.setEmail(email);
            user.setPassword(encoder.encode(password));
            switch (playerCounter){
                case 1:
                    user.setRole(Role.ROLE_ADMIN);
                case 2:
                    user.setRole(Role.ROLE_MANAGER);
                default:
                    user.setRole(Role.ROLE_PLAYER);
            }
            // přiřadit hráče k uživateli
            player.setUser(user);

            // uložit uživatele (cascade uloží i hráče, pokud je správně nastaven)
            appUserRepository.save(user);

            playerCounter++;
        }

        // --- Uložit hráče (už uložen při cascade, ale pro jistotu) ---
        playerRepository.saveAll(players);

        // --- Vytvoření zápasů ---
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 21, 18, 45);
        for (int i = 0; i < 10; i++) {
            MatchEntity match = new MatchEntity();
            match.setDateTime(startDate.plusWeeks(i));
            match.setLocation("Ostravice");
            match.setDescription("");
            match.setMaxPlayers(12);
            match.setPrice(2200);
            matchRepository.save(match);
        }

        // --- Registrace hráčů na zápas id 3 ---
        MatchEntity match3 = matchRepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Match with id 3 not found"));

        for (long playerId = 1; playerId <= 6; playerId++) {
            final long pid = playerId;
            PlayerEntity player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player with id " + pid + " not found"));

            MatchRegistrationEntity reg = new MatchRegistrationEntity();
            reg.setMatch(match3);
            reg.setPlayer(player);
            reg.setStatus(PlayerMatchStatus.REGISTERED);
            reg.setTimestamp(LocalDateTime.now());
            reg.setCreatedBy("user");
            matchRegistrationRepository.save(reg);
        }

        // --- Default admin ---
        if (appUserRepository.findByEmail("admin@example.com").isEmpty()) {
            AppUserEntity admin = new AppUserEntity();
            admin.setEmail("admin@example.com");
            admin.setPassword(encoder.encode("Administrator123"));
            admin.setRole(Role.ROLE_ADMIN);
            appUserRepository.save(admin);
            System.out.println("Default admin user created.");
        } else {
            System.out.println("Admin user already exists – skipping.");
        }

        // --- vytvoření triggeru ---
        try {
            jdbcTemplate.execute("""
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
            System.out.println("Trigger created successfully.");
        } catch (Exception e) {
            System.out.println("Trigger already exists or error: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
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
            System.out.println("Trigger created successfully.");
        } catch (Exception e) {
            System.out.println("Trigger already exists or error: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("""
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
            System.out.println("Trigger created successfully.");
                } catch (Exception e) {
                    System.out.println("Trigger already exists or error: " + e.getMessage());
                }
        
        System.out.println("Data initialization completed.");
    

    }
}



