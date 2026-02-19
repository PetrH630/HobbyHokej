CREATE TRIGGER trg_player_insert
AFTER INSERT ON player_entity
FOR EACH ROW
BEGIN
    INSERT INTO player_entity_history
        (player_id, name, surname, nickname, type, full_name, phone_number,
         team, player_status, user_id, original_timestamp, action, changed_at)
    VALUES
        (NEW.id, NEW.name, NEW.surname, NEW.nickname, NEW.type, NEW.full_name, NEW.phone_number,
         NEW.team, NEW.player_status, NEW.user_id, NEW.timestamp, 'INSERT', NOW());
END;
