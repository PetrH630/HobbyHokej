package cz.phsoft.hokej.match.entities;

import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.enums.MatchResult;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.season.entities.SeasonEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující hokejový zápas.
 *
 * Uchovává základní parametry zápasu, jeho kapacitu,
 * cenu, režim hry, aktuální stav a vazbu na sezónu.
 * Informace o účasti hráčů jsou spravovány samostatnou
 * entitou MatchRegistrationEntity.
 *
 * Entita dále obsahuje auditní údaje a vložený objekt
 * reprezentující skóre zápasu.
 */
@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private String location;

    private String description;

    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_mode", nullable = false, length = 50)
    private MatchMode matchMode;

    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    /**
     * Časové razítko poslední změny zápasu.
     *
     * Hodnota se používá zejména pro auditní účely
     * a pro historizaci změn pomocí databázového triggeru.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "last_modified_by_user_id")
    private Long lastModifiedByUserId;

    /**
     * Vložený objekt reprezentující skóre zápasu.
     *
     * Hodnoty mohou být null, pokud zápas ještě nebyl odehrán.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "light", column = @Column(name = "score_light")),
            @AttributeOverride(name = "dark", column = @Column(name = "score_dark"))
    })
    private MatchScore score;

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

    public MatchMode getMatchMode() { return matchMode; }
    public void setMatchMode(MatchMode matchMode) { this.matchMode = matchMode; }

    public MatchScore getScore() { return score; }
    public void setScore(MatchScore score) { this.score = score; }

    /**
     * Určuje vítězný tým na základě aktuálního skóre.
     *
     * Pokud skóre není nastaveno nebo je výsledek nerozhodný,
     * vrací se null.
     *
     * @return vítězný tým nebo null
     */
    public Team getWinner() {
        if (score == null) {
            return null;
        }
        return score.getWinner();
    }

    /**
     * Vrací výsledek zápasu odvozený ze skóre.
     *
     * Pokud skóre není zadáno, vrací se hodnota MatchResult.NOT_PLAYED.
     *
     * @return výsledek zápasu
     */
    public MatchResult getResult() {
        if (score == null) {
            return MatchResult.NOT_PLAYED;
        }
        return score.getResult();
    }
}