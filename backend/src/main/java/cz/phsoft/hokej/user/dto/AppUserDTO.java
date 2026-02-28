package cz.phsoft.hokej.user.dto;

import cz.phsoft.hokej.user.enums.Role;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.time.Instant;

/**
 * DTO, které reprezentuje uživatelský účet aplikace.
 *
 * Slouží k přenosu uživatelských dat mezi backendem a klientem.
 * Používá se například při správě uživatelů, zobrazení profilu
 * nebo při přihlášení a načítání základních informací o účtu.
 *
 * DTO neobsahuje heslo. Pro změnu hesla se používá samostatné DTO.
 *
 */
public class AppUserDTO {

    private Long id;

    @NotBlank(message = "Křestní jméno je povinné.")
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank(message = "Příjmení je povinné.")
    @Size(min = 2, max = 50)
    private String surname;

    /**
     * Email uživatele, který se používá jako přihlašovací identifikátor.
     */
    @NotBlank(message = "Email je povinný.")
    @Email(message = "Email není ve správném formátu.")
    private String email;

    /**
     * Role uživatele v systému.
     *
     * Ovlivňuje oprávnění k jednotlivým endpointům a funkcím.
     */
    private Role role;

    /**
     * Příznak, zda je uživatelský účet aktivní.
     *
     * Neaktivní účet se nemůže přihlásit do aplikace.
     */
    private boolean enabled;

    /**
     * Hráči přiřazení k uživateli.
     *
     * Jedná se o datový pohled využívaný pro prezentační účely
     * na straně klienta. DTO nenese zodpovědnost za správu tohoto
     * vztahu v databázi.
     */
    private Set<PlayerDTO> players;

    /**
     * Časové razítko uživatele.
     * Používá se pro zobrazení data a času vytvoření nebo
     * poslední změny uživatelského účtu.
     *
     * Hodnota je spravována na backendu (entitou a triggery)
     * a klient ji pouze zobrazuje.
     */
    private LocalDateTime timestamp;

    /**
     * Čas předposledního přihlášení uživatele.
     *
     * Hodnota je nastavována při úspěšném přihlášení.
     * Pokud je null, uživatel se ještě nepřihlásil.
     */
    private Instant lastLoginAt;

    /**
     * Čas posledního (aktuálního) přihlášení uživatele.
     *
     * Hodnota je nastavována při úspěšném přihlášení.
     */
    private Instant currentLoginAt;


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Set<PlayerDTO> getPlayers() { return players; }
    public void setPlayers(Set<PlayerDTO> players) { this.players = players; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCurrentLoginAt() {
        return currentLoginAt;
    }

    public void setCurrentLoginAt(Instant currentLoginAt) {
        this.currentLoginAt = currentLoginAt;
    }
}
