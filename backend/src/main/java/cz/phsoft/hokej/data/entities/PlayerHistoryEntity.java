package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam o hráči.
 *
 * Slouží pro auditní účely a uchovávání změn hráčů v čase.
 * Každý záznam představuje stav hráče v okamžiku provedení operace
 * nad hlavní entitou PlayerEntity.
 *
 * Záznamy jsou typicky vytvářeny databázovým triggerem při změně
 * údajů hráče.
 */
@Entity
@Table(name = "player_entity_history")
public class PlayerHistoryEntity {

    /**
     * Primární klíč historického záznamu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Typ provedené operace.
     *
     * Typicky se jedná o hodnoty CREATE, STATUS_CHANGE,
     * USER_CHANGE nebo DELETE.
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * Datum a čas provedení změny.
     *
     * Udává okamžik vytvoření historického záznamu.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * ID hráče z hlavní tabulky player_entity.
     *
     * Slouží pro propojení historického záznamu s původní entitou hráče.
     */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    /**
     * Původní časové razítko hráče.
     *
     * Jedná se o hodnotu sloupce timestamp z tabulky player_entity
     * v okamžiku vytvoření historického záznamu.
     */
    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    /**
     * Křestní jméno hráče v okamžiku změny.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Příjmení hráče v okamžiku změny.
     */
    @Column(name = "surname", nullable = false)
    private String surname;

    /**
     * Přezdívka hráče v okamžiku změny.
     */
    @Column(name = "nickname")
    private String nickname;

    /**
     * Typ hráče v okamžiku změny.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PlayerType type;

    /**
     * Celé jméno hráče v okamžiku změny.
     */
    @Column(name = "full_name")
    private String fullName;

    /**
     * Telefonní číslo hráče v okamžiku změny.
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Tým hráče v okamžiku změny.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    /**
     * Stav hráče v okamžiku změny.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "player_status", nullable = false)
    private PlayerStatus playerStatus;

    /**
     * ID uživatele, ke kterému byl hráč přiřazen
     * v okamžiku vytvoření historického záznamu.
     */
    @Column(name = "user_id")
    private Long userId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public void setPlayerStatus(PlayerStatus playerStatus) { this.playerStatus = playerStatus; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
