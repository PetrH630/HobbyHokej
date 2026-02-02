package cz.phsoft.hokej.data.enums;

/**
 * Důvody omluvy hráče z účasti na zápase.
 *
 * Hodnota se používá v registraci hráče pro evidenci
 * strukturovaného důvodu neúčasti.
 */
public enum ExcuseReason {

    /**
     * Hráč je nemocný.
     */
    NEMOC,

    /**
     * Pracovní důvody bránící účasti.
     */
    PRACE,

    /**
     * Nechce se mi.
     *
     * Úmyslně ponechaný „lidský“ důvod.
     */
    NECHE_SE_MI,

    /**
     * Jiný neuvedený důvod.
     */
    JINE
}
