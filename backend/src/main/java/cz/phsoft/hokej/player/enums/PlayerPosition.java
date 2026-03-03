package cz.phsoft.hokej.player.enums;

/**
 * Výčtový typ reprezentující herní pozici hráče.
 *
 * Každá pozice je přiřazena ke kategorii reprezentované
 * výčtem PlayerPositionCategory. Kategorie se používá
 * například při automatickém skládání sestav, přesunech
 * hráčů mezi pozicemi nebo při vyhodnocování kompatibility
 * pozic.
 *
 * Speciální hodnota ANY představuje flexibilní pozici,
 * u které se kategorie neurčuje.
 */
public enum PlayerPosition {

    // brankář
    GOALIE(PlayerPositionCategory.GOALIE),

    // obrana
    DEFENSE_LEFT(PlayerPositionCategory.DEFENSE),
    DEFENSE_RIGHT(PlayerPositionCategory.DEFENSE),
    DEFENSE(PlayerPositionCategory.DEFENSE),

    // útok
    CENTER(PlayerPositionCategory.FORWARD),
    WING_LEFT(PlayerPositionCategory.FORWARD),
    WING_RIGHT(PlayerPositionCategory.FORWARD),
    FORWARD(PlayerPositionCategory.FORWARD),

    // speciální hodnota
    ANY(null);

    /**
     * Kategorie herní pozice.
     *
     * Pro hodnotu ANY je kategorie null.
     */
    private final PlayerPositionCategory category;

    /**
     * Vytváří hodnotu výčtu s přiřazenou kategorií.
     *
     * @param category kategorie herní pozice
     */
    PlayerPosition(PlayerPositionCategory category) {
        this.category = category;
    }

    /**
     * Vrací kategorii pozice.
     *
     * Kategorie se používá pro logiku rozlišení obránců,
     * útočníků a brankářů. Pro hodnotu ANY vrací null.
     *
     * @return kategorie pozice nebo null
     */
    public PlayerPositionCategory getCategory() {
        return category;
    }

    /**
     * Určuje, zda se jedná o brankářskou pozici.
     *
     * @return true, pokud je kategorie GOALIE
     */
    public boolean isGoalie() {
        return category == PlayerPositionCategory.GOALIE;
    }

    /**
     * Určuje, zda se jedná o obrannou pozici.
     *
     * @return true, pokud je kategorie DEFENSE
     */
    public boolean isDefense() {
        return category == PlayerPositionCategory.DEFENSE;
    }

    /**
     * Určuje, zda se jedná o útočnou pozici.
     *
     * @return true, pokud je kategorie FORWARD
     */
    public boolean isForward() {
        return category == PlayerPositionCategory.FORWARD;
    }
}