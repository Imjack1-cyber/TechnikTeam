-- Flyway migration V132: Add tables and columns for the Word Cloud poll feature

-- Step 1: Add a general-purpose 'options' JSON column to the polls table if it doesn't exist.
-- This will store poll-type-specific settings, like 'allowMultipleEntries' for word clouds.
-- The previous availability poll feature already added this, so this is just for safety/idempotency.
ALTER TABLE `polls` ADD COLUMN IF NOT EXISTS `options` JSON NULL;


-- Step 2: Create a new table specifically for word cloud entries.
CREATE TABLE IF NOT EXISTS `poll_word_cloud_entries` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `poll_id` INT NOT NULL,
  `user_id` INT NULL DEFAULT NULL,
  `guest_name` VARCHAR(255) NULL DEFAULT NULL,
  `word` VARCHAR(100) NOT NULL,
  `submitted_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_word_cloud_poll_word` (`poll_id`, `word`),
  CONSTRAINT `fk_word_cloud_poll` FOREIGN KEY (`poll_id`) REFERENCES `polls` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_word_cloud_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
