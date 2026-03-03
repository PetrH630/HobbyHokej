package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;
import cz.phsoft.hokej.match.enums.MatchCancelReason;

import java.util.List;

/**
 * Hlavní servisní rozhraní pro správu zápasů.
 *
 * Rozhraní definuje kompletní business API pro práci se zápasy v aplikaci.
 * Slouží jako kontrakt mezi controller vrstvou a implementacemi servisní vrstvy.
 * Implementace typicky využívá rozdělení na čtecí (query) a změnové (command) služby.
 */
public interface MatchService {

    /**
     * Vrací seznam všech zápasů v systému.
     *
     * Metoda se používá pro administrativní přehled zápasů bez ohledu na čas konání
     * a stav zápasu.
     *
     * @return seznam všech zápasů reprezentovaných pomocí MatchDTO
     */
    List<MatchDTO> getAllMatches();

    /**
     * Vrací seznam všech nadcházejících zápasů.
     *
     * Do výběru jsou zahrnuty pouze zápasy s datem konání v budoucnosti.
     *
     * @return seznam nadcházejících zápasů jako MatchDTO
     */
    List<MatchDTO> getUpcomingMatches();

    /**
     * Vrací seznam všech již odehraných zápasů.
     *
     * Do výběru jsou zahrnuty zápasy s datem konání v minulosti.
     *
     * @return seznam minulých zápasů jako MatchDTO
     */
    List<MatchDTO> getPastMatches();

    /**
     * Vrací nejbližší nadcházející zápas.
     *
     * Pokud žádný budoucí zápas neexistuje, může být vrácena hodnota null.
     *
     * @return nejbližší nadcházející zápas jako MatchDTO nebo null
     */
    MatchDTO getNextMatch();

    /**
     * Vrací detail zápasu dle jeho identifikátoru v administrativním pohledu.
     *
     * Metoda nebere v úvahu kontext konkrétního hráče.
     *
     * @param id identifikátor zápasu
     * @return zápas jako MatchDTO
     */
    MatchDTO getMatchById(Long id);

    /**
     * Vytváří nový zápas na základě vstupního DTO.
     *
     * Metoda provádí základní validace a předává data do persistence vrstvy.
     *
     * @param dto vstupní DTO s definicí nového zápasu
     * @return vytvořený zápas jako MatchDTO včetně přiděleného ID
     */
    MatchDTO createMatch(MatchDTO dto);

    /**
     * Aktualizuje existující zápas podle předaného DTO.
     *
     * @param id identifikátor aktualizovaného zápasu
     * @param dto DTO obsahující nové hodnoty pro zápas
     * @return aktualizovaný zápas jako MatchDTO
     */
    MatchDTO updateMatch(Long id, MatchDTO dto);

    /**
     * Odstraňuje zápas ze systému.
     *
     * Způsob mazání (hard delete, soft delete) je dán implementací.
     *
     * @param id identifikátor zápasu
     * @return objekt SuccessResponseDTO s potvrzením operace
     */
    SuccessResponseDTO deleteMatch(Long id);

    /**
     * Vrací detail zápasu z pohledu hráče.
     *
     * Výstup může obsahovat informace o registracích, týmech a stavu
     * konkrétního hráče vůči danému zápasu.
     *
     * @param id identifikátor zápasu
     * @return detail zápasu jako MatchDetailDTO
     */
    MatchDetailDTO getMatchDetail(Long id);

    /**
     * Vrací seznam zápasů dostupných pro registraci konkrétního hráče.
     *
     * Zohledňují se pravidla pro dostupnost, čas konání a případná omezení
     * pro daného hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam dostupných zápasů jako MatchDTO
     */
    List<MatchDTO> getAvailableMatchesForPlayer(Long playerId);

    /**
     * Vrací seznam nadcházejících zápasů pro konkrétního hráče.
     *
     * Může být filtrováno podle registrace, týmů nebo jiných podmínek
     * daných implementací.
     *
     * @param playerId identifikátor hráče
     * @return seznam nadcházejících zápasů pro hráče jako MatchDTO
     */
    List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId);

    /**
     * Zjišťuje identifikátor hráče na základě e-mailu uživatele.
     *
     * Používá se jako pomocná metoda pro navázání identity uživatele
     * a jeho hráčského profilu.
     *
     * @param email e-mail uživatele
     * @return identifikátor hráče nebo null, pokud hráč neexistuje
     */
    Long getPlayerIdByEmail(String email);

    /**
     * Vrací přehled nadcházejících zápasů pro hráče v přehledové podobě.
     *
     * DTO MatchOverviewDTO je optimalizováno pro zobrazení v seznamech
     * a přehledech na úvodních obrazovkách.
     *
     * @param playerId identifikátor hráče
     * @return seznam nadcházejících zápasů jako MatchOverviewDTO
     */
    List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId);

    /**
     * Vrací přehled všech odehraných zápasů daného hráče.
     *
     * Používá se pro osobní historii zápasů z pohledu konkrétního hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam odehraných zápasů jako MatchOverviewDTO
     */
    List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId);

    /**
     * Ruší zápas a nastavuje důvod zrušení.
     *
     * Implementace provádí kontrolu stavu zápasu, nastavuje MatchStatus a
     * ukládá důvod zrušení v podobě MatchCancelReason.
     *
     * @param matchId identifikátor zápasu
     * @param reason důvod zrušení zápasu
     * @return objekt SuccessResponseDTO s potvrzením operace
     */
    SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason);

    /**
     * Obnovuje dříve zrušený zápas.
     *
     * Implementace provádí kontrolu původního stavu a nastavuje vhodný
     * nový stav zápasu.
     *
     * @param matchId identifikátor zápasu
     * @return objekt SuccessResponseDTO s potvrzením operace
     */
    SuccessResponseDTO unCancelMatch(Long matchId);

    /**
     * Aktualizuje skóre zápasu.
     *
     * Zadané hodnoty jsou uloženy do entitního modelu a mohou být
     * použity pro výpočet výsledku a statistik.
     *
     * @param matchId identifikátor zápasu
     * @param scoreLight počet branek týmu LIGHT
     * @param scoreDark počet branek týmu DARK
     * @return aktualizovaný zápas jako MatchDTO
     */
    MatchDTO updateMatchScore(Long matchId, Integer scoreLight, Integer scoreDark);
}