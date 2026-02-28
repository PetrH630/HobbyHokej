package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;

/**
 * Rozhraní pro odesílání notifikací hráčům a uživatelům.
 *
 * Definuje jednotný vstupní bod pro notifikační logiku v aplikaci.
 * Implementace na základě typu notifikace a kontextu rozhoduje,
 * jakým kanálem a s jakým obsahem bude příjemce informován.
 *
 * Účel:
 * - centralizovat notifikační logiku do jednoho místa,
 * - oddělit business události od konkrétní formy notifikace,
 * - umožnit snadné rozšíření o další typy notifikací a kanály.
 *
 * Metody tohoto rozhraní se typicky volají z business služeb
 * v reakci na konkrétní události (registrace na zápas, změna hesla,
 * aktivace účtu a podobně).
 */
public interface NotificationService {

    /**
     * Odešle notifikaci konkrétnímu hráči.
     *
     * Parametr context nese dodatečné informace potřebné
     * pro sestavení obsahu notifikace. Typicky se jedná
     * o doménovou entitu nebo kontextový objekt související
     * s danou událostí. Může být null u jednodušších notifikací.
     *
     * Příklady:
     * - MatchRegistrationEntity pro registraci, odhlášení a omluvu,
     * - null pro vytvoření hráče nebo změnu stavu.
     *
     * @param player  hráč, kterému je notifikace určena
     * @param type    typ notifikace
     * @param context kontextová data související s notifikací
     */
    void notifyPlayer(PlayerEntity player, NotificationType type, Object context);

    /**
     * Odešle notifikaci konkrétnímu uživateli.
     *
     * Používá se zejména pro systémové notifikace na úrovni účtu,
     * například aktivace účtu, reset hesla nebo změna hesla.
     *
     * @param user    uživatel, kterému je notifikace určena
     * @param type    typ notifikace
     * @param context kontextová data související s notifikací
     */
    void notifyUser(AppUserEntity user,
                    NotificationType type,
                    Object context);
}
