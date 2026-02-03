package cz.phsoft.hokej.data.repositories;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repozitář pro práci s entitou PlayerSettingsEntity.
 *
 * Slouží k načítání a správě nastavení hráčů
 * (kontakty, notifikační preference).
 */
public interface PlayerSettingsRepository extends JpaRepository<PlayerSettingsEntity, Long> {

    /**
     * Vyhledá nastavení podle hráče.
     *
     * @param player hráč
     * @return nastavení zabalené v Optional, pokud existuje
     */
    Optional<PlayerSettingsEntity> findByPlayer(PlayerEntity player);

    /**
     * Vyhledá nastavení podle ID hráče.
     *
     * @param playerId ID hráče
     * @return nastavení zabalené v Optional, pokud existuje
     */
    Optional<PlayerSettingsEntity> findByPlayerId(Long playerId);

    /**
     * Ověří, zda existuje záznam nastavení pro daného hráče.
     *
     * @param player hráč
     * @return true, pokud nastavení existuje
     */
    boolean existsByPlayer(PlayerEntity player);
}
