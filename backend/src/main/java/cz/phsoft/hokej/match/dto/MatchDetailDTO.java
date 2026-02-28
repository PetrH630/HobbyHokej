package cz.phsoft.hokej.match.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailní DTO reprezentující jeden zápas včetně kontextu hráče
 * a agregovaných statistik.
 *
 * Používá se zejména na stránce detailu zápasu, pro zobrazení
 * seznamů hráčů podle stavu registrace, pro zobrazení kapacity,
 * ceny a aktuální účasti. Kromě základních informací o zápasu
 * obsahuje i stav přihlášeného hráče a rozdělené seznamy hráčů
 * podle jednotlivých kategorií účasti.
 */
public class MatchDetailDTO implements NumberedMatchDTO {

    /**
     * Pořadové číslo zápasu v sezóně počítané podle data v rámci dané sezóny.
     * Hodnota se nastavuje pouze na serveru a na klienta se vrací jako
     * read-only údaj.
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
     * Režim zápasu (počet hráčů na ledě, s brankářem / bez brankáře).
     */
    private MatchMode matchMode;

    /**
     * Kapacitní a agregační údaje zápasu.
     *
     * Obsahují například maximální počet hráčů, počet přihlášených hráčů
     * celkově a v jednotlivých týmech, počet náhradníků a počet hráčů
     * v různých stavech účasti.
     */
    private int maxPlayers;
    private int inGamePlayers;
    private int inGamePlayersDark;
    private int inGamePlayersLight;
    private int substitutePlayers;
    private int outGamePlayers;
    private int waitingPlayers;
    private int noActionPlayers;
    private int noExcusedPlayersSum;
    private int remainingSlots;

    /**
     * Cena přepočtená na jednoho přihlášeného hráče.
     * Hodnota se počítá na serveru podle aktuálního stavu registrací.
     */
    private double pricePerRegisteredPlayer;

    /**
     * Stav přihlášeného hráče k tomuto zápasu.
     */
    private PlayerMatchStatus playerMatchStatus;

    /**
     * Informace o omluvě přihlášeného hráče, pokud je k dispozici.
     */
    private ExcuseReason excuseReason;
    private String excuseNote;

    /**
     * Stav zápasu a případný důvod jeho zrušení.
     */
    private MatchStatus matchStatus;
    private MatchCancelReason cancelReason;

    /**
     * ID sezóny, do které zápas patří.
     * Hodnota se nastavuje pouze na serveru.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long seasonId;

    /**
     * Seznamy hráčů rozdělené podle stavu registrace.
     *
     * Seznamy se používají pro přehledné zobrazení v uživatelském rozhraní,
     * například pro zvýraznění přihlášených hráčů, náhradníků nebo
     * neomluvených hráčů.
     */
    private List<PlayerDTO> registeredPlayers;
    private List<PlayerDTO> registeredDarkPlayers;
    private List<PlayerDTO> registeredLightPlayers;
    private List<PlayerDTO> reservedPlayers;
    private List<PlayerDTO> unregisteredPlayers;
    private List<PlayerDTO> excusedPlayers;
    private List<PlayerDTO> substitutedPlayers;
    private List<PlayerDTO> noExcusedPlayers;
    private List<PlayerDTO> noResponsePlayers;
    // plný seznam registrací pro FE
    /**
     * Seznam všech registrací k zápasu včetně týmu, stavu a pozice v zápase.
     *
     * Slouží zejména pro frontendovou logiku, která potřebuje znát obsazení
     * jednotlivých pozic v rámci týmů (například pro blokaci dalších registrací
     * na již plně obsazenou pozici).
     */
    private List<MatchRegistrationDTO> registrations;
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

    public MatchMode getMatchMode() { return matchMode; }
    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }

    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }

    public int getInGamePlayers() { return inGamePlayers; }
    public void setInGamePlayers(int inGamePlayers) { this.inGamePlayers = inGamePlayers; }

    public int getInGamePlayersDark() { return inGamePlayersDark; }
    public void setInGamePlayersDark(int inGamePlayersDark) { this.inGamePlayersDark = inGamePlayersDark; }

    public int getInGamePlayersLight() { return inGamePlayersLight; }
    public void setInGamePlayersLight(int inGamePlayersLight) { this.inGamePlayersLight = inGamePlayersLight; }

    public int getSubstitutePlayers() { return substitutePlayers; }
    public void setSubstitutePlayers(int substitutePlayers) { this.substitutePlayers = substitutePlayers; }

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

    public List<PlayerDTO> getRegisteredDarkPlayers() {
        return registeredDarkPlayers;
    }

    public void setRegisteredDarkPlayers(List<PlayerDTO> registeredDarkPlayers) {
        this.registeredDarkPlayers = registeredDarkPlayers;
    }

    public List<PlayerDTO> getRegisteredLightPlayers() {
        return registeredLightPlayers;
    }

    public void setRegisteredLightPlayers(List<PlayerDTO> registeredLightPlayers) {
        this.registeredLightPlayers = registeredLightPlayers;
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

    public List<PlayerDTO> getSubstitutedPlayers() { return substitutedPlayers; }
    public void setSubstitutedPlayers(List<PlayerDTO> substitutedPlayers) {
        this.substitutedPlayers = substitutedPlayers;
    }

    public List<PlayerDTO> getNoExcusedPlayers() { return noExcusedPlayers; }
    public void setNoExcusedPlayers(List<PlayerDTO> noExcusedPlayers) {
        this.noExcusedPlayers = noExcusedPlayers;
    }

    public List<PlayerDTO> getNoResponsePlayers() { return noResponsePlayers; }
    public void setNoResponsePlayers(List<PlayerDTO> noResponsePlayers) {
        this.noResponsePlayers = noResponsePlayers;
    }

    // NumberedMatchDTO

    @Override
    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    @Override
    public Integer getMatchNumber() {
        return matchNumber;
    }
    public List<MatchRegistrationDTO> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<MatchRegistrationDTO> registrations) {
        this.registrations = registrations;
    }

}