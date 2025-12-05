package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.JerseyColor;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer {
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;

    public DataInitializer(PlayerRepository playerRepository,
                           MatchRepository matchRepository,
                           MatchRegistrationRepository matchRegistrationRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
    }

    @PostConstruct
    public void init() {
        // Pokud existuje alespoň 1 hráč, znamená to, že DB už byla inicializovaná
        if (playerRepository.count() > 0) {
            System.out.println("Data already initialized – skipping DataInitializer.");
            return;
        }
        System.out.println("Initializing default data...");

        // --- 10 hráčů ---
        List<PlayerEntity> players = List.of(
                new PlayerEntity("Petr", "Hlista", PlayerType.VIP, JerseyColor.DARK),
                new PlayerEntity("Laďa", "Bražina", PlayerType.VIP, JerseyColor.LIGHT),
                new PlayerEntity("David", "Podsedník", PlayerType.VIP, JerseyColor.LIGHT),
                new PlayerEntity("Vlastík", "Pstruží", PlayerType.VIP, JerseyColor.LIGHT),
                new PlayerEntity("Otakar", "Záškodný", PlayerType.VIP, JerseyColor.LIGHT),
                new PlayerEntity("Jarda", "Menšík", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Luboš", "Novák", PlayerType.STANDARD, JerseyColor.LIGHT),
                new PlayerEntity("Lukáš", "Novák", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Martin", "Čermák", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Pavel", "Eliáš", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Staňa", "Jurok", PlayerType.BASIC, JerseyColor.DARK),
                new PlayerEntity("Michal", "Pyszko", PlayerType.BASIC, JerseyColor.DARK),
                new PlayerEntity("Jenda", "Kaluža", PlayerType.STANDARD, JerseyColor.LIGHT),
                new PlayerEntity("Tomáš", "Faldyna", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Martin", "Faldyna", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Lola", "Dorda", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Lukáš", "Dorda", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Martin", "Dorda", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Radim", "Mališ", PlayerType.STANDARD, JerseyColor.DARK),
                new PlayerEntity("Laďa", "Pavlica", PlayerType.STANDARD, JerseyColor.LIGHT),
                new PlayerEntity("Vlaďa", "Menšík", PlayerType.STANDARD, JerseyColor.DARK)

        );
        playerRepository.saveAll(players);

        // --- 5 zápasů, každý pátek od 21.11.2025 ---
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
      // --- Vytvoření registrací 6 hráčů (id 1-6) na zápas id 3 ---
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
    }
}
