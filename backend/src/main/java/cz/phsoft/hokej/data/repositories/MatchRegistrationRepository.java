package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

    @Repository
    public interface MatchRegistrationRepository extends JpaRepository<MatchRegistrationEntity, Long> {

        // Vrátí poslední status pro daného hráče a zápas
        Boolean existsByPlayerIdAndMatchId(Long playerId, Long matchId);

        // Volitelně: všechny registrace pro určitý zápas
        List<MatchRegistrationEntity> findByMatchId(Long matchId);

        // Volitelně: všechny registrace pro určitého hráče
        List<MatchRegistrationEntity> findByPlayerId(Long playerId);

        // --- Nová metoda pro hledání konkrétní registrace ---
        Optional<MatchRegistrationEntity> findByPlayerIdAndMatchId(Long playerId, Long matchId);

        long countByMatchAndStatus(MatchEntity match, PlayerMatchStatus status);

    }
