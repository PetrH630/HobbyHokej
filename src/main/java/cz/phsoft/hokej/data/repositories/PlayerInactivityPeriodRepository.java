
package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerInactivityPeriod;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayerInactivityPeriodRepository extends JpaRepository<PlayerInactivityPeriod, Long> {

    // zjistí, zda hráč je aktuálně neaktivní
    boolean existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
            PlayerEntity player, LocalDateTime from, LocalDateTime to);

    // získá všechny neaktivní období hráče, které spadají do intervalu
    List<PlayerInactivityPeriod> findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
            PlayerEntity player, LocalDateTime from, LocalDateTime to);
}
