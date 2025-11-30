package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private String fullName = name + " " + surname;

    @ManyToMany(mappedBy = "players")
    private Set<MatchEntity> matches = new HashSet<>();

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Set<MatchEntity> getMatches() {
        return matches;
    }

    public void setMatches(Set<MatchEntity> matches) {
        this.matches = matches;
    }
}
