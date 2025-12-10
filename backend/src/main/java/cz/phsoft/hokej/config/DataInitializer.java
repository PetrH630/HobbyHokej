package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.JerseyColor;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final AppUserRepository appUserRepository;

    public DataInitializer(PlayerRepository playerRepository,
                           MatchRepository matchRepository,
                           MatchRegistrationRepository matchRegistrationRepository,
                           AppUserRepository appUserRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.appUserRepository = appUserRepository;
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
                new PlayerEntity("Petr", "Hlista", PlayerType.VIP, "+420776609956", JerseyColor.DARK),
                new PlayerEntity("Laďa", "Bražina", PlayerType.VIP, "+420776609956", JerseyColor.LIGHT),
                new PlayerEntity("David", "Podsedník", PlayerType.VIP, "+420776609956", JerseyColor.LIGHT),
                new PlayerEntity("Vlastík", "Pstruží", PlayerType.VIP, "+420776609956", JerseyColor.LIGHT),
                new PlayerEntity("Otakar", "Záškodný", PlayerType.VIP, "+420776609956", JerseyColor.LIGHT),
                new PlayerEntity("Jarda", "Menšík", PlayerType.STANDARD, "+420776609956", JerseyColor.DARK),
                new PlayerEntity("Luboš", "Novák", PlayerType.STANDARD, "+420776609956", JerseyColor.LIGHT),
                new PlayerEntity("Lukáš", "Novák", PlayerType.STANDARD, "+420776609956", JerseyColor.DARK),
                new PlayerEntity("Martin", "Čermák", PlayerType.STANDARD, "+420776609956", JerseyColor.DARK),
                new PlayerEntity("Pavel", "Eliáš", PlayerType.STANDARD, "+420776609956", JerseyColor.DARK)
                // ... případně další hráči
        ));

        // --- Vytvoření uživatelů ke každému hráči ---

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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

        System.out.println("Data initialization completed.");

        int playerCounter = 1;
        for (PlayerEntity player : players) {
            // vytvoření uživatele
            String email = "player" + playerCounter + "@example.com";
            String password = "Player123";

            AppUserEntity user = new AppUserEntity();
            user.setEmail(email);
            user.setPassword(encoder.encode(password));
            user.setRole(Role.ROLE_PLAYER);

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

        System.out.println("Data initialization completed.");
    }
}



