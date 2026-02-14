CREATE TRIGGER trg_app_user_delete
AFTER DELETE ON app_users
FOR EACH ROW
BEGIN
    INSERT INTO app_users_history
        (user_id, name, surname, email, role, enabled,
         original_timestamp, action, changed_at)
    VALUES
        (OLD.id, OLD.name, OLD.surname, OLD.email, OLD.role, OLD.enabled,
         OLD.timestamp, 'DELETE', NOW());
END;
