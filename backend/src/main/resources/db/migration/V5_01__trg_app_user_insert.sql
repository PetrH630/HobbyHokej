CREATE TRIGGER trg_app_user_insert
AFTER INSERT ON app_users
FOR EACH ROW
BEGIN
    INSERT INTO app_users_history
        (user_id, name, surname, email, role, enabled,
         original_timestamp, action, changed_at)
    VALUES
        (NEW.id, NEW.name, NEW.surname, NEW.email, NEW.role, NEW.enabled,
         NEW.timestamp, 'INSERT', NOW());
END;
