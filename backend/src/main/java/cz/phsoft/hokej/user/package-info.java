/**
 * Balíček user obsahuje aplikační logiku související se správou
 * uživatelských účtů v systému HobbyHokej.
 *
 * Součástí balíčku jsou entity reprezentující uživatele
 * a jejich nastavení, DTO objekty pro komunikaci s prezentační
 * vrstvou, mapery pro převod mezi entitami a DTO,
 * repozitáře pro přístup k perzistentním datům a servisní
 * vrstvy zajišťující business logiku nad uživatelskými účty.
 *
 * Odpovědnosti zahrnují zejména:
 * - registraci uživatelů a aktivaci účtů,
 * - změnu a reset hesla,
 * - správu uživatelského nastavení,
 * - evidenci historie změn účtu pro auditní účely.
 *
 * Autentizace a autorizace jsou řešeny bezpečnostní konfigurací
 * aplikace a nejsou primární odpovědností tohoto balíčku.
 */
package cz.phsoft.hokej.user;