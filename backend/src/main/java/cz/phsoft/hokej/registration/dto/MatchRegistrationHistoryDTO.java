package cz.phsoft.hokej.registration.dto;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;

import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam o registraci hráče k zápasu.
 *
 * Slouží pro auditní a přehledové účely. Obsahuje informace o tom,
 * jak se registrace v čase měnila, včetně časového razítka změny,
 * typu provedené akce, autora změny a výsledného stavu registrace.
 *
 * Datová struktura odpovídá záznamu uloženému v tabulce historie
 * registrací. Objekt neobsahuje business logiku a používá se pouze
 * pro přenos dat mezi backendem a klientem.
 */
public class MatchRegistrationHistoryDTO {

    /**
     * Jednoznačný identifikátor historického záznamu.
     */
    private Long id;

    /**
     * Typ akce, která byla nad registrací provedena.
     * Může reprezentovat například vytvoření, změnu nebo zrušení registrace.
     */
    private String action;

    /**
     * Interní administrátorská poznámka zaznamenaná v okamžiku změny.
     */
    private String adminNote;

    /**
     * Datum a čas, kdy došlo ke změně registrace.
     */
    private LocalDateTime changedAt;

    /**
     * Identifikace subjektu, který změnu provedl.
     * Slouží k rozlišení uživatelských a systémových zásahů.
     */
    private String createdBy;

    /**
     * Textová poznámka k omluvě evidovaná v okamžiku změny.
     */
    private String excuseNote;

    /**
     * Důvod omluvy hráče platný v okamžiku změny.
     */
    private ExcuseReason excuseReason;

    /**
     * Identifikátor zápasu, ke kterému se historický záznam vztahuje.
     */
    private Long matchId;

    /**
     * Identifikátor původní registrace, ke které historický záznam náleží.
     */
    private Long matchRegistrationId;

    /**
     * Původní časové razítko registrace před provedením změny.
     * Slouží pro detailní auditní rekonstrukci vývoje dat.
     */
    private LocalDateTime originalTimestamp;

    /**
     * Identifikátor hráče, jehož registrace byla změněna.
     */
    private Long playerId;

    /**
     * Stav registrace v okamžiku zaznamenané změny.
     */
    private PlayerMatchStatus status;

    /**
     * Tým, do kterého byl hráč v daném okamžiku přiřazen.
     */
    private Team team;

    /**
     * Pozice hráče v tomto konkrétním zápase v okamžiku změny.
     */
    private PlayerPosition positionInMatch;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

    public String getAdminNote() { return adminNote; }

    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getChangedAt() { return changedAt; }

    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getExcuseNote() { return excuseNote; }

    public void setExcuseNote(String excuseNote) { this.excuseNote = excuseNote; }

    public ExcuseReason getExcuseReason() { return excuseReason; }

    public void setExcuseReason(ExcuseReason excuseReason) { this.excuseReason = excuseReason; }

    public Long getMatchId() { return matchId; }

    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getMatchRegistrationId() { return matchRegistrationId; }

    public void setMatchRegistrationId(Long matchRegistrationId) { this.matchRegistrationId = matchRegistrationId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }

    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public Long getPlayerId() { return playerId; }

    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public PlayerMatchStatus getStatus() { return status; }

    public void setStatus(PlayerMatchStatus status) { this.status = status; }

    public Team getTeam() { return team; }

    public void setTeam(Team team) { this.team = team; }

    public PlayerPosition getPositionInMatch() {
        return positionInMatch;
    }

    public void setPositionInMatch(PlayerPosition positionInMatch) {
        this.positionInMatch = positionInMatch;
    }
}