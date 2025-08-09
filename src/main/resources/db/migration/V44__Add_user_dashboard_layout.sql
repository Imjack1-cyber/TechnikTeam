-- Flyway migration V44: Add dashboard layout preference to users table

ALTER TABLE `users`
ADD COLUMN `dashboard_layout` JSON NULL AFTER `admin_notes`;