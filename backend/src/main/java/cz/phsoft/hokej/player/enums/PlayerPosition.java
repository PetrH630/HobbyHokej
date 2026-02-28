package cz.phsoft.hokej.player.enums;

import cz.phsoft.hokej.registration.enums.PlayerPositionCategory;

public enum PlayerPosition {

    // --- brankář ---
    GOALIE(PlayerPositionCategory.GOALIE),

    // --- obrana ---
    DEFENSE_LEFT(PlayerPositionCategory.DEFENSE),
    DEFENSE_RIGHT(PlayerPositionCategory.DEFENSE),
    DEFENSE(PlayerPositionCategory.DEFENSE),

    // --- útok ---
    CENTER(PlayerPositionCategory.FORWARD),
    WING_LEFT(PlayerPositionCategory.FORWARD),
    WING_RIGHT(PlayerPositionCategory.FORWARD),
    FORWARD(PlayerPositionCategory.FORWARD),

    // --- speciální hodnota ---
    ANY(null); // "nezáleží" – flexibilní, kategorie se neurčuje

    private final PlayerPositionCategory category;

    PlayerPosition(PlayerPositionCategory category) {
        this.category = category;
    }

    /**
     * Vrací kategorii pozice (obránce / útočník / brankář).
     * Pro ANY vrací null.
     */
    public PlayerPositionCategory getCategory() {
        return category;
    }

    public boolean isGoalie() {
        return category == PlayerPositionCategory.GOALIE;
    }

    public boolean isDefense() {
        return category == PlayerPositionCategory.DEFENSE;
    }

    public boolean isForward() {
        return category == PlayerPositionCategory.FORWARD;
    }
}