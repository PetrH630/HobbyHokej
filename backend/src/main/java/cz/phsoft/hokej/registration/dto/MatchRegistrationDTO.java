package cz.phsoft.hokej.registration.dto;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO, které reprezentuje registraci hráče k zápasu.
 *
 * Slouží k přenosu informací o stavu účasti hráče na konkrétním zápase
 * mezi backendem a klientem. Používá se při registraci, odhlášení,
 * omluvě hráče, v přehledech registrací a v administraci účasti.
 */
public class MatchRegistrationDTO {

    /**
     * ID registrace.
     *
     * Při vytváření nové registrace může být hodnota null.
     * Hodnota se generuje na serveru.
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
     * Důvod omluvy, pokud je registrace ve stavu omluvené účasti.
     */
    private ExcuseReason excuseReason;

    /**
     * Textová poznámka k omluvě zadaná hráčem.
     */
    private String excuseNote;

    /**
     * Interní poznámka administrátora k dané registraci.
     */
    private String adminNote;

    /**
     * Tým, do kterého je hráč přiřazen v daném zápase.
     */
    private Team team;

    /**
     * Informace o původu registrace.
     *
     * Typicky se používají hodnoty jako "user" pro akce hráče
     * a "system" pro automatické změny provedené systémem.
     */
    @NotNull
    private String createdBy;

    /**
     * Pozice hráče v tomto konkrétním zápase.
     *
     * Slouží pro prezentaci rozložení hráčů na ledě
     * a pro statistiky podle postů.
     */
    private PlayerPosition positionInMatch;

    /**
     * Detail hráče. Používá se pro prezentační účely v přehledech
     * registrací, aby nebylo nutné dělat další dotazy na hráče.
     */
    private PlayerDTO playerDTO;

    public MatchRegistrationDTO() {}

    public MatchRegistrationDTO(Long id,
                                Long playerId,
                                PlayerMatchStatus status,
                                ExcuseReason excuseReason,
                                String excuseNote,
                                String adminNote,
                                Team team
    ) {
        this.id = id;
        this.playerId = playerId;
        this.status = status;
        this.excuseReason = excuseReason;
        this.excuseNote = excuseNote;
        this.adminNote = adminNote;
        this.team = team;
    }

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

    public PlayerPosition getPositionInMatch() {
        return positionInMatch;
    }

    public void setPositionInMatch(PlayerPosition positionInMatch) {
        this.positionInMatch = positionInMatch;
    }

    public PlayerDTO getPlayerDTO() { return playerDTO; }
    public void setPlayerDTO(PlayerDTO playerDTO) { this.playerDTO = playerDTO; }
}