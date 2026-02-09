package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entita reprezentující sezónu.
 *
 * Sezóna vymezuje časové období, do kterého spadají zápasy
 * a související statistiky. V systému může být v jednom okamžiku
 * označena právě jedna sezóna jako aktivní.
 */
@Entity
@Table(name = "season")
public class SeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Název sezóny, například "2024/2025".
     */
    @Column(nullable = false)
    private String name;

    /**
     * Datum začátku sezóny.
     */
    @Column(nullable = false)
    private LocalDate startDate;

    /**
     * Datum konce sezóny.
     */
    @Column(nullable = false)
    private LocalDate endDate;

    /**
     * Příznak, zda je sezóna aktuálně aktivní.
     */
    private boolean active;

    /**
     * Identifikátor uživatele, který sezónu vytvořil.
     *
     * Hodnota se nastaví při vytvoření sezóny a při dalších změnách
     * se obvykle nemění. V historii sezóny se používá pro auditní účely.
     */
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    /**
     * Časové razítko sezóny.
     * Používá se pro zaznamenání vytvoření a poslední změny sezóny.
     */
    @Column(nullable = false, updatable = true)
    private java.time.LocalDateTime timestamp = java.time.LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        this.timestamp = java.time.LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.timestamp = java.time.LocalDateTime.now();
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

    public java.time.LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
}
