package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRegistrationHistoryRepository extends JpaRepository<MatchRegistrationHistoryEntity, Long> {

    // všechny registrace na zápas
    List<MatchRegistrationHistoryEntity> findByMatchRegistrationIdOrderByChangedAtDesc(Long matchRegistrationId);

    // Historie pro konkrétní zápas
    List<MatchRegistrationHistoryEntity> findByMatchIdOrderByChangedAtDesc(Long matchId);

    // Historie změn hráče
    List<MatchRegistrationHistoryEntity> findByPlayerIdOrderByChangedAtDesc(Long playerId);

}