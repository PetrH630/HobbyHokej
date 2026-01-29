package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.models.dto.PlayerSettingsDTO;

/**
 * Service pro práci s nastavením hráče (PlayerSettingsEntity).
 *
 * Řeší:
 * - načtení nastavení konkrétního hráče,
 * - aktualizaci nastavení konkrétního hráče,
 * - případně nastavení pro aktuálního hráče (currentPlayer).
 */
public interface PlayerSettingsService {

    /**
     * Vrátí nastavení pro hráče podle jeho ID.
     * Pokud nastavení ještě neexistuje, vytvoří se s default hodnotami.
     *
     * Autorizace (vlastnictví hráče / role admin) se řeší
     * na úrovni controlleru nebo vyšší servisní vrstvy.
     */
    PlayerSettingsDTO getSettingsForPlayer(Long playerId);

    /**
     * Aktualizuje nastavení pro hráče podle jeho ID.
     *
     * @param playerId ID hráče
     * @param dto      nové nastavení
     * @return aktualizované nastavení
     */
    PlayerSettingsDTO updateSettingsForPlayer(Long playerId, PlayerSettingsDTO dto);

    PlayerSettingsEntity createDefaultSettingsForPlayer(PlayerEntity player);
}
