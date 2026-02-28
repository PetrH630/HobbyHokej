package cz.phsoft.hokej.match.services;


import cz.phsoft.hokej.match.dto.MatchHistoryDTO;

import java.util.List;

/**
     * Servisní rozhraní pro práci s historií zápasů.
     *
     * Slouží pouze pro čtení auditních záznamů,
     * zápis se provádí přes databázové triggery.
     */
    public interface MatchHistoryService {

        /**
         * Vrátí historii daného zápasu podle jeho ID.
         *
         * @param matchId ID zápasu z hlavní tabulky matches
         * @return seznam historických záznamů od nejnovějšího po nejstarší
         */
        List<MatchHistoryDTO> getHistoryForMatch(Long matchId);
}
