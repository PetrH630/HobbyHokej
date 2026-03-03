package cz.phsoft.hokej.player.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO reprezentující období neaktivity hráče.
 *
 * Slouží k přenosu informací o časovém intervalu, ve kterém se hráč
 * neúčastní zápasů, například z důvodu zranění nebo dlouhodobé absence.
 * DTO je využíváno mezi prezentační a servisní vrstvou a neobsahuje
 * žádnou business logiku.
 */
public class PlayerInactivityPeriodDTO {

    /**
     * Jedinečný identifikátor období neaktivity.
     */
    private Long id;

    /**
     * Identifikátor hráče, ke kterému se období neaktivity vztahuje.
     */
    private Long playerId;

    /**
     * Datum a čas začátku neaktivity.
     */
    @NotNull(message = "Datum začátku neaktivity je povinné.")
    private LocalDateTime inactiveFrom;

    /**
     * Datum a čas konce neaktivity.
     */
    @NotNull(message = "Datum konce neaktivity je povinné.")
    private LocalDateTime inactiveTo;

    /**
     * Textový důvod neaktivity hráče.
     */
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