package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;

/**
 * Servis pro ukládání aplikačních (in-app) notifikací do databáze.
 *
 * Slouží jako doplněk k NotificationService, které řeší e-mailové
 * a SMS notifikace. Tento servis vytváří zjednodušené notifikace
 * pro zobrazení v uživatelském rozhraní (badge, přehled posledních událostí,
 * panel notifikací).
 *
 * Implementace této služby typicky:
 * - využívá InAppNotificationBuilder pro sestavení textu notifikace,
 * - vytváří a ukládá odpovídající entitu notifikace,
 * - zajišťuje správné přiřazení notifikace k uživateli nebo hráči.
 */
public interface InAppNotificationService {

    /**
     * Uloží notifikaci související s hráčem.
     *
     * Typicky se používá z notifyPlayer a vytváří notifikaci
     * pro uživatele vlastnícího hráče. Text notifikace se
     * typicky sestavuje s využitím InAppNotificationBuilder.
     *
     * @param player  hráč, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu
     */
    void storeForPlayer(PlayerEntity player, NotificationType type, Object context);

    /**
     * Uloží notifikaci související s hráčem včetně informací o cílech
     * pro e-mail a SMS notifikaci.
     *
     * Tato varianta umožňuje předat do implementace doplňující údaje,
     * které mohou být využity pro navázání in-app notifikace na související
     * e-mailovou nebo SMS notifikaci (například pro interní audit nebo logování).
     *
     * @param player   hráč, kterého se notifikace týká
     * @param type     typ notifikace
     * @param context  volitelný kontext pro sestavení textu
     * @param emailTo  cílová e-mailová adresa použitá při odeslání e-mailu
     * @param smsTo    cílové telefonní číslo použité při odeslání SMS
     */
    void storeForPlayer(PlayerEntity player,
                        NotificationType type,
                        Object context,
                        String emailTo,
                        String smsTo);

    /**
     * Uloží notifikaci související s uživatelem.
     *
     * Typicky se používá z notifyUser a vytváří notifikaci
     * přímo pro daného uživatele bez vazby na konkrétního hráče.
     *
     * @param user    uživatel, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu
     */
    void storeForUser(AppUserEntity user, NotificationType type, Object context);

    /**
     * Uloží notifikaci související s uživatelem včetně informace
     * o cílové e-mailové adrese.
     *
     * Tato varianta umožňuje implementaci propojit in-app notifikaci
     * s konkrétním odeslaným e-mailem, případně tyto údaje dále
     * auditovat nebo logovat.
     *
     * @param user    uživatel, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context volitelný kontext pro sestavení textu
     * @param emailTo cílová e-mailová adresa použitá při odeslání e-mailu
     */
    void storeForUser(AppUserEntity user,
                      NotificationType type,
                      Object context,
                      String emailTo);

    /**
     * Ukládá speciální zprávu typu SPECIAL_MESSAGE
     * pro zadaného uživatele a volitelně hráče.
     *
     * Text zprávy je předáván přímo z volající vrstvy
     * a nevyužívá InAppNotificationBuilder. Tato metoda
     * se používá zejména pro jednorázová informativní sdělení,
     * která nespadají do standardizovaných typů notifikací.
     *
     * @param user         uživatel, ke kterému je notifikace přiřazena
     * @param player       hráč, kterého se notifikace týká (může být null)
     * @param messageShort stručný text notifikace pro seznam
     * @param messageFull  plný text notifikace pro detail
     */
    void storeSpecialMessage(AppUserEntity user,
                             PlayerEntity player,
                             String messageShort,
                             String messageFull);

    /**
     * Ukládá speciální zprávu typu SPECIAL_MESSAGE včetně informací
     * o e-mailovém a SMS cíli.
     *
     * Tato varianta umožňuje implementaci uchovat vazbu mezi in-app
     * notifikací a souvisejícími e-mailovými nebo SMS zprávami.
     *
     * @param user         uživatel, ke kterému je notifikace přiřazena
     * @param player       hráč, kterého se notifikace týká (může být null)
     * @param messageShort stručný text notifikace pro seznam
     * @param messageFull  plný text notifikace pro detail
     * @param emailTo      cílová e-mailová adresa použitá při odeslání e-mailu
     * @param smsTo        cílové telefonní číslo použité při odeslání SMS
     */
    void storeSpecialMessage(AppUserEntity user,
                             PlayerEntity player,
                             String messageShort,
                             String messageFull,
                             String emailTo,
                             String smsTo);
}