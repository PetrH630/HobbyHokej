package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "match_registration_history")
public class MatchRegistrationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private MatchEntity match;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerMatchStatus status;

    @Enumerated(EnumType.STRING)
    private ExcuseReason excuseReason;

    private String excuseNote;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;



    public MatchRegistrationHistoryEntity() {}

    // --- Gettery a settery ---
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

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
