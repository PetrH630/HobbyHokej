package cz.phsoft.hokej.notifications.dto;

/**
 * DTO reprezentující jednoho možného příjemce speciální notifikace.
 *
 * Používá se v administrátorském uživatelském rozhraní
 * pro výběr cílových uživatelů nebo hráčů.
 *
 * Třída slouží pouze jako datový přenosový objekt.
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
    /**
     * Vytváří prázdný přenosový objekt.
     */
    public SpecialNotificationTargetDTO() {
    }
    /**
     * Vytváří přenosový objekt reprezentující cíl notifikace.
     *
     * @param userId identifikátor uživatele
     * @param playerId identifikátor hráče
     * @param displayName zobrazované jméno v uživatelském rozhraní
     * @param type typ cíle
     */
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