package cz.phsoft.hokej.models.dto;

import java.time.LocalDateTime;

public class PlayerInactivityPeriodDTO {

    private Long id;
    private Long playerId;
    private LocalDateTime inactiveFrom;
    private LocalDateTime inactiveTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public LocalDateTime getInactiveFrom() {
        return inactiveFrom;
    }

    public void setInactiveFrom(LocalDateTime inactiveFrom) {
        this.inactiveFrom = inactiveFrom;
    }

    public LocalDateTime getInactiveTo() {
        return inactiveTo;
    }

    public void setInactiveTo(LocalDateTime inactiveTo) {
        this.inactiveTo = inactiveTo;
    }
}
