CREATE TRIGGER trg_match_delete
AFTER DELETE ON matches
FOR EACH ROW
BEGIN
    INSERT INTO matches_history
        (match_id,
         original_timestamp,
         action,
         changed_at,
         date_time,
         location,
         description,
         max_players,
         price,
         match_status,
         match_mode,
         cancel_reason,
         season_id,
         created_by_user_id,
         last_modified_by_user_id)
    VALUES
        (OLD.id,
         OLD.timestamp,
         'DELETE',
         NOW(),
         OLD.date_time,
         OLD.location,
         OLD.description,
         OLD.max_players,
         OLD.price,
         OLD.match_status,
         OLD.match_mode,
         OLD.cancel_reason,
         OLD.season_id,
         OLD.created_by_user_id,
         OLD.last_modified_by_user_id);
END;