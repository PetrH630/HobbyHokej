package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující zápas.
 *
 * Obsahuje základní informace o zápasu, jeho kapacitě,
 * stavu a vazbě na sezónu.
 */
@Entity
@Table(name = "matches")
public class MatchEntity {

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
     */
    private String description;

    /**
     * Maximální počet hráčů povolených pro zápas.
     */
    @Column(nullable = false)
    private Integer maxPlayers;

    /**
     * Celková cena zápasu.
     */
    @Column(nullable = false)
    private Integer price;

    /**
     * Aktuální stav zápasu (např. aktivní, zrušený).
     */
    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    /**
     * Důvod zrušení zápasu (pouze pokud je zápas zrušen).
     */
    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    /**
     * Sezóna, do které zápas patří.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "season_id", nullable = false)
    private SeasonEntity season;

    public MatchEntity() {
    }

    // gettery / settery

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
}
