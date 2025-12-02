package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
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
        // --- 10 hráčů ---
        List<PlayerEntity> players = List.of(
                new PlayerEntity("Petr", "Hlista", PlayerType.VIP),
                new PlayerEntity("Petr", "Svoboda", PlayerType.VIP),
                new PlayerEntity("Tomáš", "Dvořák", PlayerType.VIP),
                new PlayerEntity("Martin", "Černý", PlayerType.STANDARD),
                new PlayerEntity("Jakub", "Procházka", PlayerType.STANDARD),
                new PlayerEntity("Lukáš", "Veselý", PlayerType.STANDARD),
                new PlayerEntity("David", "Král", PlayerType.BASIC),
                new PlayerEntity("Michal", "Beneš", PlayerType.BASIC),
                new PlayerEntity("Filip", "Horák", PlayerType.BASIC),
                new PlayerEntity("Karel", "Zeman", PlayerType.BASIC)
        );
        playerRepository.saveAll(players);

        // --- 5 zápasů, každý pátek od 21.11.2025 ---
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 21, 18, 45);
        for (int i = 0; i < 5; i++) {
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
            PlayerEntity player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player with id " + playerId + " not found"));

            MatchRegistrationEntity reg = new MatchRegistrationEntity();
            reg.setMatch(match3);
            reg.setPlayer(player);
            reg.setStatus(PlayerMatchStatus.REGISTERED);
            reg.setTimestamp(LocalDateTime.now());
            reg.setCreatedBy("system");

            matchRegistrationRepository.save(reg);
        }
    }

}
