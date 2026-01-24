package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class AppUserDTO {

    private Long id;

    @NotBlank(message = "Křestní jméno je povinné.")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "Příjmení je povinné.")
    @Size(min = 2, max = 50)
    private String surname;

    @NotBlank(message = "Email je povinný.")
    @Email(message = "Email není ve správném formátu.")
    private String email;

    private Role role;
    private boolean enabled;
    private Set<PlayerDTO> players; // jednostranné


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled;}

    public Set<PlayerDTO> getPlayers() {
        return players;
    }

    public void setPlayers(Set<PlayerDTO> players) {
        this.players = players;
    }
}
