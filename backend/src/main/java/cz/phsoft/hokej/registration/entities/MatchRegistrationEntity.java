package cz.phsoft.hokej.registration.entities;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující registraci hráče k zápasu.
 *
 * Uchovává informace o účasti hráče, jeho aktuálním stavu,
 * případné omluvě a administrativních poznámkách. Samostatná
 * entita umožňuje sledovat změny registrace a pracovat
 * s historií účasti.
 */
@Entity
@Table(name = "match_registrations")
public class MatchRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Zápas, ke kterému se registrace vztahuje.
     */
    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    /**
     * Hráč, kterého se registrace týká.
     */
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    /**
     * Aktuální stav registrace hráče k zápasu.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerMatchStatus status;

    /**
     * Důvod omluvy hráče, pokud je hráč omluven.
     */
    @Enumerated(EnumType.STRING)
    private ExcuseReason excuseReason;

    /**
     * Volitelná poznámka k omluvě hráče.
     */
    private String excuseNote;

    /**
     * Administrativní poznámka k registraci.
     */
    private String adminNote;

    /**
     * Tým, do kterého je hráč pro zápas zařazen.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    /**
     * Časové razítko registrace.
     * Používá se například pro určení pořadí přihlášení.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Původ vytvoření registrace.
     * Typické hodnoty jsou například "user" nebo "system".
     */
    @Column(nullable = false, updatable = true)
    private String createdBy;

    /**
     * Pozice hráče v tomto konkrétním zápase.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "position_in_match", length = 30)
    private PlayerPosition positionInMatch;

    /**
     * Příznak, že připomínka MATCH_REMINDER už byla
     * pro tuto registraci odeslána.
     *
     * Slouží k tomu, aby plánovač neposílal připomínku
     * stejnému hráči pro stejný zápas vícekrát.
     */
    @Column(name = "reminder_already_sent", nullable = false)
    private boolean reminderAlreadySent = false;

    public MatchRegistrationEntity() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public MatchEntity getMatch() { return match; }

    public void setMatch(MatchEntity match) { this.match = match; }

    public PlayerEntity getPlayer() { return player; }

    public void setPlayer(PlayerEntity player) { this.player = player; }

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

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public boolean isReminderAlreadySent() {
        return reminderAlreadySent;
    }

    public void setReminderAlreadySent(boolean reminderAlreadySent) {
        this.reminderAlreadySent = reminderAlreadySent;
    }

    public PlayerPosition getPositionInMatch() {
        return positionInMatch;
    }

    public void setPositionInMatch(PlayerPosition positionInMatch) {
        this.positionInMatch = positionInMatch;
    }
}