package cz.phsoft.hokej.match.entities;

import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.enums.MatchStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam změny zápasu.
 *
 * Každý záznam představuje snapshot stavu zápasu
 * v okamžiku provedení operace nad entitou MatchEntity.
 * Záznamy jsou typicky vytvářeny databázovým triggerem.
 */
@Entity
@Table(name = "matches_history")
public class MatchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "description")
    private String description;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_status")
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancel_reason")
    private MatchCancelReason cancelReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false)
    private MatchMode matchMode;

    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "last_modified_by_user_id")
    private Long lastModifiedByUserId;

    /**
     * Skóre zápasu v okamžiku změny.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "light", column = @Column(name = "score_light")),
            @AttributeOverride(name = "dark", column = @Column(name = "score_dark"))
    })
    private MatchScore score;

    public MatchHistoryEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }
    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public MatchMode getMatchMode() { return matchMode; }
    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Long getLastModifiedByUserId() { return lastModifiedByUserId; }
    public void setLastModifiedByUserId(Long lastModifiedByUserId) { this.lastModifiedByUserId = lastModifiedByUserId; }

    public MatchScore getScore() { return score; }
    public void setScore(MatchScore score) { this.score = score; }
}