package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
    List<MatchEntity> findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime dateTime);
    List<MatchEntity> findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime dateTime);

}
