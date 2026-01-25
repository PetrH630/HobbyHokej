package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO reprezentující zápas.
 *
 * Slouží k přenosu dat o zápasech mezi backendem
 * a klientem (vytváření, editace, přehledy).
 *
 * Neobsahuje žádnou business logiku ani vazby na entity.
 */
public class MatchDTO {

    private Long id;

    /**
     * Datum a čas konání zápasu.
     */
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

    /**
     * Aktuální stav zápasu (např. aktivní, zrušený).
     */
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    /**
     * Důvod zrušení zápasu, pokud byl zrušen.
     */
    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    /**
     * ID sezóny, do které zápas patří.
     *
     * Pouze pro čtení – nastavuje server.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long seasonId;

    // gettery / settery

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

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }
    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }
}
