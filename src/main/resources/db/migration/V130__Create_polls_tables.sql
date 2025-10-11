-- Flyway migration V130: Create tables for the general-purpose internal polls feature

-- Table to store the main poll information
CREATE TABLE IF NOT EXISTS `polls` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `uuid` VARCHAR(36) NOT NULL UNIQUE,
  `question` TEXT NOT NULL,
  `created_by_user_id` INT NOT NULL,
  `closes_at` TIMESTAMP NULL DEFAULT NULL,
  `is_closed` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `type` ENUM('MULTIPLE_CHOICE', 'WORD_CLOUD', 'AVAILABILITY', 'SCHEDULING') NOT NULL DEFAULT 'MULTIPLE_CHOICE',
  `description` TEXT NULL,
  `start_time` TIMESTAMP NULL DEFAULT NULL,
  `end_time` TIMESTAMP NULL DEFAULT NULL,
  `options` JSON NULL,
  `verification_code` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  KEY `fk_poll_creator` (`created_by_user_id`),
  CONSTRAINT `fk_poll_creator` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table to store the options for each poll
CREATE TABLE IF NOT EXISTS `poll_options` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `poll_id` INT NOT NULL,
  `option_text` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_poll_options_poll_id` (`poll_id`),
  CONSTRAINT `fk_poll_option_poll` FOREIGN KEY (`poll_id`) REFERENCES `polls` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table to store user votes (rebuilt structure)
DROP TABLE IF EXISTS `poll_votes`;
CREATE TABLE `poll_votes` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `poll_id` INT NOT NULL,
    `user_id` INT NULL,
    `guest_name` VARCHAR(255) NULL DEFAULT NULL,
    `poll_option_id` INT NULL,
    `notes` TEXT NULL DEFAULT NULL,
    `voted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_poll_user_vote` (`poll_id`, `user_id`),
    UNIQUE KEY `uk_poll_guest_vote` (`poll_id`, `guest_name`),
    CONSTRAINT `fk_poll_vote_poll_ref` FOREIGN KEY (`poll_id`) REFERENCES `polls`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_poll_vote_option_ref` FOREIGN KEY (`poll_option_id`) REFERENCES `poll_options`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_poll_vote_user_ref` FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_vote_user_or_guest` CHECK (`user_id` IS NOT NULL OR `guest_name` IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Add a new permission for managing polls
INSERT INTO `permissions` (`permission_key`, `description`)
SELECT 'POLL_MANAGE', 'Kann allgemeine Umfragen für das Team erstellen und verwalten.'
WHERE NOT EXISTS (SELECT 1 FROM `permissions` WHERE `permission_key` = 'POLL_MANAGE');

-- Grant this new permission to ALL existing admins.
INSERT INTO user_permissions (user_id, permission_id)
SELECT u.id, (SELECT p.id FROM permissions p WHERE p.permission_key = 'POLL_MANAGE')
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.role_name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_permissions up_check
    JOIN permissions p_check ON up_check.permission_id = p_check.id
    WHERE up_check.user_id = u.id AND p_check.permission_key = 'POLL_MANAGE'
);