-- Flyway migration V97: Add columns to support revoking admin log actions
-- MODIFIED: Removed ADD COLUMN statements for columns 'status', 'context', 'revoked_by_admin_id', and 'revoked_at' as they are already present in V1.

-- Add constraint to track who revoked an action and when
ALTER TABLE `admin_logs`
ADD CONSTRAINT `fk_admin_log_revoked_by` FOREIGN KEY (`revoked_by_admin_id`) REFERENCES `users`(`id`) ON DELETE SET NULL;

-- Add a new permission for revoking actions
INSERT INTO `permissions` (`permission_key`, `description`)
VALUES ('LOG_REVOKE', 'Kann protokollierte Admin-Aktionen rückgängig machen.');

-- Grant to default admin
INSERT INTO user_permissions (user_id, permission_id)
SELECT 1, LAST_INSERT_ID()
WHERE EXISTS (SELECT 1 FROM users WHERE id = 1);