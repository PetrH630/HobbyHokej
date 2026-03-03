package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;

import java.util.List;

/**
 * Servisní rozhraní pro čtecí operace nad zápasy.
 *
 * Tato vrstva poskytuje pouze operace pro načítání dat.
 * Neprovádí žádné změny stavu databáze ani nespouští notifikace.
 *
 * Slouží jako čtecí část architektury oddělené podle principu CQRS.
 */
public interface MatchQueryService {

    /**
     * Vrací seznam všech zápasů aktuální sezóny.
     *
     * @return seznam zápasů jako MatchDTO
     */
    List<MatchDTO> getAllMatches();

    /**
     * Vrací seznam všech nadcházejících zápasů aktuální sezóny.
     *
     * @return seznam budoucích zápasů
     */
    List<MatchDTO> getUpcomingMatches();

    /**
     * Vrací seznam všech odehraných zápasů aktuální sezóny.
     *
     * @return seznam minulých zápasů
     */
    List<MatchDTO> getPastMatches();

    /**
     * Vrací nejbližší nadcházející zápas.
     *
     * Pokud žádný budoucí zápas neexistuje, vrací se null.
     *
     * @return nejbližší budoucí zápas nebo null
     */
    MatchDTO getNextMatch();

    /**
     * Vrací základní informace o zápasu podle jeho identifikátoru.
     *
     * @param id identifikátor zápasu
     * @return zápas jako MatchDTO
     */
    MatchDTO getMatchById(Long id);

    /**
     * Vrací detail zápasu.
     *
     * Detail obsahuje agregované informace o registracích,
     * týmech a stavu hráče vůči zápasu.
     *
     * @param id identifikátor zápasu
     * @return detail zápasu jako MatchDetailDTO
     */
    MatchDetailDTO getMatchDetail(Long id);

    /**
     * Vrací seznam zápasů dostupných pro konkrétního hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam dostupných zápasů
     */
    List<MatchDTO> getAvailableMatchesForPlayer(Long playerId);

    /**
     * Vrací identifikátor hráče podle e-mailu uživatele.
     *
     * @param email e-mail uživatele
     * @return identifikátor hráče
     */
    Long getPlayerIdByEmail(String email);

    /**
     * Vrací přehled nadcházejících zápasů pro hráče.
     *
     * Výstup je optimalizován pro zobrazení v přehledech.
     *
     * @param playerId identifikátor hráče
     * @return seznam přehledových DTO
     */
    List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId);

    /**
     * Vrací nadcházející zápasy pro konkrétního hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam zápasů
     */
    List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId);

    /**
     * Vrací přehled všech odehraných zápasů hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam přehledových DTO minulých zápasů
     */
    List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId);
}