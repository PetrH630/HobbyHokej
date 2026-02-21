package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.Role;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entita reprezentující uživatelský účet aplikace.
 *
 * Slouží pro autentizaci a autorizaci uživatelů v systému.
 * Uchovává základní identifikační údaje, přihlašovací informace,
 * roli uživatele a stav aktivace účtu.
 *
 * Jeden uživatel může mít přiřazeno více hráčů. Detailní uživatelské
 * preference a nastavení jsou odděleny do entity AppUserSettingsEntity.
 */
@Entity
@Table(name = "app_users")
public class AppUserEntity {

    /**
     * Primární klíč uživatele.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Křestní jméno uživatele.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Příjmení uživatele.
     * Při uložení je převáděno na velká písmena.
     */
    @Column(nullable = false)
    private String surname;

    /**
     * Unikátní e-mail uživatele sloužící pro přihlášení do systému.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Hash hesla uživatele.
     * Heslo se nikdy neukládá v otevřené podobě.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role uživatele v systému.
     * Určuje oprávnění uživatele při přístupu k jednotlivým endpointům.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Příznak aktivace účtu.
     * Nastavuje se například po úspěšné e-mailové aktivaci účtu.
     */
    @Column(nullable = false)
    private boolean enabled = false;

    /**
     * Hráči přiřazení k tomuto uživateli.
     *
     * Vztah je typu one-to-many, kdy jeden uživatel může mít více hráčů.
     * Životní cyklus hráčů je navázán na uživatele.
     */
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<PlayerEntity> players;

    /**
     * Nastavení uživatele.
     *
     * Obsahuje například způsob výběru aktuálního hráče,
     * nastavení notifikací a další preference.
     * Jeden uživatel má právě jedno nastavení.
     */
    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private AppUserSettingsEntity settings;

    /**
     * Časové razítko uživatele.
     *
     * Uchovává datum a čas poslední změny entity.
     * Hodnota se aktualizuje při vytvoření i při každé úpravě záznamu.
     */
    @Column(nullable = false, updatable = true)
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Metoda volaná před prvním uložením entity.
     *
     * Nastavuje aktuální časové razítko a převádí příjmení
     * na velká písmena.
     */
    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
        if (surname != null) {
            this.surname = this.surname.toUpperCase();
        }
    }

    /**
     * Metoda volaná před aktualizací entity.
     *
     * Aktualizuje časové razítko a převádí příjmení
     * na velká písmena.
     */
    @PreUpdate
    public void preUpdate() {
        this.timestamp = LocalDateTime.now();
        if (surname != null) {
            this.surname = this.surname.toUpperCase();
        }
    }

    /**
     * Uchovává čas předposledního přihlášení uživatele.
     * Používá se pro výpočet notifikací od posledního přihlášení.
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Uchovává čas posledního přihlášení uživatele.
     * Aktualizuje se při každém úspěšném přihlášení.
     */
    @Column(name = "current_login_at")
    private Instant currentLoginAt;


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

    /**
     * Nastavuje uživatelské nastavení a zároveň zajišťuje obousměrnou vazbu.
     *
     * @param settings instance nastavení uživatele
     */
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
