package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.SeasonHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeasonHistoryRepository
        extends JpaRepository<SeasonHistoryEntity, Long> {

    List<SeasonHistoryEntity> findBySeasonIdOrderByChangedAtDesc(Long seasonId);
}
