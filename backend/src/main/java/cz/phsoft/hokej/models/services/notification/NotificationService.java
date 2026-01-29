package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;

/**
 * Rozhraní pro odesílání notifikací hráčům.
 * <p>
 * Definuje jednotný vstupní bod pro notifikační logiku v aplikaci.
 * Na základě typu notifikace a kontextu rozhoduje implementace,
 * jakým kanálem (SMS, email, …) a s jakým obsahem bude hráč informován.
 * </p>
 *
 * Účel:
 * <ul>
 *     <li>centralizovat notifikační logiku do jednoho místa,</li>
 *     <li>oddělit business události od konkrétní formy notifikace,</li>
 *     <li>umožnit snadné rozšíření o další typy notifikací a kanály.</li>
 * </ul>
 *
 * Použití:
 * <ul>
 *     <li>využívá se v business službách (registrace na zápasy, správa hráčů),</li>
 *     <li>volá se vždy v reakci na konkrétní business událost.</li>
 * </ul>
 *
 * Implementační poznámky:
 * <ul>
 *     <li>implementace typicky respektuje nastavení notifikací hráče
 *     (např. povolení SMS / emailu),</li>
 *     <li>odesílání notifikací by mělo být odolné vůči selhání
 *     jednotlivých kanálů,</li>
 *     <li>selhání notifikace nesmí ovlivnit hlavní business proces.</li>
 * </ul>
 */
public interface NotificationService {

    /**
     * Odešle notifikaci konkrétnímu hráči.
     * <p>
     * Metoda slouží jako hlavní vstupní bod pro notifikace hráče
     * v reakci na business události v systému.
     * </p>
     *
     * Parametr {@code context}:
     * <ul>
     *     <li>nese dodatečné informace potřebné pro sestavení obsahu notifikace,</li>
     *     <li>typicky se jedná o doménovou entitu vztahující se k události,</li>
     *     <li>může být {@code null} pro jednoduché notifikace.</li>
     * </ul>
     *
     * Typické příklady kontextu:
     * <ul>
     *     <li>{@code MatchRegistrationEntity} – registrace / odhlášení / omluva,</li>
     *     <li>{@code null} – vytvoření hráče, schválení hráče, změna stavu.</li>
     * </ul>
     *
     * @param player  hráč, kterému je notifikace určena
     * @param type    typ notifikace (např. PLAYER_CREATED, PLAYER_REGISTERED, …)
     * @param context kontextová data související s notifikací
     */
    void notifyPlayer(PlayerEntity player, NotificationType type, Object context);

}
