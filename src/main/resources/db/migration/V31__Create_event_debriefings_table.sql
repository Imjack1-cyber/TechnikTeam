-- Flyway migration V31: Create table for post-event debriefing reports

CREATE TABLE `event_debriefings` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `event_id` INT NOT NULL,
    `author_user_id` INT NOT NULL,
    `submitted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `what_went_well` TEXT NULL,
    `what_to_improve` TEXT NULL,
    `equipment_notes` TEXT NULL,
    `standout_crew_members` TEXT NULL COMMENT 'JSON array of user IDs',
    UNIQUE KEY `uk_event_debriefing_event_id` (`event_id`),
    FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`author_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;