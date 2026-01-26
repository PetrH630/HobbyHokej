package cz.phsoft.hokej.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailní DTO reprezentující zápas včetně
 * kontextu přihlášeného hráče a agregovaných statistik.
 *
 * Používá se zejména:
 * <ul>
 *     <li>na stránce detailu zápasu,</li>
 *     <li>pro zobrazení seznamů hráčů dle stavu registrace,</li>
 *     <li>pro zobrazení kapacity, ceny a stavu účasti.</li>
 * </ul>
 *
 * DTO obsahuje jak:
 * <ul>
 *     <li>základní informace o zápasu,</li>
 *     <li>agregované hodnoty (počty hráčů, ceny),</li>
 *     <li>stav přihlášeného hráče k danému zápasu.</li>
 * </ul>
 */
public class MatchDetailDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateTime;

    private String location;
    private String description;
    private Integer price;

    /**
     * Kapacitní a agregační údaje zápasu.
     */
    private int maxPlayers;
    private int inGamePlayers;
    private int inGamePlayersDark;
    private int inGamePlayersLight;
    private int outGamePlayers;
    private int waitingPlayers;
    private int noActionPlayers;
    private int noExcusedPlayersSum;
    private int remainingSlots;

    /**
     * Cena přepočtená na jednoho přihlášeného hráče.
     * Hodnota je počítána serverem.
     */
    private double pricePerRegisteredPlayer;

    /**
     * Stav přihlášeného hráče k tomuto zápasu.
     */
    private PlayerMatchStatus playerMatchStatus;

    /**
     * Informace o omluvě přihlášeného hráče (pokud existuje).
     */
    private ExcuseReason excuseReason;
    private String excuseNote;

    /**
     * Stav zápasu a případný důvod zrušení.
     */
    private MatchStatus matchStatus;
    private MatchCancelReason cancelReason;

    /**
     * ID sezóny – pouze pro čtení.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long seasonId;

    /**
     * Seznamy hráčů rozdělené podle stavu registrace.
     */
    private List<PlayerDTO> registeredPlayers;
    private List<PlayerDTO> reservedPlayers;
    private List<PlayerDTO> unregisteredPlayers;
    private List<PlayerDTO> excusedPlayers;
    private List<PlayerDTO> noExcusedPlayers;
    private List<PlayerDTO> noResponsePlayers;

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

    public int getInGamePlayersDark() { return inGamePlayersDark; }

    public void setInGamePlayersDark(int inGamePlayersDark) { this.inGamePlayersDark = inGamePlayersDark; }

    public int getInGamePlayersLight() { return inGamePlayersLight; }

    public void setInGamePlayersLight(int inGamePlayersLight) { this.inGamePlayersLight = inGamePlayersLight; }

    public int getOutGamePlayers() { return outGamePlayers; }
    public void setOutGamePlayers(int outGamePlayers) { this.outGamePlayers = outGamePlayers; }

    public int getWaitingPlayers() { return waitingPlayers; }
    public void setWaitingPlayers(int waitingPlayers) { this.waitingPlayers = waitingPlayers; }

    public int getNoActionPlayers() { return noActionPlayers; }
    public void setNoActionPlayers(int noActionPlayers) { this.noActionPlayers = noActionPlayers; }

    public int getNoExcusedPlayersSum() { return noExcusedPlayersSum; }
    public void setNoExcusedPlayersSum(int noExcusedPlayersSum) { this.noExcusedPlayersSum = noExcusedPlayersSum; }

    public int getRemainingSlots() { return remainingSlots; }
    public void setRemainingSlots(int remainingSlots) { this.remainingSlots = remainingSlots; }

    public double getPricePerRegisteredPlayer() { return pricePerRegisteredPlayer; }
    public void setPricePerRegisteredPlayer(double pricePerRegisteredPlayer) {
        this.pricePerRegisteredPlayer = pricePerRegisteredPlayer;
    }

    public PlayerMatchStatus getPlayerMatchStatus() { return playerMatchStatus; }
    public void setPlayerMatchStatus(PlayerMatchStatus playerMatchStatus) {
        this.playerMatchStatus = playerMatchStatus;
    }

    public ExcuseReason getExcuseReason() { return excuseReason; }
    public void setExcuseReason(ExcuseReason excuseReason) { this.excuseReason = excuseReason; }

    public String getExcuseNote() { return excuseNote; }
    public void setExcuseNote(String excuseNote) { this.excuseNote = excuseNote; }

    public MatchStatus getMatchStatus() { return matchStatus; }
    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }
    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    public List<PlayerDTO> getRegisteredPlayers() { return registeredPlayers; }
    public void setRegisteredPlayers(List<PlayerDTO> registeredPlayers) {
        this.registeredPlayers = registeredPlayers;
    }

    public List<PlayerDTO> getReservedPlayers() { return reservedPlayers; }
    public void setReservedPlayers(List<PlayerDTO> reservedPlayers) {
        this.reservedPlayers = reservedPlayers;
    }

    public List<PlayerDTO> getUnregisteredPlayers() { return unregisteredPlayers; }
    public void setUnregisteredPlayers(List<PlayerDTO> unregisteredPlayers) {
        this.unregisteredPlayers = unregisteredPlayers;
    }

    public List<PlayerDTO> getExcusedPlayers() { return excusedPlayers; }
    public void setExcusedPlayers(List<PlayerDTO> excusedPlayers) {
        this.excusedPlayers = excusedPlayers;
    }

    public List<PlayerDTO> getNoExcusedPlayers() { return noExcusedPlayers; }
    public void setNoExcusedPlayers(List<PlayerDTO> noExcusedPlayers) {
        this.noExcusedPlayers = noExcusedPlayers;
    }

    public List<PlayerDTO> getNoResponsePlayers() { return noResponsePlayers; }
    public void setNoResponsePlayers(List<PlayerDTO> noResponsePlayers) {
        this.noResponsePlayers = noResponsePlayers;
    }
}
