package cz.phsoft.hokej.match.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

/**
 * Přehledové DTO reprezentující zápas v seznamu.
 *
 * Používá se v přehledech nadcházejících a minulých zápasů, v případech,
 * kdy stačí zjednodušený pohled na zápas doplněný o agregované informace
 * a stav aktuálního hráče. Typicky se používá na úvodních obrazovkách,
 * dashboardu nebo v kartách zápasů.
 */
public class MatchOverviewDTO implements NumberedMatchDTO {

    /**
     * Pořadové číslo zápasu v sezóně počítané podle data v rámci dané sezóny.
     * Hodnota se nastavuje pouze na serveru.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer matchNumber;

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    private String location;
    private String description;
    private Integer price;

    /**
     * Základní agregační údaje zápasu.
     *
     * Obsahují maximální počet hráčů a aktuální počet přihlášených
     * hráčů v zápase.
     */
    private int maxPlayers;
    private int inGamePlayers;

    /**
     * Cena přepočtená na jednoho přihlášeného hráče.
     * Hodnota se počítá na serveru.
     */
    private double pricePerRegisteredPlayer;

    /**
     * Stav přihlášeného hráče k danému zápasu.
     */
    private PlayerMatchStatus playerMatchStatus;

    /**
     * Režim zápasu (počet hráčů na ledě, s brankářem / bez brankáře).
     */
    @Enumerated(EnumType.STRING)
    private MatchMode matchMode;

    /**
     * Stav zápasu a případný důvod jeho zrušení.
     */
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    /**
     * ID sezóny, do které zápas patří.
     * Hodnota se nastavuje pouze na serveru.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long seasonId;

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

    public MatchMode getMatchMode() { return matchMode; }
    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }
    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    // NumberedMatchDTO

    @Override
    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    @Override
    public Integer getMatchNumber() {
        return matchNumber;
    }
}