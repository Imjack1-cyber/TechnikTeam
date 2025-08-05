-- Flyway migration V30: Add permission for managing damage reports

INSERT INTO `permissions` (`permission_key`, `description`)
VALUES ('DAMAGE_REPORT_MANAGE', 'Kann von Benutzern gemeldete Schäden einsehen und bearbeiten.');

-- Grant this new permission to the default admin user created on first startup
INSERT INTO user_permissions (user_id, permission_id)
SELECT 1, LAST_INSERT_ID()
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1);