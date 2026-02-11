package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entita reprezentující sezónu.
 *
 * Sezóna vymezuje časové období, do kterého spadají zápasy
 * a související statistiky. V systému může být v jednom okamžiku
 * označena právě jedna sezóna jako aktivní.
 *
 * Entita dále obsahuje auditní údaje o vytvoření a poslední
 * změně záznamu.
 */
@Entity
@Table(name = "season")
public class SeasonEntity {

    /**
     * Primární klíč sezóny.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Název sezóny.
     *
     * Například hodnota ve formátu "2024/2025".
     */
    @Column(nullable = false)
    private String name;

    /**
     * Datum začátku sezóny.
     *
     * Určuje první den, od kterého jsou zápasy
     * do sezóny zahrnovány.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Datum konce sezóny.
     *
     * Určuje poslední den, do kterého sezóna trvá.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Příznak, zda je sezóna aktuálně aktivní.
     *
     * Aktivní sezóna se používá jako výchozí při vytváření
     * nových zápasů a při filtrování dat.
     */
    private boolean active;

    /**
     * Identifikátor uživatele, který sezónu vytvořil.
     *
     * Hodnota se nastavuje při vytvoření sezóny
     * a slouží pro auditní účely.
     */
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    /**
     * Časové razítko sezóny.
     *
     * Uchovává datum a čas vytvoření nebo poslední změny sezóny.
     * Hodnota se aktualizuje při každém uložení entity.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Metoda volaná před prvním uložením entity.
     *
     * Nastavuje aktuální časové razítko.
     */
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Metoda volaná před aktualizací entity.
     *
     * Aktualizuje časové razítko.
     */
    @PreUpdate
    public void preUpdate() {
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

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

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
