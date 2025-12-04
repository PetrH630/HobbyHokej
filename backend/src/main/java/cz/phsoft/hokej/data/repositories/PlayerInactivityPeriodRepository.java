
package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PlayerInactivityPeriodRepository extends JpaRepository<PlayerInactivityPeriodEntity, Long> {

    // zjistí, zda hráč je aktuálně neaktivní
    boolean existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
            PlayerEntity player, LocalDateTime from, LocalDateTime to);

    // získá všechny neaktivní období hráče, které spadají do intervalu
    List<PlayerInactivityPeriodEntity> findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
            PlayerEntity player, LocalDateTime from, LocalDateTime to);

    List<PlayerInactivityPeriodEntity> findByPlayerOrderByInactiveFromAsc(PlayerEntity player);
}
