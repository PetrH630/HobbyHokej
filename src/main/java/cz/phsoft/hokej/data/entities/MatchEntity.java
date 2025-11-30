package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @Column(nullable = false)
    private LocalDateTime date;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private Set<MatchRegistrationEntity> registrations = new HashSet<>();

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Set<MatchRegistrationEntity> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(Set<MatchRegistrationEntity> registrations) {
        this.registrations = registrations;
    }
}
