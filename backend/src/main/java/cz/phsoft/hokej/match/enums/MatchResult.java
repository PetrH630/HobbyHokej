package cz.phsoft.hokej.match.enums;

/**
 * Výčtový typ reprezentující výsledek zápasu.
 *
 * Hodnota je odvozena ze skóre a používá se
 * pro jednoznačné určení výsledku z pohledu aplikace.
 */
public enum MatchResult {

    /**
     * Vyhrál tým LIGHT.
     */
    LIGHT_WIN,

    /**
     * Vyhrál tým DARK.
     */
    DARK_WIN,

    /**
     * Zápas skončil remízou.
     */
    DRAW,

    /**
     * Skóre zatím nebylo zadáno.
     */
    NOT_PLAYED
}