package cz.phsoft.hokej.models.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam o sezóně.
 */
public class SeasonHistoryDTO {

    private Long id;
    private String action;
    private LocalDateTime changedAt;
    private Long seasonId;
    private LocalDateTime originalTimestamp;

    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    /**
     * ID uživatele, který sezónu vytvořil.
     */
    private Long createdByUserId;

    // gettery / settery

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
}
