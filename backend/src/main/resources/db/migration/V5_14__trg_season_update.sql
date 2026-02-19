CREATE TRIGGER trg_season_update
AFTER UPDATE ON season
FOR EACH ROW
BEGIN
    INSERT INTO season_history
        (season_id, original_timestamp, action, changed_at,
         name, start_date, end_date, active, created_by_user_id)
    VALUES
        (NEW.id, NEW.timestamp, 'UPDATE', NOW(),
         NEW.name, NEW.start_date, NEW.end_date, NEW.active, NEW.created_by_user_id);
END;
