package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam o hráči.
 *
 * Slouží pro auditní účely a sledování změn hráčů v čase,
 * zejména při vytvoření hráče, změně stavu a změně přiřazeného uživatele.
 */
@Entity
@Table(name = "player_entity_history")
public class PlayerHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Typ provedené operace.
     * Typicky hodnoty CREATE, STATUS_CHANGE, USER_CHANGE, DELETE.
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * Datum a čas provedení změny.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * ID hráče v hlavní tabulce player_entity.
     */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    /**
     * Původní časové razítko hráče.
     * Jedná se o hodnotu sloupce timestamp z tabulky player_entity
     * v okamžiku, kdy byl záznam přesunut do historie.
     */
    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "nickname")
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PlayerType type;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "player_status", nullable = false)
    private PlayerStatus playerStatus;

    @Column(name = "user_id")
    private Long userId;

    // gettery / settery ...

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
