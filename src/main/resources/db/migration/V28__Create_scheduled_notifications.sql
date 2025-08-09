-- Flyway migration V29: Add table for scheduled notifications

CREATE TABLE `scheduled_notifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `target_user_id` INT NOT NULL,
    `notification_type` VARCHAR(50) NOT NULL COMMENT 'e.g., EVENT_REMINDER',
    `related_entity_id` INT NOT NULL COMMENT 'e.g., the event_id',
    `send_at` TIMESTAMP NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT NOT NULL,
    `url` VARCHAR(512) NOT NULL,
    `status` ENUM('PENDING', 'SENT', 'ERROR') NOT NULL DEFAULT 'PENDING',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_send_at_status` (`send_at`, `status`),
    FOREIGN KEY (`target_user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;