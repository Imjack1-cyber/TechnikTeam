-- Flyway migration V37: Add table for event inventory checklists

CREATE TABLE `event_inventory_checklist` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `event_id` INT NOT NULL,
    `item_id` INT NOT NULL,
    `quantity` INT NOT NULL,
    `status` ENUM('PENDING', 'PACKED_OUT', 'RETURNED_CHECKED', 'RETURNED_DEFECT') NOT NULL DEFAULT 'PENDING',
    `last_updated_by_user_id` INT NULL,
    `last_updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_event_item` (`event_id`, `item_id`),
    FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`item_id`) REFERENCES `storage_items`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`last_updated_by_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;