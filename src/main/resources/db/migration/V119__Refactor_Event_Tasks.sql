-- Flyway migration V119: Refactor event tasks to support categories and advanced features

START TRANSACTION;

-- Step 1: Create the new event_task_categories table
CREATE TABLE `event_task_categories` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `event_id` INT NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `display_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_event_task_categories_event_id` (`event_id`),
  CONSTRAINT `fk_task_category_event` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: Modify the event_tasks table in separate, compatible statements
ALTER TABLE `event_tasks` ADD COLUMN `category_id` INT NULL AFTER `event_id`;
ALTER TABLE `event_tasks` CHANGE COLUMN `description` `name` VARCHAR(255) NOT NULL;
ALTER TABLE `event_tasks` CHANGE COLUMN `details` `description` TEXT DEFAULT NULL;
ALTER TABLE `event_tasks` MODIFY COLUMN `status` ENUM('LOCKED', 'OPEN', 'IN_PROGRESS', 'DONE') NOT NULL DEFAULT 'LOCKED';
ALTER TABLE `event_tasks` ADD COLUMN `is_important` BOOLEAN NOT NULL DEFAULT FALSE AFTER `required_persons`;
ALTER TABLE `event_tasks` ADD CONSTRAINT `fk_task_category` FOREIGN KEY (`category_id`) REFERENCES `event_task_categories` (`id`) ON DELETE SET NULL;

-- Step 3: Modify the event_task_assignments table, safely handling all foreign key dependencies
-- Drop FKs from other tables that reference event_task_assignments (none in the original schema, but good practice).
-- Drop FKs from event_task_assignments itself. The names are defined in V3__Add_foreign_keys.sql.
ALTER TABLE `event_task_assignments` DROP FOREIGN KEY `event_task_assignments_ibfk_1`;
ALTER TABLE `event_task_assignments` DROP FOREIGN KEY `event_task_assignments_ibfk_2`;

-- Now, modify the event_task_assignments table
ALTER TABLE `event_task_assignments` ADD COLUMN `status` ENUM('ACTIVE', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE' AFTER `user_id`;
ALTER TABLE `event_task_assignments` DROP PRIMARY KEY;
ALTER TABLE `event_task_assignments` ADD PRIMARY KEY (`task_id`, `user_id`);

-- Finally, re-add the foreign key constraints we dropped.
ALTER TABLE `event_task_assignments` ADD CONSTRAINT `event_task_assignments_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `event_tasks` (`id`) ON DELETE CASCADE;
ALTER TABLE `event_task_assignments` ADD CONSTRAINT `event_task_assignments_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;


COMMIT;