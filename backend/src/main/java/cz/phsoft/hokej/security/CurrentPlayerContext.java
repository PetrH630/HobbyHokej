package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;

/**
 * Thread-local kontext pro uchování aktuálně zvoleného hráče.
 *
 * Slouží k uložení instance PlayerEntity, která je považována
 * za aktuálního hráče v rámci jednoho HTTP requestu.
 *
 * Kontext je:
 * - nastaven na začátku requestu ve filtru CurrentPlayerFilter,
 * - dostupný v celém call stacku (controller, service, helper),
 * - vyčištěn po dokončení zpracování requestu.
 *
 * Použití ThreadLocal zajišťuje, že každý HTTP request
 * má vlastní instanci kontextu a nedochází ke sdílení dat
 * mezi paralelně zpracovávanými požadavky.
 *
 * ThreadLocal musí být vždy vyčištěn metodou clear,
 * jinak hrozí únik paměti a nechtěné přenášení dat
 * mezi jednotlivými requesty.
 */
public final class CurrentPlayerContext {

    /**
     * ThreadLocal uchovávající aktuálního hráče
     * pro právě zpracovávaný request.
     */
    private static final ThreadLocal<PlayerEntity> currentPlayer = new ThreadLocal<>();

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
     * @return instance PlayerEntity nebo null,
     * pokud nebyl hráč pro request zvolen
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
     * a zabraňuje memory leakům při opakovaném použití vláken.
     */
    public static void clear() {
        currentPlayer.remove();
    }
}
