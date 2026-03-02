package cz.phsoft.hokej.match.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.enums.MatchResult;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.player.enums.Team;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO, které reprezentuje základní informace o zápasu.
 *
 * Slouží k přenosu dat o zápasech mezi backendem a klientem,
 * například při vytváření, editaci nebo zobrazování výsledků zápasu.
 *
 * DTO neobsahuje business logiku ani vazby na entity
 * a slouží pouze jako transportní objekt.
 */
public class MatchDTO implements NumberedMatchDTO {

    /**
     * Pořadové číslo zápasu v sezóně počítané podle data v rámci dané sezóny.
     * Hodnota se nastavuje pouze na serveru a na klienta se vrací jako read-only údaj.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer matchNumber;

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
     * Režim zápasu (počet hráčů na ledě, s brankářem / bez brankáře).
     */
    @NotNull(message = "Režim zápasu je povinný.")
    @Enumerated(EnumType.STRING)
    private MatchMode matchMode;

    /**
     * Aktuální stav zápasu.
     */
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    /**
     * Důvod zrušení zápasu, pokud byl zápas zrušen.
     */
    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    /**
     * ID sezóny, do které zápas patří.
     * Hodnota se nastavuje pouze na serveru.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long seasonId;

    /**
     * Počet branek týmu LIGHT.
     *
     * Hodnota může být null, pokud se zápas ještě neodehrál.
     */
    private Integer scoreLight;

    /**
     * Počet branek týmu DARK.
     *
     * Hodnota může být null, pokud se zápas ještě neodehrál.
     */
    private Integer scoreDark;

    /**
     * Výsledek zápasu odvozený ze skóre.
     *
     * Hodnota se nastavuje pouze na serveru.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MatchResult result;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Team winner;

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

    public MatchMode getMatchMode() { return matchMode; }

    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }

    public MatchStatus getMatchStatus() { return matchStatus; }

    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }

    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public Long getSeasonId() { return seasonId; }

    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    public Integer getScoreLight() {
        return scoreLight;
    }

    public void setScoreLight(Integer scoreLight) {
        this.scoreLight = scoreLight;
    }

    public Integer getScoreDark() {
        return scoreDark;
    }

    public void setScoreDark(Integer scoreDark) {
        this.scoreDark = scoreDark;
    }

    public Team getWinner() {
        return winner;
    }

    public void setWinner(Team winner) {
        this.winner = winner;
    }

    public MatchResult getResult() {
        return result;
    }

    public void setResult(MatchResult result) {
        this.result = result;
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
}