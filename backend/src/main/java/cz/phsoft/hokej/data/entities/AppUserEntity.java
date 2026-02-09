package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entita reprezentující uživatelský účet aplikace.
 *
 * Slouží pro autentizaci a autorizaci uživatelů v systému.
 * Jeden uživatel může mít přiřazeno více hráčů.
 * Detailní nastavení uživatele je odděleno do entity AppUserSettingsEntity.
 */
@Entity
@Table(name = "app_users")
public class AppUserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    /**
     * Unikátní email uživatele sloužící pro přihlášení.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hash hesla uživatele.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role uživatele v systému.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Příznak aktivace účtu.
     * Nastaví se například po úspěšné emailové aktivaci.
     */
    @Column(nullable = false)
    private boolean enabled = false;

    /**
     * Hráči přiřazení k tomuto uživateli.
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<PlayerEntity> players;

    /**
     * Nastavení uživatele (způsob výběru hráče, notifikace a další preference).
     *
     * Jeden uživatel má právě jedno AppUserSettingsEntity.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AppUserSettingsEntity settings;

    /**
     * Časové razítko uživatele.
     * Používá se pro uložení data a času vytvoření a změn u uživatele.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }

    public void setSurname(String surname) { this.surname = surname.toUpperCase(); }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Set<PlayerEntity> getPlayers() { return players; }

    public void setPlayers(Set<PlayerEntity> players) { this.players = players; }

    public AppUserSettingsEntity getSettings() { return settings; }

    public void setSettings(AppUserSettingsEntity settings) {
        this.settings = settings;
        if (settings != null) {
            settings.setUser(this);
        }
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
