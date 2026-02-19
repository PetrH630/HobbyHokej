CREATE TRIGGER trg_season_delete
AFTER DELETE ON season
FOR EACH ROW
BEGIN
    INSERT INTO season_history
        (season_id, original_timestamp, action, changed_at,
         name, start_date, end_date, active, created_by_user_id)
    VALUES
        (OLD.id, OLD.timestamp, 'DELETE', NOW(),
         OLD.name, OLD.start_date, OLD.end_date, OLD.active, OLD.created_by_user_id);
END;
