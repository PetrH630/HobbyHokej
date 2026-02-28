package cz.phsoft.hokej.user.entities;

import cz.phsoft.hokej.user.enums.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam o uživateli.
 *
 * Slouží pro auditní účely a uchovávání změn uživatelského účtu v čase.
 * Každý záznam představuje stav uživatele v okamžiku provedení operace
 * nad hlavní entitou AppUserEntity.
 *
 * Záznamy jsou typicky vytvářeny databázovým triggerem při operacích
 * INSERT, UPDATE nebo DELETE.
 */
@Entity
@Table(name = "app_users_history")
public class AppUserHistoryEntity {

    /**
     * Primární klíč historického záznamu.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Typ provedené operace.
     *
     * Typicky se jedná o hodnoty INSERT, UPDATE nebo DELETE.
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * Datum a čas provedení změny.
     *
     * Udává okamžik vytvoření historického záznamu.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * ID uživatele z hlavní tabulky app_users.
     *
     * Slouží pro propojení historického záznamu s původní entitou uživatele.
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Původní časové razítko uživatele.
     *
     * Jedná se o hodnotu timestamp z AppUserEntity v okamžiku změny.
     * Umožňuje dohledat přesný stav uživatele při konkrétní operaci.
     */
    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    /**
     * Jméno uživatele v okamžiku změny.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Příjmení uživatele v okamžiku změny.
     */
    @Column(name = "surname", nullable = false)
    private String surname;

    /**
     * E-mail uživatele v okamžiku změny.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /**
     * Role uživatele v okamžiku změny.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    /**
     * Stav aktivace účtu v okamžiku změny.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    /**
     * Bezparametrický konstruktor požadovaný JPA.
     */
    public AppUserHistoryEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

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
}
