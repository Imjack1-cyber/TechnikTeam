-- Flyway migration V39: Create table for event roles and modify assignments

-- Step 1: Create the new event_roles table
CREATE TABLE `event_roles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` TEXT NULL,
    `icon_class` VARCHAR(50) DEFAULT 'fa-user-tag',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Add role_id column to the event_assignments table
ALTER TABLE `event_assignments`
ADD COLUMN `role_id` INT NULL AFTER `user_id`,
ADD CONSTRAINT `fk_assignment_role` FOREIGN KEY (`role_id`) REFERENCES `event_roles`(`id`) ON DELETE SET NULL;

-- Step 3: Ensure one user can't have multiple roles in the same event by modifying the unique constraint
-- This might drop the old key and add a new one
ALTER TABLE `event_assignments`
DROP INDEX `unique_assignment`,
ADD UNIQUE KEY `uk_event_user_assignment` (`event_id`, `user_id`);