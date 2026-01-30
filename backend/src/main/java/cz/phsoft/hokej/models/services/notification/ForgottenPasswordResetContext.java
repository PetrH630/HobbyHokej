package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;

/**
 * Kontext pro notifikace kolem zapomenutého hesla.
 *
 * Obsahuje:
 * - uživatele, kterého se reset týká,
 * - odkaz pro nastavení nového hesla.
 */
public record ForgottenPasswordResetContext(
        AppUserEntity user,
        String resetLink
) {
}
