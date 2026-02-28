package cz.phsoft.hokej.match.util;

import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.player.enums.PlayerPosition;

import java.util.*;

/**
 * Pomocná utilita pro práci s rozložením pozic na ledě
 * a kapacitou pozic pro daný MatchMode.
 */
public final class MatchModeLayoutUtil {

    private MatchModeLayoutUtil() {
        // utility – žádná instance
    }

    /**
     * Vrací seznam "ice" pozic pro daný MatchMode v pořadí,
     * v jakém se používají pro rozdělení kapacity.
     *
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
     * Backend obdoba buildPositionCapacityForMode(icePositions, slotsPerTeam).
     *
     * - GOALIE dostane 1 místo, pokud je v icePositions a kapacita > 0,
     * - zbytek kapacity se cyklicky rozděluje mezi ostatní pozice
     *   v pořadí dle icePositions bez GOALIE.
     *
     * Používá se pro výpočet teoretické kapacity pozic pro jeden tým.
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

        // 1) Brankář – pokud systém obsahuje GOALIE, dáme mu 1 slot
        if (hasGoalie && remainingSlots > 0) {
            capacity.put(PlayerPosition.GOALIE, 1);
            remainingSlots -= 1;
        }

        // 2) Bruslaři – všechny ostatní pozice v pořadí, jak vrací getIcePositionsForMode
        List<PlayerPosition> skaterOrder = icePositions.stream()
                .filter(pos -> pos != PlayerPosition.GOALIE)
                .toList();

        if (skaterOrder.isEmpty() || remainingSlots <= 0) {
            return capacity;
        }

        // 3) Dokud máme sloty, točíme skaterOrder dokola
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