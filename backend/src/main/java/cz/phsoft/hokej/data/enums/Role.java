package cz.phsoft.hokej.data.enums;

/**
 * Role aplikačního uživatele.
 *
 * Hodnoty se typicky mapují na autorizační systém Spring Security
 * (GrantedAuthority) a určují, k jakým funkcím má uživatel přístup.
 */
public enum Role {
    /**
     * Běžný uživatel zaměřený na práci s vlastními hráči a zápasy.
     */
    ROLE_PLAYER,
    /**
     * Manažer týmu s rozšířenými oprávněními pro správu hráčů a zápasů.
     */
    ROLE_MANAGER,
    /**
     * Administrátor aplikace s plnými oprávněními.
     */
    ROLE_ADMIN
}
