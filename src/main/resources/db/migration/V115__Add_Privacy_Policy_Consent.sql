-- Flyway migration V88: Add privacy policy consent tracking to users table

ALTER TABLE `users`
ADD COLUMN `privacy_policy_version` VARCHAR(20) NULL DEFAULT NULL AFTER `deleted_at`,
ADD COLUMN `privacy_policy_accepted_at` TIMESTAMP NULL DEFAULT NULL AFTER `privacy_policy_version`;