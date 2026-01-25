package cz.phsoft.hokej.data.repositories;


import cz.phsoft.hokej.data.entities.SeasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<SeasonEntity, Long> {

    Optional<SeasonEntity> findByActiveTrue();

    List<SeasonEntity> findAllByOrderByStartDateAsc();

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);

    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
            LocalDate endDate,
            LocalDate startDate,
            Long id);

    long countByActiveTrue();
}
