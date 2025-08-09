-- Flyway migration V88: Add an auto-updating timestamp to event_tasks

ALTER TABLE `event_tasks`
ADD COLUMN `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;