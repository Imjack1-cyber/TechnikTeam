-- Flyway migration V100: Add soft delete columns to the users table

ALTER TABLE `users`
ADD COLUMN `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE AFTER `suspended_reason`,
ADD COLUMN `deleted_at` TIMESTAMP NULL DEFAULT NULL AFTER `is_deleted`;

-- Add an index for efficient querying of active users
CREATE INDEX `idx_users_active_username` ON `users` (`is_deleted`, `username`);