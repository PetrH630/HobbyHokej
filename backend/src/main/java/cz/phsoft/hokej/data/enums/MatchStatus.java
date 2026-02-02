package cz.phsoft.hokej.data.enums;

/**
 * Stav zápasu z pohledu plánování a změn.
 *
 * Enum se používá k označení, zda je zápas zrušený,
 * znovu aktivovaný nebo zda došlo ke změně jeho termínu.
 */
public enum MatchStatus {

    /**
     * Zápas byl znovu aktivován po předchozím zrušení.
     */
    UNCANCELED,     // znovu aktivovaný

    /**
     * Zápas byl zrušen.
     */
    CANCELLED,      // zrušený

    /**
     * Došlo ke změně data nebo času zápasu.
     */
    UPDATED,        // změna data-času
}
