package cz.phsoft.hokej.registration.util;

import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.PlayerPositionCategory;

/**
 * Pomocná utilita pro práci s herními pozicemi hráče.
 *
 * Centralizuje mapování enumu PlayerPosition do vyšších kategorií
 * reprezentovaných enumem PlayerPositionCategory a poskytuje
 * pomocné metody pro vyhodnocení typu pozice.
 *
 * Třída je bezstavová a obsahuje pouze statické metody.
 * Používá se v business logice zápasů a registrací
 * pro rozhodování o přesunech hráčů mezi pozicemi
 * a pro kontrolu kompatibility pozic.
 */
public final class PlayerPositionUtil {

    /**
     * Soukromý konstruktor zabraňující vytvoření instance.
     * Třída je určena pouze pro statické použití.
     */
    private PlayerPositionUtil() {
        // utility class – nevytváří se instance
    }

    /**
     * Určuje kategorii herní pozice hráče.
     *
     * Metoda převádí konkrétní herní pozici na obecnější kategorii,
     * která se používá například při rozhodování, zda je změna pozice
     * v rámci stejné skupiny rolí nebo mezi různými kategoriemi.
     *
     * Pokud je předána hodnota null nebo pozice ANY,
     * není kategorie definována a vrací se null.
     *
     * @param position herní pozice hráče
     * @return kategorie pozice nebo null, pokud není určena
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
     * @param position herní pozice
     * @return true, pokud pozice spadá do kategorie brankář
     */
    public static boolean isGoalie(PlayerPosition position) {
        return getCategory(position) == PlayerPositionCategory.GOALIE;
    }

    /**
     * Ověřuje, zda pozice patří do kategorie obránce.
     *
     * @param position herní pozice
     * @return true, pokud pozice spadá do kategorie obránce
     */
    public static boolean isDefense(PlayerPosition position) {
        return getCategory(position) == PlayerPositionCategory.DEFENSE;
    }

    /**
     * Ověřuje, zda pozice patří do kategorie útočník.
     *
     * @param position herní pozice
     * @return true, pokud pozice spadá do kategorie útočník
     */
    public static boolean isForward(PlayerPosition position) {
        return getCategory(position) == PlayerPositionCategory.FORWARD;
    }

    /**
     * Ověřuje, zda dvě pozice patří do stejné kategorie.
     *
     * Pozice bez definované kategorie, například ANY nebo null,
     * se považují za nekompatibilní a metoda vrací false.
     *
     * @param a první pozice
     * @param b druhá pozice
     * @return true, pokud obě pozice spadají do stejné kategorie
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