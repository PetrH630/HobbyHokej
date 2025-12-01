/*

package cz.phsoft.hokej.config;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Component
public class DataInitializer {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;

    public DataInitializer(PlayerRepository playerRepository, MatchRepository matchRepository) {
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
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
            match.setDescription(""); // prázdný popis
            matchRepository.save(match);
        }
    }
}
*/