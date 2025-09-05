-- Flyway migration V113: Add fcm_token column to users table if it doesn't already exist.
-- The previous V95 migration has been merged and this file may cause conflicts.
-- This script is now idempotent.

-- This script checks if the column exists before trying to add it.
-- NOTE: This syntax is specific to MariaDB/MySQL and might not work on other databases.
SET @s = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE table_schema = DATABASE()
        AND table_name = 'users'
        AND column_name = 'fcm_token'
    ) > 0,
    "SELECT 'Column fcm_token already exists in users table.' AS ' ';",
    "ALTER TABLE `users` ADD COLUMN `fcm_token` VARCHAR(255) NULL DEFAULT NULL AFTER `theme`;"
));

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- This script checks if the index exists before trying to add it.
SET @s = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE table_schema = DATABASE()
        AND table_name = 'users'
        AND index_name = 'idx_users_fcm_token'
    ) > 0,
    "SELECT 'Index idx_users_fcm_token already exists on users table.' AS ' ';",
    "CREATE INDEX `idx_users_fcm_token` ON `users`(`fcm_token`);"
));

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;