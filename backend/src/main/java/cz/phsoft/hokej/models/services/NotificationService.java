package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;

public interface NotificationService {


    /**
     * Hlavní vstupní bod pro notifikace hráče.
     *
     * @param player   konkrétní hráč (má v sobě usera + NotificationSettings)
     * @param type     typ notifikace (PLAYER_CREATED, PLAYER_REGISTERED, ...)
     * @param context  kontextová entita – typicky:
     *                 - MatchRegistrationEntity pro registrace / odhlášení / omluvu
     *                 - null pro jednoduché notifikace (vytvoření hráče, schválení, ...)
     */
    void notifyPlayer(PlayerEntity player,NotificationType type, Object context);


}
