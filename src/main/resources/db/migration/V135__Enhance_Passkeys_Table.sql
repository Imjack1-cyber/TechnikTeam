-- Flyway migration V135: Add last used timestamp and user agent to passkeys table

ALTER TABLE `user_passkeys`
ADD COLUMN `last_used_at` TIMESTAMP NULL DEFAULT NULL AFTER `created_at`,
ADD COLUMN `user_agent` TEXT NULL DEFAULT NULL AFTER `last_used_at`;