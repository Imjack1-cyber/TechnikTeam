-- Flyway migration V29: Create table for user-submitted damage reports

CREATE TABLE `damage_reports` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `item_id` INT NOT NULL,
    `reporter_user_id` INT NOT NULL,
    `report_description` TEXT NOT NULL,
    `reported_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('PENDING', 'CONFIRMED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    `reviewed_by_admin_id` INT NULL,
    `reviewed_at` TIMESTAMP NULL,
    `admin_notes` TEXT NULL,
    FOREIGN KEY (`item_id`) REFERENCES `storage_items`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`reporter_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`reviewed_by_admin_id`) REFERENCES `users`(`id`) ON DELETE SET NULL,
    INDEX `idx_damage_reports_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;