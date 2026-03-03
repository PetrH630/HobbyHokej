package cz.phsoft.hokej.season.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam o sezóně.
 *
 * Slouží pro auditní účely a sledování změn sezón v čase.
 * Každý záznam představuje snapshot hodnot sezóny
 * v okamžiku provedení operace.
 *
 * Entita je mapována na tabulku season_history a je
 * naplňována typicky databázovým triggerem nebo
 * aplikační logikou při změně SeasonEntity.
 */
@Entity
@Table(name = "season_history")
public class SeasonHistoryEntity {

    /**
     * Primární klíč historického záznamu.
     *
     * Hodnota je generována databází.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Typ provedené operace nad sezónou.
     *
     * Obvykle se jedná o hodnoty INSERT, UPDATE nebo DELETE.
     */
    @Column(nullable = false)
    private String action;

    /**
     * Datum a čas provedení změny.
     *
     * Představuje okamžik, kdy byla změna zaznamenána.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * ID sezóny z hlavní tabulky season.
     *
     * Slouží jako referenční vazba na původní entitu.
     */
    @Column(name = "season_id", nullable = false)
    private Long seasonId;

    /**
     * Původní časové razítko sezóny.
     *
     * Umožňuje dohledat konkrétní verzi záznamu,
     * která byla předmětem změny.
     */
    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    /**
     * Název sezóny v okamžiku provedení změny.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Datum začátku sezóny v okamžiku provedení změny.
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Datum konce sezóny v okamžiku provedení změny.
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Příznak aktivní sezóny v době provedení změny.
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * ID uživatele, který sezónu vytvořil.
     *
     * Hodnota je zkopírována z pole createdByUserId
     * v entitě SeasonEntity.
     */
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Long getSeasonId() { return seasonId; }
    public void setSeasonId(Long seasonId) { this.seasonId = seasonId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
}