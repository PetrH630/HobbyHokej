package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.data.enums.MatchStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * třída pro vkládání Entity zápasu do db
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

    // maximální počet hráčů - využívá se pro ověření kapacity při přihlášení
    @Column(nullable = false)
    private Integer maxPlayers;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Enumerated(EnumType.STRING)
    private MatchCancelReason cancelReason;

    public MatchEntity() {}

    // Gettery a settery
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

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public MatchCancelReason getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(MatchCancelReason cancelReason) {
        this.cancelReason = cancelReason;
    }
}
