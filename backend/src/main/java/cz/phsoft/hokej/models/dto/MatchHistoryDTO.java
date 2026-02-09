package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;

import java.time.LocalDateTime;

public class MatchHistoryDTO {

    private Long id;
    private String action;
    private LocalDateTime changedAt;
    private Long matchId;
    private LocalDateTime originalTimestamp;
    private LocalDateTime dateTime;
    private String location;
    private String description;
    private Integer maxPlayers;
    private Integer price;
    private MatchStatus matchStatus;
    private MatchCancelReason cancelReason;
    private Long seasonId;
    private Long createdByUserId;
    private Long lastModifiedByUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public LocalDateTime getOriginalTimestamp() {
        return originalTimestamp;
    }

    public void setOriginalTimestamp(LocalDateTime originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

    public MatchCancelReason getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(MatchCancelReason cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Long getLastModifiedByUserId() { return lastModifiedByUserId; }
    public void setLastModifiedByUserId(Long lastModifiedByUserId) { this.lastModifiedByUserId = lastModifiedByUserId; }
}
