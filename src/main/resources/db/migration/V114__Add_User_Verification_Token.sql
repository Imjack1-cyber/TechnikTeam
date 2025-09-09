-- Flyway migration V114: Add verification token to users table

-- Add a column to store a unique token for public user verification
ALTER TABLE `users`
ADD COLUMN `verification_token` VARCHAR(36) NULL DEFAULT NULL AFTER `admin_notes`;

-- Add a unique index to the new column
ALTER TABLE `users`
ADD UNIQUE INDEX `idx_users_verification_token` (`verification_token`);

-- Populate the token for all existing users with a UUID
UPDATE `users` SET `verification_token` = UUID() WHERE `verification_token` IS NULL;