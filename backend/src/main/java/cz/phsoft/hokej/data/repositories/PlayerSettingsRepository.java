package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s nastavením hráče (PlayerSettingsEntity).
 */
public interface PlayerSettingsRepository extends JpaRepository<PlayerSettingsEntity, Long> {

    Optional<PlayerSettingsEntity> findByPlayer(PlayerEntity player);

    Optional<PlayerSettingsEntity> findByPlayerId(Long playerId);

    boolean existsByPlayer(PlayerEntity player);
}
