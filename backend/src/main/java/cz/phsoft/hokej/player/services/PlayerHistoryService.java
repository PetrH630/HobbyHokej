package cz.phsoft.hokej.player.services;


import cz.phsoft.hokej.player.dto.PlayerHistoryDTO;

import java.util.List;

/**
 * Servisní rozhraní pro práci s historií hráčů.
 *
 * Slouží pouze pro čtení auditních záznamů.
 */
public interface PlayerHistoryService {

    /**
     * Vrátí historii daného hráče.
     *
     * @param playerId ID hráče
     * @return seznam historických záznamů od nejnovějšího po nejstarší
     */
    List<PlayerHistoryDTO> getHistoryForPlayer(Long playerId);
}
