package cz.phsoft.hokej.models.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO reprezentující období neaktivity hráče.
 *
 * Slouží k přenosu informací o časovém intervalu, ve kterém se hráč
 * neúčastní zápasů, například z důvodu zranění nebo dlouhodobé absence.
 */
public class PlayerInactivityPeriodDTO {

    private Long id;
    private Long playerId;

    @NotNull(message = "Datum začátku neaktivity je povinné.")
    private LocalDateTime inactiveFrom;

    @NotNull(message = "Datum konce neaktivity je povinné.")
    private LocalDateTime inactiveTo;

    @NotNull(message = "Duvod neaktivity je povinný.")
    private String inactivityReason;



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public LocalDateTime getInactiveFrom() { return inactiveFrom; }
    public void setInactiveFrom(LocalDateTime inactiveFrom) { this.inactiveFrom = inactiveFrom; }

    public LocalDateTime getInactiveTo() { return inactiveTo; }
    public void setInactiveTo(LocalDateTime inactiveTo) { this.inactiveTo = inactiveTo; }

    public String getInactivityReason() {
        return inactivityReason;
    }

    public void setInactivityReason(String inactivityReason) {
        this.inactivityReason = inactivityReason;
    }
}
