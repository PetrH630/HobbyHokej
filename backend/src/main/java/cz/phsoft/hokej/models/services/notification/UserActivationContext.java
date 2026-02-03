package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;

/**
 * Kontext pro notifikace související s aktivací uživatelského účtu.
 *
 * Obsahuje:
 * - uživatele, který má být aktivován,
 * - aktivační odkaz použitý v e-mailu.
 *
 * Slouží k oddělení doménových entit od dat potřebných
 * pro sestavení aktivačních notifikací.
 */
public record UserActivationContext(
        AppUserEntity user,
        String activationLink
) {
}
