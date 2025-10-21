-- Flyway migration V138: Idempotent fix for the granular permissions system.

START TRANSACTION;

-- Step 1: Create a junction table for role-based permissions if it doesn't exist
CREATE TABLE IF NOT EXISTS `role_permissions` (
  `role_id` INT NOT NULL,
  `permission_id` INT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Add a new permission to manage the permissions system itself, if it doesn't exist
INSERT INTO `permissions` (`permission_key`, `description`)
SELECT 'PERMISSION_MANAGE', 'Kann Rollen und individuelle Benutzerberechtigungen verwalten.'
WHERE NOT EXISTS (SELECT 1 FROM `permissions` WHERE `permission_key` = 'PERMISSION_MANAGE');

-- Step 3: Grant all existing permissions to the ADMIN role (ID=1)
-- This makes the ADMIN role a true "superuser" role by default.
INSERT IGNORE INTO `role_permissions` (role_id, permission_id)
SELECT 1, id FROM `permissions`;

COMMIT;