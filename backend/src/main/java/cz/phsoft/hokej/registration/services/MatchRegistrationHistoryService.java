package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.dto.MatchRegistrationHistoryDTO;

import java.util.List;

/**
 * Service se používá pro práci s historickými (auditními) záznamy
 * registrací hráčů k zápasům.
 *
 * Tato service je čistě pro čtení. Pracuje s historickými daty,
 * neprovádí žádné změny v databázi a neobsahuje vlastní business logiku.
 * Slouží k oddělení auditních dotazů od hlavní logiky registrací.
 *
 * Typickým použitím je zobrazení historie změn registrace
 * aktuálně přihlášeného hráče nebo provádění administrativního auditu
 * registrací konkrétního hráče k danému zápasu.
 */
public interface MatchRegistrationHistoryService {

    /**
     * Vrátí historii všech změn registrace aktuálně přihlášeného hráče
     * pro zadaný zápas.
     *
     * Metoda pracuje s kontextem aktuálního hráče
     * a vrací pouze záznamy, které se k tomuto hráči vztahují.
     * Historie je seřazena sestupně podle času změny, takže
     * nejnovější změna je na prvním místě.
     *
     * @param matchId ID zápasu, ke kterému se historie načítá
     * @return seznam historických záznamů registrace hráče k zápasu
     */
    List<MatchRegistrationHistoryDTO> getHistoryForCurrentPlayerAndMatch(Long matchId);

    /**
     * Vrátí historii všech změn registrace zadaného hráče
     * k danému zápasu.
     *
     * Metoda se používá zejména pro administrativní a auditní účely,
     * například při kontrole zásahů do registrací hráčů
     * nebo při řešení sporů a reklamací.
     * Historie je seřazena sestupně podle času změny.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam historických záznamů registrace hráče k zápasu
     */
    List<MatchRegistrationHistoryDTO> getHistoryForPlayerAndMatch(Long matchId, Long playerId);
}
