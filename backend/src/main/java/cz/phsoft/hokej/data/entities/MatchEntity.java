package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující hokejový zápas.
 *
 * Uchovává základní informace o zápasu, jeho kapacitě,
 * ceně, aktuálním stavu a vazbě na sezónu.
 * Informace o účasti hráčů jsou uloženy v samostatné entitě
 * MatchRegistrationEntity.
 *
 * Entita dále obsahuje auditní údaje o vytvoření a poslední
 * úpravě zápasu.
 */
@Entity
@Table(name = "matches")
public class MatchEntity {

    /**
     * Primární klíč zápasu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Datum a čas konání zápasu.
     */
    @Column(nullable = false)
    private LocalDateTime dateTime;

    /**
     * Místo konání zápasu.
     */
    @Column(nullable = false)
    private String location;

    /**
     * Volitelný popis zápasu.
     *
     * Slouží například pro doplňující informace o organizaci zápasu.
     */
    private String description;

    /**
     * Maximální počet hráčů povolených pro zápas.
     *
     * Hodnota se používá pro kontrolu kapacity při registraci hráčů.
     */
    @Column(nullable = false)
    private Integer maxPlayers;

    /**
     * Celková cena zápasu.
     *
     * Hodnota může sloužit pro výpočet podílu jednotlivých hráčů.
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * Aktuální stav zápasu.
     *
     * Stav určuje, zda je zápas plánovaný, zrušený nebo například odehraný.
     */
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    /**
     * Důvod zrušení zápasu.
     *
     * Vyplňuje se pouze v případě, že je zápas zrušen.
     */
    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    /**
     * Sezóna, do které zápas patří.
     *
     * Každý zápas musí být přiřazen k existující sezóně.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    /**
     * Časové razítko zápasu.
     *
     * Uchovává datum a čas vytvoření nebo poslední změny zápasu.
     * Hodnota se aktualizuje při každé změně záznamu.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * ID uživatele, který zápas vytvořil.
     *
     * Slouží pro auditní účely.
     */
    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    /**
     * ID uživatele, který zápas naposledy změnil.
     *
     * Slouží pro auditní účely a sledování odpovědnosti za změny.
     */
    @Column(name = "last_modified_by_user_id")
    private Long lastModifiedByUserId;

    /**
     * Bezparametrický konstruktor požadovaný JPA.
     */
    public MatchEntity() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateTime() { return dateTime; }

    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Integer getMaxPlayers() { return maxPlayers; }

    public void setMaxPlayers(Integer maxPlayers) { this.maxPlayers = maxPlayers; }

    public Integer getPrice() { return price; }

    public void setPrice(Integer price) { this.price = price; }

    public MatchStatus getMatchStatus() { return matchStatus; }

    public void setMatchStatus(MatchStatus matchStatus) { this.matchStatus = matchStatus; }

    public MatchCancelReason getCancelReason() { return cancelReason; }

    public void setCancelReason(MatchCancelReason cancelReason) { this.cancelReason = cancelReason; }

    public SeasonEntity getSeason() { return season; }

    public void setSeason(SeasonEntity season) { this.season = season; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getCreatedByUserId() { return createdByUserId; }

    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public Long getLastModifiedByUserId() { return lastModifiedByUserId; }

    public void setLastModifiedByUserId(Long lastModifiedByUserId) { this.lastModifiedByUserId = lastModifiedByUserId; }
}
