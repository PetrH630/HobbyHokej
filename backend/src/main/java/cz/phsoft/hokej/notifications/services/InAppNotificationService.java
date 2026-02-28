package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;

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

    void storeForPlayer(PlayerEntity player,
                        NotificationType type,
                        Object context,
                        String emailTo,
                        String smsTo);
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

    void storeForUser(AppUserEntity user,
                      NotificationType type,
                      Object context,
                      String emailTo);
    /**
     * Ukládá speciální zprávu typu SPECIAL_MESSAGE
     * pro zadaného uživatele a (volitelně) hráče.
     *
     * Text zprávy je předáván přímo z volající vrstvy
     * a nevyužívá InAppNotificationBuilder.
     *
     * @param user uživatel, ke kterému je notifikace přiřazena
     * @param player hráč, kterého se notifikace týká (může být null)
     * @param messageShort stručný text notifikace pro seznam
     * @param messageFull plný text notifikace pro detail
     */
    void storeSpecialMessage(AppUserEntity user,
                             PlayerEntity player,
                             String messageShort,
                             String messageFull);

    void storeSpecialMessage(AppUserEntity user,
                             PlayerEntity player,
                             String messageShort,
                             String messageFull,
                             String emailTo,
                             String smsTo);
}