package cz.phsoft.hokej.match.util;

import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.player.enums.PlayerPosition;

import java.util.*;

/**
 * Utilitní třída pro práci s rozložením herních pozic
 * a výpočtem kapacity pozic podle zvoleného MatchMode.
 *
 * Třída obsahuje čistě statické metody a neudržuje žádný stav.
 * Slouží jako doménová pomůcka pro servisní vrstvu při výpočtu
 * teoretického rozložení hráčů na jeden tým.
 */
public final class MatchModeLayoutUtil {

    /**
     * Soukromý konstruktor zabraňující vytvoření instance.
     */
    private MatchModeLayoutUtil() {
        // utility třída, není určena k instanciaci
    }

    /**
     * Vrací seznam herních pozic na ledě pro daný MatchMode.
     *
     * Pořadí pozic odpovídá pořadí, ve kterém se následně
     * rozděluje kapacita hráčů. Pokud je předán null,
     * vrací se výchozí rozložení pozic.
     *
     * @param mode herní režim zápasu
     * @return seznam pozic v pořadí rozdělování kapacity
     */
    public static List<PlayerPosition> getIcePositionsForMode(MatchMode mode) {
        if (mode == null) {
            return defaultPositions();
        }

        return switch (mode) {
            case THREE_ON_THREE_NO_GOALIE -> List.of(
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE
            );
            case THREE_ON_THREE_WITH_GOALIE -> List.of(
                    PlayerPosition.GOALIE,
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE
            );
            case FOUR_ON_FOUR_NO_GOALIE -> List.of(
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE_LEFT,
                    PlayerPosition.DEFENSE_RIGHT
            );
            case FOUR_ON_FOUR_WITH_GOALIE -> List.of(
                    PlayerPosition.GOALIE,
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE_LEFT,
                    PlayerPosition.DEFENSE_RIGHT
            );
            case FIVE_ON_FIVE_NO_GOALIE -> List.of(
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.CENTER,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE_LEFT,
                    PlayerPosition.DEFENSE_RIGHT
            );
            case FIVE_ON_FIVE_WITH_GOALIE -> List.of(
                    PlayerPosition.GOALIE,
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.CENTER,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE_LEFT,
                    PlayerPosition.DEFENSE_RIGHT
            );
            case SIX_ON_SIX_NO_GOALIE -> List.of(
                    PlayerPosition.WING_LEFT,
                    PlayerPosition.CENTER,
                    PlayerPosition.WING_RIGHT,
                    PlayerPosition.DEFENSE,
                    PlayerPosition.DEFENSE_LEFT,
                    PlayerPosition.DEFENSE_RIGHT
            );
        };
    }

    /**
     * Vrací výchozí rozložení pozic používané při absenci MatchMode.
     *
     * @return seznam standardních pozic
     */
    private static List<PlayerPosition> defaultPositions() {
        return List.of(
                PlayerPosition.GOALIE,
                PlayerPosition.DEFENSE_LEFT,
                PlayerPosition.DEFENSE_RIGHT,
                PlayerPosition.WING_LEFT,
                PlayerPosition.CENTER,
                PlayerPosition.WING_RIGHT
        );
    }

    /**
     * Vypočítá teoretickou kapacitu pozic pro jeden tým
     * podle zvoleného MatchMode a počtu slotů.
     *
     * Logika výpočtu:
     * - Pokud je mezi pozicemi GOALIE a je k dispozici alespoň jeden slot,
     *   brankář získá jeden slot.
     * - Zbývající sloty jsou cyklicky rozdělovány mezi ostatní pozice
     *   v pořadí definovaném metodou getIcePositionsForMode.
     *
     * Výsledkem je mapa pozice → počet míst pro danou pozici.
     *
     * @param mode herní režim zápasu
     * @param slotsPerTeam maximální počet hráčů na jeden tým
     * @return mapa kapacit pozic
     */
    public static Map<PlayerPosition, Integer> buildPositionCapacityForMode(
            MatchMode mode,
            int slotsPerTeam
    ) {

        List<PlayerPosition> icePositions = getIcePositionsForMode(mode);
        Map<PlayerPosition, Integer> capacity = new EnumMap<>(PlayerPosition.class);

        if (icePositions.isEmpty() || slotsPerTeam <= 0) {
            return capacity;
        }

        int totalSlots = Math.max(0, slotsPerTeam);
        if (totalSlots == 0) {
            return capacity;
        }

        boolean hasGoalie = icePositions.contains(PlayerPosition.GOALIE);
        int remainingSlots = totalSlots;

        // Brankář získá jeden slot, pokud je součástí režimu
        if (hasGoalie && remainingSlots > 0) {
            capacity.put(PlayerPosition.GOALIE, 1);
            remainingSlots -= 1;
        }

        // Ostatní pozice reprezentují hráče v poli
        List<PlayerPosition> skaterOrder = icePositions.stream()
                .filter(pos -> pos != PlayerPosition.GOALIE)
                .toList();

        if (skaterOrder.isEmpty() || remainingSlots <= 0) {
            return capacity;
        }

        // Sloty se rozdělují cyklicky mezi pozice hráčů v poli
        int idx = 0;
        while (remainingSlots > 0) {
            PlayerPosition pos = skaterOrder.get(idx % skaterOrder.size());
            capacity.merge(pos, 1, Integer::sum);
            remainingSlots--;
            idx++;
        }

        return capacity;
    }
}