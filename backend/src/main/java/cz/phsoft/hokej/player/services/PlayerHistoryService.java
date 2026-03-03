package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerHistoryDTO;

import java.util.List;

/**
 * Rozhraní servisní vrstvy pro práci s historií hráčů.
 *
 * Definuje kontrakt pro čtení auditních záznamů změn nad hráči.
 * Rozhraní neposkytuje žádné změnové operace a slouží výhradně
 * pro přístup k historickým datům.
 *
 * Implementace zajišťuje načtení historických záznamů
 * z perzistentní vrstvy a jejich převod do DTO reprezentace.
 */
public interface PlayerHistoryService {

    /**
     * Vrátí historii změn daného hráče.
     *
     * Historické záznamy jsou vráceny v pořadí
     * od nejnovější změny po nejstarší.
     *
     * @param playerId identifikátor hráče
     * @return seznam historických záznamů ve formě PlayerHistoryDTO
     */
    List<PlayerHistoryDTO> getHistoryForPlayer(Long playerId);
}