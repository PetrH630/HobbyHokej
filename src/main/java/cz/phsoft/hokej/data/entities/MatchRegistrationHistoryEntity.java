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
    @JoinColumn(name = "registration_id", nullable = false)
    private MatchRegistrationEntity registration;

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

    public MatchRegistrationEntity getRegistration() { return registration; }
    public void setRegistration(MatchRegistrationEntity registration) { this.registration = registration; }

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
