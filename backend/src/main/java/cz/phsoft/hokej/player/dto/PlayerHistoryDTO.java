package cz.phsoft.hokej.player.dto;

import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.PlayerStatus;
import cz.phsoft.hokej.player.enums.PlayerType;
import cz.phsoft.hokej.player.enums.Team;

import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam změny hráče.
 *
 * Slouží k přenosu dat o změnách hráče z databázové vrstvy
 * do prezentační vrstvy. Obsahuje kompletní snapshot stavu
 * hráče v okamžiku provedené změny včetně informace o typu akce,
 * času změny a původním časovém razítku vytvoření záznamu.
 *
 * DTO odpovídá záznamu uloženému v tabulce historie hráče,
 * která je typicky naplňována databázovým triggerem.
 * Neobsahuje žádnou business logiku a slouží výhradně
 * jako datový přenosový objekt mezi servisní a prezentační vrstvou.
 */
public class PlayerHistoryDTO {

    /**
     * Jedinečný identifikátor historického záznamu.
     */
    private Long id;

    /**
     * Identifikátor uživatele, který změnu provedl.
     */
    private Long userId;

    /**
     * Identifikátor hráče, ke kterému se historický záznam vztahuje.
     */
    private Long playerId;

    /**
     * Typ provedené akce, například CREATE, UPDATE nebo DELETE.
     */
    private String action;

    /**
     * Datum a čas provedení změny.
     */
    private LocalDateTime changedAt;

    /**
     * Křestní jméno hráče v okamžiku změny.
     */
    private String name;

    /**
     * Příjmení hráče v okamžiku změny.
     */
    private String surname;

    /**
     * Přezdívka hráče v okamžiku změny.
     */
    private String nickname;

    /**
     * Celé jméno hráče v okamžiku změny.
     */
    private String fullName;

    /**
     * Telefonní číslo hráče v okamžiku změny.
     */
    private String phoneNumber;

    /**
     * Typ hráče v okamžiku změny.
     */
    private PlayerType type;

    /**
     * Tým hráče v okamžiku změny.
     */
    private Team team;

    /**
     * Primární pozice hráče v okamžiku změny.
     */
    private PlayerPosition primaryPosition;

    /**
     * Sekundární pozice hráče v okamžiku změny.
     */
    private PlayerPosition secondaryPosition;

    /**
     * Stav hráče v systému v okamžiku změny.
     */
    private PlayerStatus playerStatus;

    /**
     * Původní časové razítko hráče z hlavní tabulky.
     */
    private LocalDateTime originalTimestamp;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public PlayerType getType() { return type; }
    public void setType(PlayerType type) { this.type = type; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public PlayerPosition getPrimaryPosition() { return primaryPosition; }
    public void setPrimaryPosition(PlayerPosition primaryPosition) { this.primaryPosition = primaryPosition; }

    public PlayerPosition getSecondaryPosition() { return secondaryPosition; }
    public void setSecondaryPosition(PlayerPosition secondaryPosition) { this.secondaryPosition = secondaryPosition; }

    public PlayerStatus getPlayerStatus() { return playerStatus; }
    public void setPlayerStatus(PlayerStatus playerStatus) { this.playerStatus = playerStatus; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }
}