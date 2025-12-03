package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.JerseyColor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_registrations")
public class MatchRegistrationEntity {

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

    private String adminNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "jersey_color")
    private JerseyColor jerseyColor;

    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false, updatable = false)
    private String createdBy; // "user" nebo "system"

    public MatchRegistrationEntity() {
    }

    // Gettery a Settery
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatchEntity getMatch() {
        return match;
    }

    public void setMatch(MatchEntity match) {
        this.match = match;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public PlayerMatchStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerMatchStatus status) {
        this.status = status;
    }

    public ExcuseReason getExcuseReason() {
        return excuseReason;
    }

    public void setExcuseReason(ExcuseReason excuseReason) {
        this.excuseReason = excuseReason;
    }

    public String getExcuseNote() {
        return excuseNote;
    }

    public void setExcuseNote(String excuseNote) {
        this.excuseNote = excuseNote;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public JerseyColor getJerseyColor() {
        return jerseyColor;
    }

    public void setJerseyColor(JerseyColor jerseyColor) {
        this.jerseyColor = jerseyColor;
    }
}



