package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchHistoryDTO;

import java.util.List;

/**
 * Servisní rozhraní pro práci s historií zápasů.
 *
 * Slouží výhradně pro čtecí operace nad auditní historií.
 * Historické záznamy jsou vytvářeny na úrovni databáze
 * prostřednictvím triggerů, nikoliv aplikační logikou.
 */
public interface MatchHistoryService {

    /**
     * Vrátí historii daného zápasu podle jeho ID.
     *
     * Záznamy jsou vráceny od nejnovější změny po nejstarší.
     *
     * @param matchId identifikátor zápasu z hlavní tabulky
     * @return seznam historických záznamů
     */
    List<MatchHistoryDTO> getHistoryForMatch(Long matchId);
}