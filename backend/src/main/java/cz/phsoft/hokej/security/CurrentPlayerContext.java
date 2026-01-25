package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;

/**
 * Thread-local kontext pro „aktuálního hráče“.
 *
 * Slouží k uchování instance {@link PlayerEntity},
 * která je zvolená jako „current player“
 * v rámci jednoho konkrétního HTTP requestu.
 *
 * -------------------------------------------------
 * ŽIVOTNÍ CYKLUS
 * -------------------------------------------------
 * <ul>
 *     <li>naplněn v {@code CurrentPlayerFilter} na začátku requestu,</li>
 *     <li>dostupný v celém call stacku
 *         (controller → service → helper),</li>
 *     <li>vyčištěn po dokončení requestu.</li>
 * </ul>
 *
 * -------------------------------------------------
 * PROČ THREADLOCAL
 * -------------------------------------------------
 * Spring obsluhuje HTTP requesty paralelně ve vláknech.
 * {@link ThreadLocal} zajišťuje, že:
 * <ul>
 *     <li>každý request má vlastní instanci {@code PlayerEntity},</li>
 *     <li>nedochází ke sdílení dat mezi uživateli,</li>
 *     <li>není nutné předávat hráče přes všechny metody.</li>
 * </ul>
 *
 * -------------------------------------------------
 * DŮLEŽITÉ
 * -------------------------------------------------
 * {@link ThreadLocal} MUSÍ být vždy vyčištěn pomocí {@link #clear()},
 * jinak hrozí:
 * <ul>
 *     <li>memory leak v aplikačním serveru,</li>
 *     <li>únik dat mezi jednotlivými requesty.</li>
 * </ul>
 */
public final class CurrentPlayerContext {

    /**
     * ThreadLocal uchovávající aktuálního hráče
     * pro právě zpracovávaný request.
     */
    private static final ThreadLocal<PlayerEntity> currentPlayer = new ThreadLocal<>();

    private CurrentPlayerContext() {
        // utility class – nelze instancovat
    }

    /**
     * Nastaví aktuálního hráče do thread-local kontextu.
     *
     * Volá se typicky:
     * <ul>
     *     <li>na začátku requestu ve {@code CurrentPlayerFilter}.</li>
     * </ul>
     *
     * @param player hráč zvolený jako aktuální pro daný request
     */
    public static void set(PlayerEntity player) {
        currentPlayer.set(player);
    }

    /**
     * Vrátí aktuálního hráče pro právě zpracovávaný request.
     *
     * @return {@link PlayerEntity} nebo {@code null},
     *         pokud hráč nebyl zvolen nebo request
     *         nevyžaduje kontext hráče
     */
    public static PlayerEntity get() {
        return currentPlayer.get();
    }

    /**
     * Vyčistí thread-local kontext.
     *
     * MUSÍ se volat:
     * <ul>
     *     <li>vždy po dokončení requestu (typicky ve {@code finally} bloku filtru).</li>
     * </ul>
     *
     * Použití {@link ThreadLocal#remove()}:
     * <ul>
     *     <li>uvolní referenci na {@link PlayerEntity},</li>
     *     <li>zabrání memory leakům při reuse vláken.</li>
     * </ul>
     */
    public static void clear() {
        currentPlayer.remove();
    }
}
