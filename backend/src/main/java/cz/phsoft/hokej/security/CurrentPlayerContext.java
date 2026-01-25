package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;

/**
 * Thread-local kontext pro „aktuálního hráče“.
 *
 * ÚČEL:
 * -----
 * Uchovává PlayerEntity, který je zvolený jako "current player"
 * PRO JEDEN KONKRÉTNÍ HTTP REQUEST.
 *
 * Tento kontext:
 * - je naplněn v CurrentPlayerFilter
 * - je dostupný v celém call stacku (controller → service → helper)
 * - je vyčištěn po dokončení requestu
 *
 * Proč ThreadLocal:
 * -----------------
 * Spring obsluhuje requesty paralelně ve vláknech.
 * ThreadLocal zaručuje, že:
 * - každý request má svůj vlastní PlayerEntity
 * - nedojde ke sdílení dat mezi uživateli
 *
 * DŮLEŽITÉ:
 * ThreadLocal MUSÍ být vždy vyčištěn,
 * jinak hrozí memory leak nebo přenos dat mezi requesty.
 */
public class CurrentPlayerContext {

    /**
     * ThreadLocal držící aktuálního hráče pro právě běžící thread.
     */
    private static final ThreadLocal<PlayerEntity> currentPlayer = new ThreadLocal<>();

    /**
     * Nastaví aktuálního hráče do thread-local kontextu.
     *
     * Volá se:
     * - v CurrentPlayerFilter (na začátku requestu)
     */
    public static void set(PlayerEntity player) {
        currentPlayer.set(player);
    }

    /**
     * Vrátí aktuálního hráče pro daný request.
     *
     * @return PlayerEntity nebo null,
     *         pokud hráč nebyl zvolen nebo request nevyžaduje hráče
     */
    public static PlayerEntity get() {
        return currentPlayer.get();
    }

    /**
     * Vyčistí thread-local kontext.
     *
     * MUSÍ se volat:
     * - vždy po dokončení requestu (finally blok ve filtru)
     *
     * Použití remove() místo set(null):
     * - uvolní referenci
     * - zabrání memory leakům v aplikačním serveru
     */
    public static void clear() {
        currentPlayer.remove();
    }
}
