package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.Role;

import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam o uživateli.
 *
 * Slouží pro auditní a přehledové účely.
 * Obsahuje informace o změnách uživatelského účtu v čase,
 * včetně typu akce, původního časového razítka a aktuálních dat.
 */
public class AppUserHistoryDTO {

    private Long id;

    /**
     * Typ provedené operace.
     * Typicky hodnoty INSERT, UPDATE nebo DELETE.
     */
    private String action;

    /**
     * Datum a čas provedení změny.
     */
    private LocalDateTime changedAt;

    /**
     * ID uživatele z hlavní tabulky app_users.
     */
    private Long userId;

    /**
     * Původní časové razítko uživatele.
     */
    private LocalDateTime originalTimestamp;

    private String name;
    private String surname;
    private String email;
    private Role role;
    private boolean enabled;

    // gettery / settery

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
