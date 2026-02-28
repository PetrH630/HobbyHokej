package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;

/**
 * Kontext pro notifikace související s aktivací uživatelského účtu.
 *
 * Obsahuje:
 * - uživatele, který má být aktivován,
 * - aktivační odkaz použitý v e-mailu.
 *
 * Slouží k oddělení doménových entit od dat potřebných
 * pro sestavení aktivačních notifikací.
 * @param user Uživatel, kterému je aktivační odkaz určen.
 * @param activationLink Jednorázový odkaz pro aktivaci účtu.
 */
public record UserActivationContext(
        AppUserEntity user,
        String activationLink
) {
}
