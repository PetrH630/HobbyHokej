package cz.phsoft.hokej.models.services;

/**
 * Rozhraní pro správu „aktuálně zvoleného hráče“ přihlášeného uživatele.
 * <p>
 * Uživatel může mít v systému více hráčů, ale většina operací
 * (registrace na zápasy, přehledy, statistiky) pracuje vždy
 * s jedním jednoznačně zvoleným hráčem.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>poskytnout jednotný kontrakt pro práci s aktuálním hráčem,</li>
 *     <li>oddělit práci se session / kontextem od business logiky,</li>
 *     <li>zajistit konzistentní chování napříč aplikací.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se zejména v controllerech a business službách,</li>
 *     <li>typicky v kontextu endpointů pracujících s „/me“.</li>
 * </ul>
 *
 * Implementační poznámky:
 * <ul>
 *     <li>implementace typicky ukládá identifikátor hráče do uživatelského kontextu
 *     (např. HTTP session),</li>
 *     <li>ověření existence a stavu hráče je zodpovědností implementace.</li>
 * </ul>
 */
public interface CurrentPlayerService {

    /**
     * Vrátí ID aktuálně zvoleného hráče.
     *
     * @return ID hráče nebo {@code null}, pokud aktuální hráč není nastaven
     */
    Long getCurrentPlayerId();

    /**
     * Nastaví aktuálního hráče.
     * <p>
     * Metoda slouží ke změně uživatelského kontextu na konkrétního hráče.
     * Implementace je zodpovědná za validaci, že hráč může být zvolen
     * (např. že existuje a je ve správném stavu).
     * </p>
     *
     * @param playerId ID hráče, který má být nastaven jako aktuální
     */
    void setCurrentPlayerId(Long playerId);

    /**
     * Ověří, že je aktuální hráč nastaven.
     * <p>
     * Používá se zejména před operacemi, které vyžadují kontext
     * aktuálního hráče.
     * </p>
     *
     * @throws RuntimeException pokud aktuální hráč není nastaven
     */
    void requireCurrentPlayer();

    /**
     * Odstraní informaci o aktuálním hráči z uživatelského kontextu.
     * <p>
     * Typicky se používá při odhlášení uživatele
     * nebo při resetu uživatelského kontextu.
     * </p>
     */
    void clear();
}
