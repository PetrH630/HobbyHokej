CREATE TRIGGER trg_match_reg_insert
AFTER INSERT ON match_registrations
FOR EACH ROW
BEGIN
    INSERT INTO match_registration_history
    (match_registration_id, match_id, player_id, status, excuse_reason,
     excuse_note, admin_note, team, original_timestamp, created_by,
     action, changed_at)
    VALUES
    (NEW.id, NEW.match_id, NEW.player_id, NEW.status, NEW.excuse_reason,
     NEW.excuse_note, NEW.admin_note, NEW.team, NEW.timestamp, NEW.created_by,
     'INSERT', NOW());
END;
