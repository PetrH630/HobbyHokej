package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    // --- podle data bez ohledu na sezónu ---
    List<MatchEntity> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);

    List<MatchEntity> findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime dateTime);


    // --- všechny zápasy v konkrétní sezóně ---
    List<MatchEntity> findAllBySeasonIdOrderByDateTimeAsc(Long seasonId);

    List<MatchEntity> findAllBySeasonIdOrderByDateTimeDesc(Long seasonId);


    // --- zápasy v sezóně po daném čase (používané ve službě) ---
    List<MatchEntity> findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
            Long seasonId,
            LocalDateTime from
    );

    // --- zápasy v sezóně před daným časem (používané ve službě) ---
    List<MatchEntity> findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
            Long seasonId,
            LocalDateTime to
    );

    // (VOLITELNÉ) pokud bys někdy chtěl >= místo >
    // List<MatchEntity> findAllBySeasonIdAndDateTimeGreaterThanEqualOrderByDateTimeAsc(
    //         Long seasonId,
    //         LocalDateTime from
    // );
}
