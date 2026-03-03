package cz.phsoft.hokej.security;

import cz.phsoft.hokej.player.entities.PlayerEntity;

/**
 * Thread-local kontext pro uchování aktuálně zvoleného hráče.
 *
 * Třída slouží k uložení instance PlayerEntity, která je považována
 * za aktuálního hráče v rámci jednoho HTTP requestu.
 *
 * Kontext je:
 * - nastaven na začátku requestu ve filtru CurrentPlayerFilter,
 * - dostupný v celém call stacku, například v controlleru a service vrstvě,
 * - vyčištěn po dokončení zpracování requestu.
 *
 * Použití ThreadLocal zajišťuje, že každý HTTP request
 * má vlastní instanci kontextu a nedochází ke sdílení dat
 * mezi paralelně zpracovávanými požadavky.
 *
 * Kontext musí být vždy vyčištěn metodou clear,
 * jinak hrozí únik paměti a přenášení dat mezi jednotlivými requesty.
 *
 * Třída je navržena jako utilitní a nelze ji instancovat.
 */
public final class CurrentPlayerContext {

    /**
     * ThreadLocal uchovávající aktuálního hráče
     * pro právě zpracovávaný request.
     */
    private static final ThreadLocal<PlayerEntity> currentPlayer = new ThreadLocal<>();

    /**
     * Soukromý konstruktor brání vytvoření instance třídy.
     *
     * Třída slouží výhradně jako statický kontext.
     */
    private CurrentPlayerContext() {
        // Utility třída, instanci nelze vytvořit
    }

    /**
     * Nastaví aktuálního hráče do thread-local kontextu.
     *
     * Metoda se volá typicky ve filtru CurrentPlayerFilter
     * na začátku zpracování HTTP requestu.
     *
     * @param player hráč zvolený jako aktuální pro daný request
     */
    public static void set(PlayerEntity player) {
        currentPlayer.set(player);
    }

    /**
     * Vrátí aktuálního hráče pro právě zpracovávaný request.
     *
     * Pokud hráč nebyl v rámci requestu nastaven,
     * je vrácena hodnota null.
     *
     * @return instance PlayerEntity nebo null
     */
    public static PlayerEntity get() {
        return currentPlayer.get();
    }

    /**
     * Vyčistí thread-local kontext.
     *
     * Metoda musí být vždy volána po dokončení requestu,
     * typicky ve finally bloku filtru.
     *
     * Použití ThreadLocal.remove uvolňuje referenci
     * a zabraňuje únikům paměti při opakovaném použití vláken.
     */
    public static void clear() {
        currentPlayer.remove();
    }
}