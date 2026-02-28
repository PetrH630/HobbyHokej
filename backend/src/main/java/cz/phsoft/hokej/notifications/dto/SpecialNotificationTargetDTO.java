package cz.phsoft.hokej.notifications.dto;

/**
 * DTO reprezentující jednoho možného příjemce speciální zprávy.
 *
 * Používá se v admin UI pro výběr cílových uživatelů a hráčů.
 */
public class SpecialNotificationTargetDTO {

    /**
     * ID uživatele, ke kterému cíl patří.
     */
    private Long userId;

    /**
     * ID hráče, pokud se jedná o hráčský cíl.
     * Pokud je null, jde o čistého uživatele bez hráče.
     */
    private Long playerId;

    /**
     * Zobrazované jméno v UI (např. "Jan NOVÁK (hráč)").
     */
    private String displayName;

    /**
     * Typ cíle ("PLAYER" nebo "USER").
     */
    private String type;

    public SpecialNotificationTargetDTO() {
    }

    public SpecialNotificationTargetDTO(Long userId, Long playerId, String displayName, String type) {
        this.userId = userId;
        this.playerId = playerId;
        this.displayName = displayName;
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}