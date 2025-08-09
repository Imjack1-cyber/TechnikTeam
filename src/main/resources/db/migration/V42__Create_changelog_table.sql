-- Flyway migration V42: Create table for changelogs

CREATE TABLE `changelogs` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `version` VARCHAR(50) NOT NULL UNIQUE,
    `release_date` DATE NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `notes` TEXT NOT NULL,
    `is_published` BOOLEAN NOT NULL DEFAULT FALSE,
    `seen_by_users` JSON DEFAULT ('[]'),
    INDEX `idx_changelog_published_date` (`is_published`, `release_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;