-- Flyway migration V123: Alter availability tables to support advanced scheduling polls

-- Step 1: Modify the availability_polls table
ALTER TABLE `availability_polls`
ADD COLUMN `uuid` VARCHAR(36) NOT NULL UNIQUE AFTER `id`,
ADD COLUMN `type` ENUM('AVAILABILITY', 'SCHEDULING') NOT NULL DEFAULT 'AVAILABILITY' AFTER `uuid`,
ADD COLUMN `options` JSON NULL AFTER `end_time`,
ADD COLUMN `verification_code` VARCHAR(255) NULL AFTER `options`;

-- Populate UUIDs for existing polls
UPDATE `availability_polls` SET `uuid` = UUID() WHERE `uuid` IS NULL;

-- Step 2: Modify the availability_responses table
ALTER TABLE `availability_responses`
ADD COLUMN `guest_name` VARCHAR(255) NULL AFTER `user_id`,
MODIFY COLUMN `user_id` INT NULL,
MODIFY COLUMN `status` ENUM('AVAILABLE', 'UNAVAILABLE', 'MAYBE') NULL,
ADD CONSTRAINT `chk_user_or_guest` CHECK (`user_id` IS NOT NULL OR `guest_name` IS NOT NULL);

-- Step 3: Deprecate the old scheduling_slots table and create the new day-based response table
DROP TABLE IF EXISTS `scheduling_slots`;

CREATE TABLE `availability_day_responses` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `response_id` INT NOT NULL,
    `vote_date` DATE NOT NULL,
    `status` ENUM('AVAILABLE', 'MAYBE', 'UNAVAILABLE') NOT NULL,
    `notes` VARCHAR(255) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_response_day` (`response_id`, `vote_date`),
    CONSTRAINT `fk_availability_day_response_resp` FOREIGN KEY (`response_id`) REFERENCES `availability_responses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;