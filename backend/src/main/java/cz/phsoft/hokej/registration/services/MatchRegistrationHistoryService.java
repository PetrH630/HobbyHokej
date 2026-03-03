package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.dto.MatchRegistrationHistoryDTO;

import java.util.List;

/**
 * Service vrstva pro práci s historickými auditními záznamy
 * registrací hráčů k zápasům.
 *
 * Rozhraní je určeno výhradně pro čtecí operace nad historickými daty.
 * Neprovádí žádné změny v databázi a neobsahuje aplikační logiku
 * měnící aktuální stav registrací.
 *
 * Slouží k oddělení auditních dotazů od hlavní příkazové a čtecí logiky
 * registrací. Typickým použitím je zobrazení historie změn registrace
 * hráče nebo provádění administrativního auditu konkrétní registrace.
 */
public interface MatchRegistrationHistoryService {

    /**
     * Vrátí historii všech změn registrace aktuálně přihlášeného hráče
     * pro zadaný zápas.
     *
     * Metoda využívá bezpečnostní kontext aplikace pro určení
     * aktuálně přihlášeného hráče a vrací pouze záznamy vztahující se
     * k tomuto hráči a danému zápasu. Výsledky jsou seřazeny sestupně
     * podle času změny tak, aby byla nejnovější změna uvedena jako první.
     *
     * @param matchId identifikátor zápasu, ke kterému se historie načítá
     * @return seznam historických záznamů registrace aktuálního hráče k zápasu
     */
    List<MatchRegistrationHistoryDTO> getHistoryForCurrentPlayerAndMatch(Long matchId);

    /**
     * Vrátí historii všech změn registrace zadaného hráče
     * k danému zápasu.
     *
     * Metoda se používá zejména pro administrativní a auditní účely,
     * například při kontrole zásahů do registrací nebo při řešení
     * sporů a reklamací. Výsledky jsou seřazeny sestupně podle času změny,
     * aby byla nejnovější změna uvedena na prvním místě.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @return seznam historických záznamů registrace hráče k zápasu
     */
    List<MatchRegistrationHistoryDTO> getHistoryForPlayerAndMatch(Long matchId, Long playerId);
}