package cz.phsoft.hokej.registration.util;

import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.registration.enums.PlayerPositionCategory;

/**
 * Pomocná utilita pro práci s herními pozicemi hráče.
 *
 * Centralizuje mapování enumu PlayerPosition do kategorií
 * a poskytuje metody pro zjištění typu pozice
 * (brankář, obránce, útočník).
 *
 * Tato utilita se používá v business logice zápasů a registrací
 * pro rozhodování o automatických přesunech hráčů mezi pozicemi
 * a řadami.
 */
public final class PlayerPositionUtil {

    private PlayerPositionUtil() {
        // utility class – nevytváří se instance
    }

    /**
     * Určuje kategorii herní pozice hráče.
     *
     * Používá se zejména při rozhodování, zda je změna pozice
     * v rámci stejné kategorie (obrana / útok / brankář),
     * nebo zda se jedná o přechod mezi kategoriemi.
     *
     * @param position Herní pozice hráče.
     * @return Kategorie pozice nebo null, pokud není kategorie definována
     *         (např. ANY nebo null).
     */
    public static PlayerPositionCategory getCategory(PlayerPosition position) {
        if (position == null || position == PlayerPosition.ANY) {
            return null;
        }

        return switch (position) {
            case GOALIE -> PlayerPositionCategory.GOALIE;

            case DEFENSE,
                 DEFENSE_LEFT,
                 DEFENSE_RIGHT -> PlayerPositionCategory.DEFENSE;

            case CENTER,
                 WING_LEFT,
                 WING_RIGHT,
                 FORWARD -> PlayerPositionCategory.FORWARD;

            case ANY -> null;
        };
    }

    /**
     * Ověřuje, zda pozice patří do kategorie brankář.
     *
     * @param position Herní pozice.
     * @return true, pokud jde o brankáře, jinak false.
     */
    public static boolean isGoalie(PlayerPosition position) {
        return getCategory(position) == PlayerPositionCategory.GOALIE;
    }

    /**
     * Ověřuje, zda pozice patří do kategorie obránce.
     *
     * @param position Herní pozice.
     * @return true, pokud jde o obránce, jinak false.
     */
    public static boolean isDefense(PlayerPosition position) {
        return getCategory(position) == PlayerPositionCategory.DEFENSE;
    }

    /**
     * Ověřuje, zda pozice patří do kategorie útočník.
     *
     * @param position Herní pozice.
     * @return true, pokud jde o útočníka, jinak false.
     */
    public static boolean isForward(PlayerPosition position) {
        return getCategory(position) == PlayerPositionCategory.FORWARD;
    }

    /**
     * Ověřuje, zda dvě pozice patří do stejné kategorie
     * (například obě obránci nebo oba útočníci).
     *
     * Pozice bez kategorie (ANY, null) se považují
     * za nekompatibilní.
     *
     * @param a První pozice.
     * @param b Druhá pozice.
     * @return true, pokud obě pozice spadají do stejné kategorie, jinak false.
     */
    public static boolean isSameCategory(PlayerPosition a, PlayerPosition b) {
        PlayerPositionCategory ca = getCategory(a);
        PlayerPositionCategory cb = getCategory(b);
        if (ca == null || cb == null) {
            return false;
        }
        return ca == cb;
    }
}