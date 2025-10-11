-- Flyway migration V134: Ensure the poll_votes table has all necessary columns and correct constraints.
-- This makes the schema robust for all poll types and idempotent against previous failed migrations.

START TRANSACTION;

-- Ensure the 'id' column exists and is the primary key.
ALTER TABLE `poll_votes` ADD COLUMN IF NOT EXISTS `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY;

-- Ensure other necessary columns exist.
ALTER TABLE `poll_votes`
    ADD COLUMN IF NOT EXISTS `guest_name` VARCHAR(255) NULL DEFAULT NULL AFTER `user_id`,
    ADD COLUMN IF NOT EXISTS `notes` TEXT NULL DEFAULT NULL AFTER `poll_option_id`,
    MODIFY COLUMN `poll_option_id` INT NULL; -- Ensure it's nullable for non-option polls

-- Drop the old primary key if it was composite and if the new 'id' PK exists.
SET @pk_name = (SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE table_schema = DATABASE() AND table_name = 'poll_votes' AND constraint_type = 'PRIMARY KEY' AND CONSTRAINT_NAME != 'PRIMARY');
SET @sql = IF(@pk_name IS NOT NULL, CONCAT('ALTER TABLE `poll_votes` DROP PRIMARY KEY, ADD PRIMARY KEY (`id`)'), 'SELECT "Primary key is already correct or does not need changing."');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Ensure unique keys exist.
SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema = DATABASE() AND table_name = 'poll_votes' AND index_name = 'uk_poll_user_vote');
SET @sql = IF(@idx_exists = 0, 'ALTER TABLE `poll_votes` ADD UNIQUE KEY `uk_poll_user_vote` (`poll_id`, `user_id`)', 'SELECT "uk_poll_user_vote exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS WHERE table_schema = DATABASE() AND table_name = 'poll_votes' AND index_name = 'uk_poll_guest_vote');
SET @sql = IF(@idx_exists = 0, 'ALTER TABLE `poll_votes` ADD UNIQUE KEY `uk_poll_guest_vote` (`poll_id`, `guest_name`)', 'SELECT "uk_poll_guest_vote exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;


COMMIT;