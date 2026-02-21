package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;

/**
 * Servis pro ukládání aplikačních (in-app) notifikací do databáze.
 *
 * Slouží jako doplněk k NotificationService, které řeší e-mail
 * a SMS notifikace. Tento servis vytváří zjednodušené notifikace
 * pro zobrazení v UI (badge, přehled posledních událostí).
 */
public interface InAppNotificationService {

    /**
     * Uloží notifikaci související s hráčem.
     *
     * Typicky se používá z notifyPlayer a vytváří notifikaci
     * pro uživatele vlastnícího hráče.
     *
     * @param player  hráč, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu
     */
    void storeForPlayer(PlayerEntity player, NotificationType type, Object context);

    /**
     * Uloží notifikaci související s uživatelem.
     *
     * Typicky se používá z notifyUser a vytváří notifikaci
     * přímo pro daného uživatele.
     *
     * @param user    uživatel, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu
     */
    void storeForUser(AppUserEntity user, NotificationType type, Object context);
}