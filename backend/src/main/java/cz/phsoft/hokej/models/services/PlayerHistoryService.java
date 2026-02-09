package cz.phsoft.hokej.models.services;


import cz.phsoft.hokej.models.dto.PlayerHistoryDTO;

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
