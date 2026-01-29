package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.models.services.NotificationDecision;

/**
 * Service, která vyhodnocuje notifikační preference
 * na základě:
 * - AppUserSettings (nastavení účtu),
 * - PlayerSettings (nastavení hráče),
 * - typu notifikace (NotificationType).
 *
 * Nemá na starosti POSÍLÁNÍ emailů/SMS,
 * pouze říká KOMU a JAK má být notifikace doručena.
 */
public interface NotificationPreferencesService {

    /**
     * Na základě hráče a typu notifikace rozhodne,
     * kam má být zpráva poslána.
     *
     * @param player hráč, kterého se notifikace týká
     * @param type   typ notifikace
     * @return rozhodnutí, komu a kam poslat
     */
    NotificationDecision evaluate(PlayerEntity player,
                                  NotificationType type);
}
