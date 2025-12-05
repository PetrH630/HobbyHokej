package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_inactivity_period")
public class PlayerInactivityPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    // neaktivní od
    @Column(name = "inactive_from", nullable = false)
    private LocalDateTime inactiveFrom;

    // neaktivní do
    @Column(name = "inactive_to", nullable = false)
    private LocalDateTime inactiveTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

    public LocalDateTime getInactiveFrom() {
        return inactiveFrom;
    }

    public void setInactiveFrom(LocalDateTime inactiveFrom) {
        this.inactiveFrom = inactiveFrom;
    }

    public LocalDateTime getInactiveTo() {
        return inactiveTo;
    }

    public void setInactiveTo(LocalDateTime inactiveTo) {
        this.inactiveTo = inactiveTo;
    }

}
