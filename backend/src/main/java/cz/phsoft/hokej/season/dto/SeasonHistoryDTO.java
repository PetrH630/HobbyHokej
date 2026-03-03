package cz.phsoft.hokej.season.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam o sezóně.
 *
 * Slouží k přenosu auditních informací o změnách sezóny mezi backendem
 * a klientem. Obsahuje snapshot hodnot sezóny v okamžiku změny a
 * metadata o provedené akci.
 *
 * Používá se zejména v administrativních přehledech historie.
 */
public class SeasonHistoryDTO {

    /**
     * Jednoznačný identifikátor historického záznamu.
     */
    private Long id;

    /**
     * Typ provedené akce nad sezónou, například CREATE nebo UPDATE.
     */
    private String action;

    /**
     * Datum a čas, kdy ke změně došlo.
     */
    private LocalDateTime changedAt;

    /**
     * Identifikátor sezóny, ke které se historický záznam vztahuje.
     */
    private Long seasonId;

    /**
     * Původní časové razítko sezóny před provedenou změnou.
     *
     * Slouží pro dohledání stavu entity v konkrétním okamžiku.
     */
    private LocalDateTime originalTimestamp;

    /**
     * Název sezóny v době provedení změny.
     */
    private String name;

    /**
     * Datum začátku sezóny v době provedení změny.
     */
    private LocalDate startDate;

    /**
     * Datum konce sezóny v době provedení změny.
     */
    private LocalDate endDate;

    /**
     * Příznak aktivní sezóny v době provedení změny.
     */
    private boolean active;

    /**
     * ID uživatele, který změnu sezóny provedl.
     */
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