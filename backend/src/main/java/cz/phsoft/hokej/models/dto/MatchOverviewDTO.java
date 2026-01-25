package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

/**
 * Přehledové DTO reprezentující zápas v seznamu.
 *
 * Používá se zejména:
 * <ul>
 *     <li>v přehledech nadcházejících a minulých zápasů,</li>
 *     <li>v seznamu zápasů hráče,</li>
 *     <li>na dashboardu nebo úvodních obrazovkách.</li>
 * </ul>
 *
 * Obsahuje zjednodušený pohled na zápas
 * doplněný o kontext přihlášeného hráče
 * a základní agregační informace.
 */
public class MatchOverviewDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    private String location;
    private String description;
    private Integer price;

    /**
     * Základní agregační údaje zápasu.
     */
    private int maxPlayers;
    private int inGamePlayers;

    /**
     * Cena přepočtená na jednoho přihlášeného hráče.
     * Hodnota je počítána serverem.
     */
    private double pricePerRegisteredPlayer;

    /**
     * Stav přihlášeného hráče k danému zápasu.
     */
    private PlayerMatchStatus playerMatchStatus;

    /**
     * Stav zápasu a případný důvod jeho zrušení.
     */
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

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

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public int getInGamePlayers() { return inGamePlayers; }
    public void setInGamePlayers(int inGamePlayers) { this.inGamePlayers = inGamePlayers; }

    public double getPricePerRegisteredPlayer() { return pricePerRegisteredPlayer; }
    public void setPricePerRegisteredPlayer(double pricePerRegisteredPlayer) {
        this.pricePerRegisteredPlayer = pricePerRegisteredPlayer;
    }

    public PlayerMatchStatus getPlayerMatchStatus() { return playerMatchStatus; }
    public void setPlayerMatchStatus(PlayerMatchStatus playerMatchStatus) {
        this.playerMatchStatus = playerMatchStatus;
    }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }
    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }
}
