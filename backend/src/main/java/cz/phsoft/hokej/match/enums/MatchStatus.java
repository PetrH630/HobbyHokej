package cz.phsoft.hokej.match.enums;

/**
 * Výčtový typ reprezentující stav zápasu
 * z pohledu plánování a administrativních změn.
 *
 * Hodnota určuje, zda byl zápas zrušen,
 * znovu aktivován nebo aktualizován.
 */
public enum MatchStatus {

    /**
     * Zápas byl znovu aktivován po předchozím zrušení.
     */
    UNCANCELED,

    /**
     * Zápas byl zrušen.
     */
    CANCELED,

    /**
     * Došlo ke změně termínu nebo jiných parametrů zápasu.
     */
    UPDATED
}