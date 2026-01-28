package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;

import java.util.List;

/**
 * Service pro práci s historickými (auditními) záznamy
 * registrací hráčů k zápasům.
 *
 * <p>
 * Tato service:
 * <ul>
 *     <li>slouží výhradně pro čtení dat (read-only),</li>
 *     <li>pracuje nad tabulkou {@code match_registration_history},</li>
 *     <li>neobsahuje žádnou byznys logiku ani zápis do databáze,</li>
 *     <li>odděluje auditní dotazy od hlavní logiky registrací.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Typické použití:
 * <ul>
 *     <li>zobrazení historie změn registrace přihlášeného hráče,</li>
 *     <li>administrativní audit registrací konkrétního hráče k zápasu.</li>
 * </ul>
 * </p>
 */
public interface MatchRegistrationHistoryService {

    /**
     * Vrátí historii všech změn registrace přihlášeného hráče
     * pro konkrétní zápas.
     *
     * <p>
     * Metoda:
     * <ul>
     *     <li>automaticky určí aktuálně přihlášeného hráče (currentPlayer),</li>
     *     <li>vrací pouze záznamy, které se vztahují k tomuto hráči,</li>
     *     <li>výsledky jsou seřazeny sestupně podle času změny
     *         (nejnovější změna jako první).</li>
     * </ul>
     * </p>
     *
     * @param matchId ID zápasu, ke kterému se historie načítá
     * @return seznam historických záznamů registrace hráče k zápasu
     */
    List<MatchRegistrationHistoryDTO> getHistoryForCurrentPlayerAndMatch(Long matchId);

    /**
     * Vrátí historii všech změn registrace konkrétního hráče
     * k danému zápasu.
     *
     * <p>
     * Metoda je určena zejména pro:
     * <ul>
     *     <li>administrativní a auditní účely,</li>
     *     <li>kontrolu zásahů do registrací hráčů,</li>
     *     <li>řešení sporů a reklamací.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Výsledky jsou seřazeny sestupně podle času změny
     * (nejnovější změna jako první).
     * </p>
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam historických záznamů registrace hráče k zápasu
     */
    List<MatchRegistrationHistoryDTO> getHistoryForPlayerAndMatch(Long matchId, Long playerId);
}
