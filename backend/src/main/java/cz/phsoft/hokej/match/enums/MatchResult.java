package cz.phsoft.hokej.match.enums;

/**
 * Výsledek zápasu.
 *
 * Reprezentuje konečný stav zápasu na základě skóre.
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
     * Skóre zatím není zadáno.
     */
    NOT_PLAYED
}