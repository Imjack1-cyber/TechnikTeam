-- Flyway migration V25: Add permission for sending notifications

INSERT INTO `permissions` (`permission_key`, `description`)
VALUES ('NOTIFICATION_SEND', 'Kann systemweite Benachrichtigungen an Benutzer senden.');

-- Grant this new permission to the default admin user created on first startup
INSERT INTO user_permissions (user_id, permission_id)
SELECT 1, LAST_INSERT_ID()
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1);