package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;

/**
 * Služba pro vyhodnocení notifikačních preferencí.
 *
 * Na základě:
 * - nastavení uživatele (AppUserSettings),
 * - nastavení hráče (PlayerSettings),
 * - typu notifikace (NotificationType)
 *
 * rozhoduje, komu a jak má být notifikace doručena.
 * Nemá na starosti samotné odesílání e-mailů nebo SMS,
 * pouze dodává rozhodnutí pro další notifikační logiku.
 */
public interface NotificationPreferencesService {

    /**
     * Na základě hráče a typu notifikace rozhodne,
     * kam má být zpráva poslána.
     *
     * Výstupem je objekt NotificationDecision, který určuje,
     * zda se má poslat e-mail hráči, e-mail uživateli,
     * SMS hráči a jaké kontakty se mají použít.
     *
     * @param player hráč, kterého se notifikace týká
     * @param type   typ notifikace
     * @return rozhodnutí, komu a kam poslat
     */
    NotificationDecision evaluate(PlayerEntity player,
                                  NotificationType type);
}
