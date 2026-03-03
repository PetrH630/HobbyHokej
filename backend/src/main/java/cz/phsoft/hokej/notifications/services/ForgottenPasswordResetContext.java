package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;

/**
 * Kontextový objekt přenášející údaje potřebné pro odeslání notifikací
 * souvisejících s procesem resetu zapomenutého hesla.
 *
 * Obsahuje referenci na uživatele, kterého se reset týká,
 * a jednorázový odkaz určený pro nastavení nového hesla.
 *
 * Tento objekt slouží jako transportní struktura mezi aplikační
 * logikou a vrstvou pro sestavování notifikací. Umožňuje předání
 * všech potřebných údajů bez nutnosti přímé manipulace s entitami
 * ve vyšších vrstvách aplikace.
 *
 * @param user uživatel, kterému je reset hesla určen
 * @param resetLink jednorázový odkaz pro změnu hesla
 */
public record ForgottenPasswordResetContext(
        AppUserEntity user,
        String resetLink
) {
}