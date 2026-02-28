package cz.phsoft.hokej.player.enums;

/**
 * Týmové rozdělení hráčů pro zápas.
 *
 * Enum se používá pro přiřazení hráče do jednoho ze dvou týmů.
 */
public enum Team {
    /**
     * Tmavý tým.
     */
    DARK,
    /**
     * Světlý tým.
     */
    LIGHT;

    public Team opposite() {
        return this == LIGHT ? DARK : LIGHT;
    }
}
