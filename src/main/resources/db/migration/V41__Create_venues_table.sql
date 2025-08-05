-- Flyway migration V41: Create venues table and link to events

CREATE TABLE `venues` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL UNIQUE,
    `address` VARCHAR(255) NULL,
    `notes` TEXT NULL,
    `map_image_path` VARCHAR(255) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE `events`
ADD COLUMN `venue_id` INT NULL AFTER `location`,
ADD CONSTRAINT `fk_event_venue` FOREIGN KEY (`venue_id`) REFERENCES `venues`(`id`) ON DELETE SET NULL;