package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Team;

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

    private Long id;
    private Long userId;
    private Long playerId;
    private String action;
    private LocalDateTime changedAt;
    private String name;
    private String surname;
    private String nickname;
    private String fullName;
    private String phoneNumber;
    private PlayerType type;
    private Team team;
    private PlayerStatus playerStatus;
    private LocalDateTime originalTimestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PlayerType getType() {
        return type;
    }

    public void setType(PlayerType type) {
        this.type = type;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public LocalDateTime getOriginalTimestamp() {
        return originalTimestamp;
    }

    public void setOriginalTimestamp(LocalDateTime originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }
}
