-- Flyway migration V133: Unify the availability and poll systems into a single schema
START TRANSACTION;

-- Step 1: Extend polls table
ALTER TABLE `polls`
    ADD COLUMN IF NOT EXISTS `type` ENUM('MULTIPLE_CHOICE', 'WORD_CLOUD', 'AVAILABILITY', 'SCHEDULING') NOT NULL DEFAULT 'MULTIPLE_CHOICE' AFTER `uuid`,
    ADD COLUMN IF NOT EXISTS `description` TEXT NULL AFTER `question`,
    ADD COLUMN IF NOT EXISTS `start_time` TIMESTAMP NULL DEFAULT NULL AFTER `description`,
    ADD COLUMN IF NOT EXISTS `end_time` TIMESTAMP NULL DEFAULT NULL AFTER `start_time`,
    ADD COLUMN IF NOT EXISTS `verification_code` VARCHAR(255) NULL AFTER `options`;

-- Step 2: Rebuild poll_votes with matching INT FKs
DROP TABLE IF EXISTS `poll_votes`;

CREATE TABLE `poll_votes` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `poll_id` INT NOT NULL,
    `poll_option_id` INT NULL,
    `user_id` INT NULL,
    `guest_name` VARCHAR(255) NULL DEFAULT NULL,
    `notes` TEXT NULL DEFAULT NULL,
    `voted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_poll_user_vote` (`poll_id`, `user_id`),
    UNIQUE KEY `uk_poll_guest_vote` (`poll_id`, `guest_name`),
    CONSTRAINT `fk_poll_vote_poll`
        FOREIGN KEY (`poll_id`) REFERENCES `polls` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_poll_vote_option`
        FOREIGN KEY (`poll_option_id`) REFERENCES `poll_options` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 3: Create poll_day_votes
DROP TABLE IF EXISTS `poll_day_votes`;

CREATE TABLE `poll_day_votes` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `vote_id` INT NOT NULL,
    `vote_date` DATE NOT NULL,
    `status` ENUM('AVAILABLE', 'MAYBE', 'UNAVAILABLE') NOT NULL,
    `notes` VARCHAR(255) NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vote_day` (`vote_id`, `vote_date`),
    CONSTRAINT `fk_poll_day_vote_vote`
        FOREIGN KEY (`vote_id`) REFERENCES `poll_votes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 4: Drop old availability tables (dev only)
DROP TABLE IF EXISTS `availability_day_responses`;
DROP TABLE IF EXISTS `availability_responses`;
DROP TABLE IF EXISTS `availability_polls`;

COMMIT;
