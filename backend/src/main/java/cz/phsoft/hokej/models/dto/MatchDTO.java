package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class MatchDTO {

    private Long id;
    @NotNull(message = "Datum a čas zápasu je povinné.")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    @NotBlank(message = "Místo zápasu je povinné.")
    @Size(min = 3, max = 70)
    private String location;

    @Size(max = 255, message = "Popis může mít max 255 znaků.")
    private String description;

    @NotNull(message = "Maximální počet hráčů je povinný")
    private Integer maxPlayers;

    @NotNull(message = "Cena je povinná")
    private Integer price;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    // gettery a settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public MatchCancelReason getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(MatchCancelReason cancelReason) {
        this.cancelReason = cancelReason;
    }
}
