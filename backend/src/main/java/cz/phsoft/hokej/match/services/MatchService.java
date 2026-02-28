package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;
import cz.phsoft.hokej.match.enums.MatchCancelReason;

import java.util.List;

/**
 * Rozhraní se používá pro správu zápasů v aplikaci.
 *
 * Definuje kontrakt pro práci se zápasy z pohledu business logiky.
 * Zajišťuje vytváření, úpravy, mazání zápasů, získávání přehledů
 * a práci s dostupností zápasů pro konkrétního hráče.
 *
 * Rozhraní je navrženo tak, aby oddělovalo business logiku
 * od persistence vrstvy a poskytovalo jednotný vstupní bod
 * pro controllery a např. plánovače (scheduler).
 */
public interface MatchService {

    /**
     * Vrátí seznam všech zápasů v systému.
     *
     * Metoda se typicky používá pro administrátorské přehledy
     * nebo pro globální seznam zápasů v rámci vybrané sezóny.
     *
     * @return seznam všech zápasů ve formě {@link MatchDTO}
     */
    List<MatchDTO> getAllMatches();

    /**
     * Vrátí seznam všech nadcházejících zápasů.
     *
     * Za nadcházející zápasy se považují ty, které mají
     * datum a čas v budoucnosti podle interně zvolených pravidel.
     *
     * @return seznam nadcházejících zápasů
     */
    List<MatchDTO> getUpcomingMatches();

    /**
     * Vrátí seznam všech již odehraných zápasů.
     *
     * Zápasy jsou obvykle řazené od nejnovějšího po nejstarší.
     *
     * @return seznam minulých zápasů
     */
    List<MatchDTO> getPastMatches();

    /**
     * Vrátí nejbližší nadcházející zápas.
     *
     * Metoda se používá například pro zobrazení
     * „dalšího zápasu“ na dashboardu nebo
     * pro potřeby notifikací.
     *
     * @return nejbližší nadcházející zápas nebo {@code null},
     * pokud žádný neexistuje
     */
    MatchDTO getNextMatch();

    /**
     * Vrátí základní informace o zápasu podle jeho ID.
     *
     * @param id ID zápasu
     * @return zápas ve formě {@link MatchDTO}
     */
    MatchDTO getMatchById(Long id);

    /**
     * Vytvoří nový zápas.
     *
     * Metoda je typicky dostupná pouze pro administrátory
     * nebo manažery. Implementace zajišťuje validaci
     * data v rámci aktivní sezóny a přiřazení sezóny k zápasu.
     *
     * @param dto data nového zápasu
     * @return vytvořený zápas
     */
    MatchDTO createMatch(MatchDTO dto);

    /**
     * Aktualizuje existující zápas.
     *
     * Implementace je odpovědná za načtení stávajícího zápasu,
     * přenesení změn z DTO, validaci a uložení výsledného stavu.
     *
     * @param id  ID zápasu, který má být upraven
     * @param dto nové hodnoty pro zápas
     * @return aktualizovaný zápas
     */
    MatchDTO updateMatch(Long id, MatchDTO dto);

    /**
     * Smaže zápas podle ID.
     *
     * Metoda typicky vrací standardizovanou odpověď
     * s informací o úspěchu operace.
     *
     * @param id ID zápasu, který má být smazán
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO deleteMatch(Long id);

    /**
     * Vrátí detailní informace o zápasu.
     *
     * Oproti metodě {@link #getMatchById(Long)} může detail
     * obsahovat rozšířená data, například statistiky,
     * seznamy hráčů v jednotlivých stavech nebo agregované údaje.
     *
     * @param id ID zápasu
     * @return detail zápasu
     */
    MatchDetailDTO getMatchDetail(Long id);

    /**
     * Vrátí seznam zápasů, na které se daný hráč může registrovat.
     *
     * Implementace obvykle filtruje pouze nadcházející zápasy,
     * kontroluje kapacitu a respektuje pravidla sezóny
     * a případná další business omezení.
     *
     * @param playerId ID hráče
     * @return seznam dostupných zápasů pro hráče
     */
    List<MatchDTO> getAvailableMatchesForPlayer(Long playerId);

    /**
     * Vrátí nadcházející zápasy pro konkrétního hráče.
     *
     * Metoda může zohledňovat, zda je hráč registrovaný,
     * případně další business pravidla. Výsledek je
     * určen pro podrobnější zobrazení seznamu zápasů.
     *
     * @param playerId ID hráče
     * @return seznam nadcházejících zápasů pro daného hráče
     */
    List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId);

    /**
     * Najde ID hráče podle e-mailu uživatele.
     *
     * Metoda se používá jako pomocný nástroj v situaci,
     * kdy je k dispozici e-mail přihlášeného uživatele
     * a je potřeba zjistit navázaného hráče.
     *
     * @param email e-mail uživatele
     * @return ID hráče nebo {@code null}, pokud neexistuje
     */
    Long getPlayerIdByEmail(String email);

    /**
     * Vrátí přehled nadcházejících zápasů pro hráče.
     *
     * Přehled slouží pro zobrazení na dashboardu
     * nebo v jednoduchých seznamech, kde se zobrazují
     * základní informace o zápasech včetně stavu
     * daného hráče.
     *
     * @param playerId ID hráče
     * @return přehled nadcházejících zápasů pro daného hráče
     */
    List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId);

    /**
     * Vrátí přehled všech odehraných zápasů pro hráče.
     *
     * Metoda se používá pro statistiky, historii účasti
     * a přehled minulých zápasů daného hráče v rámci sezóny.
     *
     * @param playerId ID hráče
     * @return přehled všech odehraných zápasů pro daného hráče
     */
    List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId);

    /**
     * Zruší zápas a nastaví důvod zrušení.
     *
     * Zápas je označen jako zrušený včetně uvedeného důvodu.
     * Implementace může navazovat další logiku, například
     * odeslání notifikací hráčům.
     *
     * @param matchId ID zápasu
     * @param reason  důvod zrušení
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason);

    /**
     * Obnoví dříve zrušený zápas.
     *
     * Zápas se vrací do stavu, kdy je opět platný a může se konat,
     * pokud jsou splněny ostatní podmínky (datum, kapacita a podobně).
     *
     * @param matchId ID zápasu
     * @return odpověď s výsledkem operace
     */
    SuccessResponseDTO unCancelMatch(Long matchId);

}
