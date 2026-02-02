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
}
