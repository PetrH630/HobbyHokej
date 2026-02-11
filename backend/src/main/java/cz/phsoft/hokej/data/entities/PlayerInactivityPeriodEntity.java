package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující období neaktivity hráče.
 *
 * Slouží k evidenci časových úseků, ve kterých se hráč
 * neúčastní zápasů, například z důvodu zranění nebo dovolené.
 * Období neaktivity se používá při vyhodnocování dostupnosti
 * hráče pro jednotlivé zápasy.
 */
@Entity
@Table(name = "player_inactivity_period")
public class PlayerInactivityPeriodEntity {

    /**
     * Primární klíč záznamu o neaktivitě.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hráč, ke kterému se období neaktivity vztahuje.
     *
     * Vazba je povinná, protože každé období neaktivity
     * musí být přiřazeno konkrétnímu hráči.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    /**
     * Začátek období neaktivity.
     *
     * Určuje datum a čas, od kterého je hráč považován za neaktivního.
     */
    @Column(name = "inactive_from", nullable = false)
    private LocalDateTime inactiveFrom;

    /**
     * Konec období neaktivity.
     *
     * Určuje datum a čas, do kterého je hráč považován za neaktivního.
     */
    @Column(name = "inactive_to", nullable = false)
    private LocalDateTime inactiveTo;

    /**
     * Důvod neaktivity hráče.
     *
     * Slouží k evidenci příčiny absence, například zranění,
     * dovolená nebo jiný osobní důvod.
     */
    @Column(name = "inactivity_reason", nullable = false)
    private String inactivityReason;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public PlayerEntity getPlayer() { return player; }

    public void setPlayer(PlayerEntity player) { this.player = player; }

    public LocalDateTime getInactiveFrom() { return inactiveFrom; }

    public void setInactiveFrom(LocalDateTime inactiveFrom) { this.inactiveFrom = inactiveFrom; }

    public LocalDateTime getInactiveTo() { return inactiveTo; }

    public void setInactiveTo(LocalDateTime inactiveTo) { this.inactiveTo = inactiveTo; }

    public String getInactivityReason() {
        return inactivityReason;
    }

    public void setInactivityReason(String inactivityReason) {
        this.inactivityReason = inactivityReason;
    }
}
