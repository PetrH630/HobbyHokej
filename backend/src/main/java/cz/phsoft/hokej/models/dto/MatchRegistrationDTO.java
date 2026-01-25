package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO reprezentující registraci hráče k zápasu.
 *
 * Slouží k přenosu informací o stavu účasti hráče
 * na konkrétním zápasu mezi backendem a klientem.
 *
 * Používá se zejména:
 * <ul>
 *     <li>při registraci / odhlášení / omluvě hráče,</li>
 *     <li>v přehledech registrací zápasu,</li>
 *     <li>v administraci účasti hráčů.</li>
 * </ul>
 */
public class MatchRegistrationDTO {

    /**
     * ID registrace.
     *
     * Vyplněno při načítání dat (GET),
     * při vytváření nové registrace může být {@code null}.
     */
    private Long id;

    @NotNull(message = "ID zápasu je povinné.")
    @Positive(message = "ID zápasu musí být kladné.")
    private Long matchId;

    @NotNull(message = "ID hráče je povinné.")
    @Positive(message = "ID hráče musí být kladné.")
    private Long playerId;

    /**
     * Aktuální stav registrace hráče k zápasu.
     */
    private PlayerMatchStatus status;

    /**
     * Důvod omluvy – vyplněn pouze pokud je
     * {@link #status} nastaven na {@code EXCUSED}.
     */
    private ExcuseReason excuseReason;

    private String excuseNote;
    private String adminNote;
    private Team team;

    /**
     * Informace o původu registrace.
     *
     * Typické hodnoty:
     * <ul>
     *     <li>{@code "user"} – akce provedená hráčem,</li>
     *     <li>{@code "system"} – automatická změna systémem.</li>
     * </ul>
     */
    @NotNull
    private String createdBy;

    /**
     * Detail hráče – používá se pro prezentační účely
     * v přehledech registrací.
     */
    private PlayerDTO playerDTO;

    public MatchRegistrationDTO() {}

    // gettery / settery

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public PlayerMatchStatus getStatus() { return status; }
    public void setStatus(PlayerMatchStatus status) { this.status = status; }

    public ExcuseReason getExcuseReason() { return excuseReason; }
    public void setExcuseReason(ExcuseReason excuseReason) { this.excuseReason = excuseReason; }

    public String getExcuseNote() { return excuseNote; }
    public void setExcuseNote(String excuseNote) { this.excuseNote = excuseNote; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public PlayerDTO getPlayerDTO() { return playerDTO; }
    public void setPlayerDTO(PlayerDTO playerDTO) { this.playerDTO = playerDTO; }
}
