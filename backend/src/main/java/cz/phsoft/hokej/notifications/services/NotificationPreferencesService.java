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
 * Tato služba neprovádí samotné odesílání e-mailů ani SMS,
 * ale vrací rozhodnutí ve formě objektu NotificationDecision,
 * které je následně použito v NotificationService.
 *
 * Implementace typicky:
 * - načte relevantní nastavení uživatele a hráče,
 * - vyhodnotí globální a kanálové preference,
 * - sestaví výsledek obsahující cílové adresy a příznaky odeslání.
 */
public interface NotificationPreferencesService {

    /**
     * Na základě hráče a typu notifikace rozhodne,
     * kam má být zpráva doručena.
     *
     * Metoda vyhodnotí preferenční nastavení a vrátí objekt
     * NotificationDecision, který jednoznačně určuje:
     * - zda se má poslat e-mail hráči,
     * - zda se má poslat e-mail uživateli,
     * - zda se má poslat SMS hráči,
     * - jaké konkrétní kontaktní údaje se mají použít.
     *
     * Samotné odeslání zpráv není součástí této služby.
     *
     * @param player hráč, kterého se notifikace týká
     * @param type   typ notifikace
     * @return rozhodnutí obsahující informace o cílových kanálech a kontaktech
     */
    NotificationDecision evaluate(PlayerEntity player,
                                  NotificationType type);
}