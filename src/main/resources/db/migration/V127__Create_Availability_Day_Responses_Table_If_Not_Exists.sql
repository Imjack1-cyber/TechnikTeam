-- Flyway migration V127: Ensure availability_day_responses table exists.
-- This script is idempotent and safely creates the table if it was missed
-- during a previous failed migration.

CREATE TABLE IF NOT EXISTS `availability_day_responses` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `response_id` INT NOT NULL,
    `vote_date` DATE NOT NULL,
    `status` ENUM('AVAILABLE', 'MAYBE', 'UNAVAILABLE') NOT NULL,
    `notes` VARCHAR(255) NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_response_day` (`response_id`, `vote_date`),
    CONSTRAINT `fk_availability_day_response_resp` FOREIGN KEY (`response_id`) REFERENCES `availability_responses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;