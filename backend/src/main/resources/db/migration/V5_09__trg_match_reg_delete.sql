CREATE TRIGGER trg_match_reg_delete
AFTER DELETE ON match_registrations
FOR EACH ROW
BEGIN
    INSERT INTO match_registration_history
    (match_registration_id,
     match_id,
     player_id,
     status,
     excuse_reason,
     excuse_note,
     admin_note,
     team,
     position_in_match,
     original_timestamp,
     created_by,
     reminder_already_sent,
     action,
     changed_at)
    VALUES
    (OLD.id,
     OLD.match_id,
     OLD.player_id,
     OLD.status,
     OLD.excuse_reason,
     OLD.excuse_note,
     OLD.admin_note,
     OLD.team,
     OLD.position_in_match,
     OLD.timestamp,
     OLD.created_by,
     OLD.reminder_already_sent,
     'DELETE',
     NOW());
END;