package cz.phsoft.hokej.match.enums;

/**
 * Výčtový typ reprezentující důvody zrušení zápasu.
 *
 * Používá se ve vazbě na MatchStatus pro jednoznačné
 * a strojově zpracovatelné určení příčiny zrušení zápasu.
 * Hodnota je ukládána do databáze jako textový enum.
 */
public enum MatchCancelReason {

    /**
     * Nedostatečný počet přihlášených hráčů.
     */
    NOT_ENOUGH_PLAYERS,

    /**
     * Technické problémy, například led, hala nebo doprava.
     */
    TECHNICAL_ISSUE,

    /**
     * Nepříznivé počasí.
     */
    WEATHER,

    /**
     * Rozhodnutí organizátora.
     */
    ORGANIZER_DECISION,

    /**
     * Jiný, blíže nespecifikovaný důvod.
     */
    OTHER
}