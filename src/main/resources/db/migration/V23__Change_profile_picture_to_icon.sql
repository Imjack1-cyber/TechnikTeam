-- Flyway migration V23: Change profile picture from file path to icon class

ALTER TABLE `users`
DROP COLUMN `profile_picture_path`,
ADD COLUMN `profile_icon_class` VARCHAR(50) DEFAULT 'fa-user-circle' AFTER `theme`;