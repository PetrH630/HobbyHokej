package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;

/**
 * Rozhraní pro odesílání notifikací hráčům a uživatelům.
 *
 * Rozhraní definuje jednotný vstupní bod pro notifikační logiku v aplikaci.
 * Konkrétní implementace na základě typu notifikace a poskytnutého kontextu
 * rozhoduje, jakými kanály a s jakým obsahem bude příjemce informován.
 *
 * Účel rozhraní:
 * - centralizace notifikační logiky do jednoho místa,
 * - oddělení business událostí od konkrétní formy notifikace,
 * - usnadnění rozšíření o nové typy notifikací a komunikační kanály.
 *
 * Metody tohoto rozhraní se používají v aplikačních a doménových službách
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
     * s danou událostí. U jednodušších notifikací může být null.
     *
     * Příklady využití context:
     * - MatchRegistrationEntity pro registraci, odhlášení nebo omluvu,
     * - MatchEntity pro zápasové informace,
     * - null pro vytvoření hráče nebo jednoduchou změnu stavu.
     *
     * Implementace metody typicky:
     * - vyhodnotí notifikační preference příjemce,
     * - rozhodne o použití kanálů (email, SMS, in-app),
     * - sestaví obsah zprávy a provede odeslání.
     *
     * @param player  hráč, kterému je notifikace určena
     * @param type    typ notifikace vyjadřující charakter události
     * @param context volitelný kontext notifikace použitý pro sestavení obsahu
     */
    void notifyPlayer(PlayerEntity player, NotificationType type, Object context);

    /**
     * Odešle notifikaci konkrétnímu uživateli.
     *
     * Metoda se používá zejména pro systémové notifikace na úrovni účtu,
     * například aktivace účtu, reset hesla, změna hesla nebo bezpečnostní
     * upozornění. Kontext může nést doplňující informace pro sestavení
     * obsahu zprávy.
     *
     * Implementace metody typicky:
     * - vyhodnotí globální notifikační úroveň uživatele,
     * - rozhodne o použití komunikačních kanálů,
     * - sestaví a odešle výslednou notifikaci.
     *
     * @param user    uživatel, kterému je notifikace určena
     * @param type    typ notifikace vyjadřující charakter události
     * @param context volitelný kontext notifikace použitý pro sestavení obsahu
     */
    void notifyUser(AppUserEntity user,
                    NotificationType type,
                    Object context);
}