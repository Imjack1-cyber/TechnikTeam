-- Flyway migration V88: Add a flag to files to indicate if a download warning is needed

ALTER TABLE `files`
ADD COLUMN `needs_warning` BOOLEAN NOT NULL DEFAULT FALSE AFTER `required_role`;