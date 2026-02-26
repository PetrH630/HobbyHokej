CREATE TRIGGER trg_player_delete
AFTER DELETE ON player_entity
FOR EACH ROW
BEGIN
    INSERT INTO player_entity_history
        (player_id,
         name,
         surname,
         nickname,
         type,
         full_name,
         phone_number,
         team,
         primary_position,
         secondary_position,
         player_status,
         user_id,
         original_timestamp,
         action,
         changed_at)
    VALUES
        (OLD.id,
         OLD.name,
         OLD.surname,
         OLD.nickname,
         OLD.type,
         OLD.full_name,
         OLD.phone_number,
         OLD.team,
         OLD.primary_position,
         OLD.secondary_position,
         OLD.player_status,
         OLD.user_id,
         OLD.timestamp,
         'DELETE',
         NOW());
END;