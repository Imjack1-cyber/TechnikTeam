-- Flyway migration V45: Create tables for pre-flight checklists

-- Table for reusable checklist templates
CREATE TABLE `preflight_checklist_templates` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(150) NOT NULL UNIQUE,
    `description` TEXT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table for the items within a template
CREATE TABLE `preflight_checklist_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `template_id` INT NOT NULL,
    `item_text` VARCHAR(255) NOT NULL,
    `display_order` INT NOT NULL DEFAULT 0,
    FOREIGN KEY (`template_id`) REFERENCES `preflight_checklist_templates`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Link a template to an event
ALTER TABLE `events`
ADD COLUMN `preflight_template_id` INT NULL AFTER `venue_id`,
ADD CONSTRAINT `fk_event_preflight_template` FOREIGN KEY (`preflight_template_id`) REFERENCES `preflight_checklist_templates`(`id`) ON DELETE SET NULL;

-- Table to track the status of each checklist item for a specific event
CREATE TABLE `event_preflight_checklist_status` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `event_id` INT NOT NULL,
    `item_id` INT NOT NULL,
    `is_completed` BOOLEAN NOT NULL DEFAULT FALSE,
    `completed_by_user_id` INT NULL,
    `completed_at` TIMESTAMP NULL,
    UNIQUE KEY `uk_event_item_status` (`event_id`, `item_id`),
    FOREIGN KEY (`event_id`) REFERENCES `events`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`item_id`) REFERENCES `preflight_checklist_items`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`completed_by_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;