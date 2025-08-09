-- Flyway migration V38: Add admin-only notes field to users table

ALTER TABLE `users`
ADD COLUMN `admin_notes` TEXT NULL DEFAULT NULL AFTER `profile_icon_class`;