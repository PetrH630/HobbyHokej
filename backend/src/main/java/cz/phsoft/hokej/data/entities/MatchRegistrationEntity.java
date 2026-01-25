package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující registraci hráče k zápasu.
 *
 * Uchovává informace o účasti hráče, jeho stavu,
 * případné omluvě a administrativních poznámkách.
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
     * Důvod omluvy hráče (pouze pokud je hráč omluven).
     */
    @Enumerated(EnumType.STRING)
    private ExcuseReason excuseReason;

    /**
     * Volitelná poznámka k omluvě hráče.
     */
    private String excuseNote;

    /**
     * Administrativní poznámka k registraci
     * (např. neomluvená absence).
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
     *
     * Používá se pro určení pořadí registrací
     * (např. při uvolnění místa po odhlášení).
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Původ vytvoření registrace.
     *
     * Typicky:
     * - "user"   – registrace vytvořená hráčem,
     * - "system" – automatická registrace dle kapacity.
     */
    @Column(nullable = false, updatable = true)
    private String createdBy;

    public MatchRegistrationEntity() {
    }

    // gettery / settery

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
}
