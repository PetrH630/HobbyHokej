package cz.phsoft.hokej.player.services;

/**
 * Rozhraní pro správu aktuálně zvoleného hráče přihlášeného uživatele.
 *
 * Uživatel může mít v systému více hráčů, ale většina aplikačních operací
 * (registrace na zápasy, přehledy, statistiky) pracuje vždy s jedním
 * jednoznačně určeným hráčem. Toto rozhraní definuje jednotný kontrakt
 * pro práci s tímto kontextem napříč aplikací.
 *
 * Rozhraní odděluje práci s uživatelským kontextem od business logiky.
 * Konkrétní implementace obvykle ukládá identifikátor hráče do uživatelské
 * session nebo jiného kontextu. Ověření existence a stavu hráče je
 * odpovědností implementace.
 */
public interface CurrentPlayerService {

    /**
     * Vrátí identifikátor aktuálně zvoleného hráče.
     *
     * Pokud aktuální hráč není nastaven, vrací se hodnota null.
     *
     * @return identifikátor hráče nebo null, pokud není aktuální hráč nastaven
     */
    Long getCurrentPlayerId();

    /**
     * Nastaví aktuálního hráče v uživatelském kontextu.
     *
     * Metoda slouží ke změně kontextu přihlášeného uživatele
     * na konkrétního hráče. Implementace je odpovědná za to,
     * aby byl zvolen pouze platný hráč v odpovídajícím stavu.
     *
     * @param playerId identifikátor hráče, který má být nastaven jako aktuální
     */
    void setCurrentPlayerId(Long playerId);

    /**
     * Ověří, že je aktuální hráč nastaven.
     *
     * Metoda se používá před operacemi, které vyžadují kontext
     * aktuálně zvoleného hráče. Pokud hráč není zvolen, je
     * vyhozena výjimka signalizující neplatný kontext.
     *
     * @throws RuntimeException pokud aktuální hráč není nastaven
     */
    void requireCurrentPlayer();

    /**
     * Odstraní informaci o aktuálním hráči z uživatelského kontextu.
     *
     * Metoda se používá například při odhlášení uživatele nebo při
     * explicitním resetu uživatelského kontextu.
     */
    void clear();
}