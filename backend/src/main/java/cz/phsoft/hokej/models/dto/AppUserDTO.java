package cz.phsoft.hokej.models.dto;

import java.util.Set;

public class AppUserDTO {
    private Long id;
    private String email;
    private String role;
    private Set<PlayerSummaryDTO> players; // jednostranné

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Set<PlayerSummaryDTO> getPlayers() {
        return players;
    }

    public void setPlayers(Set<PlayerSummaryDTO> players) {
        this.players = players;
    }
}
