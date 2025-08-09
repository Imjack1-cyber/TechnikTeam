-- Flyway migration V21: Add profile picture path to users table

ALTER TABLE `users`
ADD COLUMN `profile_picture_path` VARCHAR(255) DEFAULT NULL AFTER `theme`;

ALTER TABLE `profile_change_requests`
MODIFY COLUMN `requested_changes` LONGTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL;