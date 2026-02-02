package cz.phsoft.hokej.data.enums;

/**
 * Důvody zrušení zápasu.
 *
 * Hodnota se používá spolu se stavem zápasu pro přehledné
 * a strojově zpracovatelné určení, proč byl zápas zrušen.
 */
public enum MatchCancelReason {

    /**
     * Nedostatečný počet přihlášených hráčů.
     */
    NOT_ENOUGH_PLAYERS,     // málo hráčů

    /**
     * Technické problémy, například led, hala nebo doprava.
     */
    TECHNICAL_ISSUE,        // technické problémy (led, hala, bus…)

    /**
     * Nepříznivé počasí.
     */
    WEATHER,                // počasí

    /**
     * Rozhodnutí organizátora.
     */
    ORGANIZER_DECISION,     // rozhodnutí organizátora

    /**
     * Jiný důvod zrušení, blíže nespecifikovaný.
     */
    OTHER                   // jiný důvod
}
