package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.models.dto.*;

import java.util.List;

/**
 * Rozhraní pro správu zápasů v aplikaci.
 * <p>
 * Definuje kontrakt pro práci se zápasy z pohledu business logiky –
 * vytváření, úpravu, mazání, získávání přehledů a práci s dostupností
 * pro konkrétního hráče.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>poskytnout jednotný vstupní bod pro práci se zápasy,</li>
 *     <li>oddělit business logiku zápasů od persistence vrstvy,</li>
 *     <li>nabídnout specializované přehledy pro hráče i administraci.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v controllerech a plánovačích (scheduler),</li>
 *     <li>pracuje výhradně s DTO objekty pro přenos dat.</li>
 * </ul>
 */
public interface MatchService {

    /**
     * Vrátí seznam všech zápasů v systému.
     * <p>
     * Metoda typicky slouží pro administrátorské přehledy
     * nebo globální seznam zápasů.
     * </p>
     *
     * @return seznam všech zápasů ve formě {@link MatchDTO}
     */
    List<MatchDTO> getAllMatches();

    /**
     * Vrátí seznam všech nadcházejících zápasů.
     * <p>
     * Nadcházející zápasy jsou ty, které mají datum v budoucnosti
     * (podle interně zvolené definice – např. datum/čas &gt;= nyní).
     * </p>
     *
     * @return seznam nadcházejících zápasů
     */
    List<MatchDTO> getUpcomingMatches();

    /**
     * Vrátí seznam všech již odehraných zápasů.
     *
     * @return seznam minulých zápasů
     */
    List<MatchDTO> getPastMatches();

    /**
     * Vrátí nejbližší nadcházející zápas.
     * <p>
     * Typicky se používá pro rychlé zobrazení „dalšího zápasu“
     * na dashboardu nebo v notifikacích.
     * </p>
     *
     * @return nejbližší nadcházející zápas nebo {@code null},
     *         pokud žádný neexistuje
     */
    MatchDTO getNextMatch();

    /**
     * Vrátí detail zápasu podle jeho ID.
     *
     * @param id ID zápasu
     * @return zápas ve formě {@link MatchDTO}
     */
    MatchDTO getMatchById(Long id);

    /**
     * Vytvoří nový zápas.
     * <p>
     * Metoda typicky dostupná pouze pro administrátory / manažery.
     * </p>
     *
     * @param dto data nového zápasu
     * @return vytvořený zápas
     */
    MatchDTO createMatch(MatchDTO dto);

    /**
     * Aktualizuje existující zápas.
     *
     * @param id  ID zápasu, který má být upraven
     * @param dto nové hodnoty pro zápas
     * @return aktualizovaný zápas
     */
    MatchDTO updateMatch(Long id, MatchDTO dto);

    /**
     * Smaže zápas podle ID.
     * <p>
     * Typicky vrací informaci o úspěchu operace
     * ve formě {@link SuccessResponseDTO}.
     * </p>
     *
     * @param id ID zápasu, který má být smazán
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO deleteMatch(Long id);

    /**
     * Vrátí detailní informace o zápasu.
     * <p>
     * Na rozdíl od {@link #getMatchById(Long)} může obsahovat
     * rozšířené informace (např. registrace, statistiky, atd.).
     * </p>
     *
     * @param id ID zápasu
     * @return detail zápasu
     */
    MatchDetailDTO getMatchDetail(Long id);

    /**
     * Vrátí seznam zápasů, na které se daný hráč může registrovat.
     * <p>
     * Typicky filtruje:
     * </p>
     * <ul>
     *     <li>pouze nadcházející zápasy,</li>
     *     <li>zápasy, kde ještě není plná kapacita,</li>
     *     <li>zápasy v rámci povolené sezóny / pravidel.</li>
     * </ul>
     *
     * @param playerId ID hráče
     * @return seznam dostupných zápasů pro hráče
     */
    List<MatchDTO> getAvailableMatchesForPlayer(Long playerId);

    /**
     * Vrátí nadcházející zápasy pro konkrétního hráče.
     * <p>
     * Může zahrnovat:
     * </p>
     * <ul>
     *     <li>zápasy, na které je hráč přihlášen,</li>
     *     <li>případně další omezené podle business pravidel.</li>
     * </ul>
     *
     * @param playerId ID hráče
     * @return seznam nadcházejících zápasů pro daného hráče
     */
    List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId);

    /**
     * Najde ID hráče podle emailu uživatele.
     * <p>
     * Pomocná metoda pro případy, kdy je k dispozici email
     * přihlášeného uživatele a je potřeba zjistit navázaného hráče.
     * </p>
     *
     * @param email email uživatele
     * @return ID hráče nebo {@code null}, pokud neexistuje
     */
    Long getPlayerIdByEmail(String email);

    /**
     * Vrátí přehled nadcházejících zápasů pro hráče.
     * <p>
     * Na rozdíl od {@link #getUpcomingMatchesForPlayer(Long)} může
     * poskytovat zjednodušený nebo agregovaný pohled (overview)
     * vhodný pro seznamy a dashboardy.
     * </p>
     *
     * @param playerId ID hráče
     * @return přehled nadcházejících zápasů pro daného hráče
     */
    List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId);

    /**
     * Vrátí přehled všech odehraných zápasů pro hráče.
     * <p>
     * Využívá se pro statistiky, historii účasti a přehled
     * minulých zápasů daného hráče.
     * </p>
     *
     * @param playerId ID hráče
     * @return přehled všech odehraných zápasů pro daného hráče
     */
    List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId);

    /**
     * Označí hráče u daného zápasu jako „neomluveného“.
     * <p>
     * Typicky používáno v administrátorském kontextu po zápase,
     * kdy je potřeba vyhodnotit docházku hráče.
     * </p>
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote poznámka administrátora
     * @return aktualizovaná registrace hráče k zápasu
     */
    MatchRegistrationDTO markNoExcused(Long matchId, Long playerId, String adminNote);

    /**
     * Zruší zápas a nastaví důvod zrušení.
     * <p>
     * Zápas je označen jako zrušený s uvedeným důvodem.
     * Implementace může navazovat další logiku
     * (např. notifikace hráčů).
     * </p>
     *
     * @param matchId ID zápasu
     * @param reason  důvod zrušení
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason);

    /**
     * Obnoví dříve zrušený zápas.
     * <p>
     * Zápas se vrátí do stavu, kdy je opět platný a může se konat,
     * pokud to dovolují ostatní podmínky (např. datum, kapacita).
     * </p>
     *
     * @param matchId ID zápasu
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO unCancelMatch(Long matchId);

}
