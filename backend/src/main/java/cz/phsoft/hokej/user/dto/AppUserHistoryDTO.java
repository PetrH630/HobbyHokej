package cz.phsoft.hokej.user.dto;

import cz.phsoft.hokej.user.enums.Role;

import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam o uživateli.
 *
 * Slouží pro auditní a přehledové účely. Obsahuje informace o změnách
 * uživatelského účtu v čase, včetně typu akce, časového razítka změny
 * a aktuálních dat uživatele v době změny.
 *
 * Data v tomto DTO typicky pocházejí z auditní historie, která je
 * spravována databází nebo speciální vrstvou na backendu.
 */
public class AppUserHistoryDTO {

    /**
     * Jednoznačný identifikátor historického záznamu.
     */
    private Long id;

    /**
     * Typ provedené operace.
     *
     * Typicky se používají hodnoty INSERT, UPDATE nebo DELETE
     * podle toho, jaká změna byla nad uživatelským účtem provedena.
     */
    private String action;

    /**
     * Datum a čas provedení změny.
     *
     * Hodnota reprezentuje okamžik, kdy byla změna zaznamenána
     * v auditní historii.
     */
    private LocalDateTime changedAt;

    /**
     * ID uživatele z hlavní tabulky app_users.
     *
     * Slouží k propojení historického záznamu s aktuální entitou
     * uživatele v databázi.
     */
    private Long userId;

    /**
     * Původní časové razítko uživatele.
     *
     * Hodnota obvykle odpovídá timestampu z hlavní tabulky v době,
     * kdy byla změna zaznamenána.
     */
    private LocalDateTime originalTimestamp;

    /**
     * Křestní jméno uživatele v době provedení změny.
     */
    private String name;

    /**
     * Příjmení uživatele v době provedení změny.
     */
    private String surname;

    /**
     * E-mail uživatele v době provedení změny.
     *
     * Slouží pro zpětné dohledání změn, které se týkaly konkrétní
     * e-mailové adresy.
     */
    private String email;

    /**
     * Role uživatele v době provedení změny.
     */
    private Role role;

    /**
     * Příznak, zda byl uživatelský účet v době změny aktivní.
     */
    private boolean enabled;

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