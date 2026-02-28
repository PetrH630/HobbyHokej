package cz.phsoft.hokej.registration.enums;

/**
 * Kategorie hráčských pozic.
 *
 * Slouží pro logiku:
 * - automatických přesunů mezi pozicemi při změně matchMode,
 * - vyhodnocování, zda jde o obránce / útočníka / brankáře,
 *   bez nutnosti všude ručně vypisovat konkrétní enum hodnoty.
 */
public enum PlayerPositionCategory {
    GOALIE,
    DEFENSE,
    FORWARD
}