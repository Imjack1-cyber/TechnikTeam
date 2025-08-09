-- Flyway migration V32: Add permissions for event debriefings

INSERT INTO `permissions` (`permission_key`, `description`)
VALUES
    ('EVENT_DEBRIEFING_VIEW', 'Kann alle abgeschlossenen Event-Debriefings einsehen.'),
    ('EVENT_DEBRIEFING_MANAGE', 'Kann Debriefings für Events erstellen und bearbeiten, auch wenn nicht Event-Leiter.');

-- Grant this new permission to the default admin user created on first startup
INSERT INTO user_permissions (user_id, permission_id)
SELECT 1, p.id FROM permissions p
WHERE p.permission_key IN ('EVENT_DEBRIEFING_VIEW', 'EVENT_DEBRIEFING_MANAGE')
AND EXISTS (SELECT 1 FROM users WHERE id = 1);