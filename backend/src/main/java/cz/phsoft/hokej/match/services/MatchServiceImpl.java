package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fasádní implementace servisní vrstvy pro práci se zápasy.
 *
 * Třída implementuje rozhraní MatchService a zachovává jednotné API
 * pro controller vrstvu. Veškerá business logika je delegována
 * do specializovaných služeb:
 *
 * - MatchQueryService pro čtecí operace,
 * - MatchCommandService pro změnové operace.
 *
 * Implementace respektuje princip oddělení čtení a zápisu
 * podle architektonického přístupu CQRS.
 */
@Service
public class MatchServiceImpl implements MatchService {

    private final MatchQueryService matchQueryService;
    private final MatchCommandService matchCommandService;

    /**
     * Vytváří fasádní službu pro práci se zápasy.
     *
     * @param matchQueryService služba odpovědná za čtecí operace
     * @param matchCommandService služba odpovědná za změnové operace
     */
    public MatchServiceImpl(
            MatchQueryService matchQueryService,
            MatchCommandService matchCommandService
    ) {
        this.matchQueryService = matchQueryService;
        this.matchCommandService = matchCommandService;
    }

    /**
     * Vrací seznam všech zápasů v systému.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @return seznam všech zápasů jako MatchDTO
     */
    @Override
    public List<MatchDTO> getAllMatches() {
        return matchQueryService.getAllMatches();
    }

    /**
     * Vrací seznam všech nadcházejících zápasů.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @return seznam budoucích zápasů jako MatchDTO
     */
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchQueryService.getUpcomingMatches();
    }

    /**
     * Vrací seznam všech odehraných zápasů.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @return seznam minulých zápasů jako MatchDTO
     */
    @Override
    public List<MatchDTO> getPastMatches() {
        return matchQueryService.getPastMatches();
    }

    /**
     * Vrací nejbližší nadcházející zápas.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @return nejbližší budoucí zápas nebo null
     */
    @Override
    public MatchDTO getNextMatch() {
        return matchQueryService.getNextMatch();
    }

    /**
     * Vrací základní informace o zápasu podle jeho ID.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param id identifikátor zápasu
     * @return zápas jako MatchDTO
     */
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchQueryService.getMatchById(id);
    }

    /**
     * Vytváří nový zápas.
     *
     * Implementace deleguje změnovou operaci do služby MatchCommandService.
     *
     * @param dto data nového zápasu
     * @return vytvořený zápas jako MatchDTO
     */
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        return matchCommandService.createMatch(dto);
    }

    /**
     * Aktualizuje existující zápas.
     *
     * Implementace deleguje změnovou operaci do služby MatchCommandService.
     *
     * @param id identifikátor zápasu
     * @param dto nové hodnoty zápasu
     * @return aktualizovaný zápas jako MatchDTO
     */
    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        return matchCommandService.updateMatch(id, dto);
    }

    /**
     * Odstraňuje zápas ze systému.
     *
     * Implementace deleguje změnovou operaci do služby MatchCommandService.
     *
     * @param id identifikátor zápasu
     * @return potvrzení operace ve formě SuccessResponseDTO
     */
    @Override
    public SuccessResponseDTO deleteMatch(Long id) {
        return matchCommandService.deleteMatch(id);
    }

    /**
     * Ruší zápas a nastavuje důvod zrušení.
     *
     * Operace je vedena jako transakční, protože dochází ke změně
     * stavu zápasu a případně dalších souvisejících údajů.
     * Logika je delegována do služby MatchCommandService.
     *
     * @param matchId identifikátor zápasu
     * @param reason důvod zrušení
     * @return potvrzení operace
     */
    @Override
    @Transactional
    public SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason) {
        return matchCommandService.cancelMatch(matchId, reason);
    }

    /**
     * Obnovuje dříve zrušený zápas.
     *
     * Operace je vedena jako transakční a delegována
     * do služby MatchCommandService.
     *
     * @param matchId identifikátor zápasu
     * @return potvrzení operace
     */
    @Override
    @Transactional
    public SuccessResponseDTO unCancelMatch(Long matchId) {
        return matchCommandService.unCancelMatch(matchId);
    }

    /**
     * Aktualizuje skóre zápasu.
     *
     * Implementace deleguje změnovou operaci do služby MatchCommandService.
     *
     * @param matchId identifikátor zápasu
     * @param scoreLight počet branek týmu LIGHT
     * @param scoreDark počet branek týmu DARK
     * @return aktualizovaný zápas jako MatchDTO
     */
    @Override
    public MatchDTO updateMatchScore(Long matchId, Integer scoreLight, Integer scoreDark) {
        return matchCommandService.updateMatchScore(matchId, scoreLight, scoreDark);
    }

    /**
     * Vrací detail zápasu.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param id identifikátor zápasu
     * @return detail zápasu jako MatchDetailDTO
     */
    @Override
    public MatchDetailDTO getMatchDetail(Long id) {
        return matchQueryService.getMatchDetail(id);
    }

    /**
     * Vrací seznam zápasů dostupných pro konkrétního hráče.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param playerId identifikátor hráče
     * @return seznam dostupných zápasů
     */
    @Override
    public List<MatchDTO> getAvailableMatchesForPlayer(Long playerId) {
        return matchQueryService.getAvailableMatchesForPlayer(playerId);
    }

    /**
     * Vrací identifikátor hráče podle e-mailu.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param email e-mail uživatele
     * @return identifikátor hráče nebo null
     */
    @Override
    public Long getPlayerIdByEmail(String email) {
        return matchQueryService.getPlayerIdByEmail(email);
    }

    /**
     * Vrací přehled nadcházejících zápasů pro hráče.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param playerId identifikátor hráče
     * @return seznam přehledových DTO
     */
    @Override
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId) {
        return matchQueryService.getUpcomingMatchesOverviewForPlayer(playerId);
    }

    /**
     * Vrací nadcházející zápasy pro konkrétního hráče.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param playerId identifikátor hráče
     * @return seznam nadcházejících zápasů
     */
    @Override
    public List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId) {
        return matchQueryService.getUpcomingMatchesForPlayer(playerId);
    }

    /**
     * Vrací přehled všech odehraných zápasů hráče.
     *
     * Implementace deleguje čtecí operaci do služby MatchQueryService.
     *
     * @param playerId identifikátor hráče
     * @return seznam přehledových DTO minulých zápasů
     */
    @Override
    public List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId) {
        return matchQueryService.getAllPassedMatchesForPlayer(playerId);
    }
}