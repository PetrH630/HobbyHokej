package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;

/**
 * Kontext pro notifikace související se zapomenutým heslem.
 *
 * Obsahuje:
 * - uživatele, kterého se reset týká,
 * - odkaz pro nastavení nového hesla.
 *
 * Slouží k přenesení potřebných údajů do builderu notifikací
 * (email, SMS) bez nutnosti pracovat přímo s entitami na vyšší úrovni.
 */
public record ForgottenPasswordResetContext(
        AppUserEntity user,
        String resetLink
) {
}
