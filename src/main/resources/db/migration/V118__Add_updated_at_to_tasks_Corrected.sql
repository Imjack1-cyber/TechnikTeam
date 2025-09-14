-- Flyway migration V118: Re-apply the schema change for the task timestamp.
-- This ensures the column exists, correcting any previous failed states.
-- The IF NOT EXISTS clause makes this script safe to re-run.

ALTER TABLE `event_tasks`
ADD COLUMN IF NOT EXISTS `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;