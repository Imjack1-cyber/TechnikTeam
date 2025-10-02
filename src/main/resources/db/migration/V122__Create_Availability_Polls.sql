-- Flyway migration V122: Create tables for the availability check feature

CREATE TABLE `availability_polls` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `start_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by_user_id` INT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_availability_polls_start_end` (`start_time`, `end_time`),
  CONSTRAINT `fk_availability_poll_creator` FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `availability_responses` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `poll_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `status` ENUM('AVAILABLE', 'UNAVAILABLE', 'MAYBE') NOT NULL,
  `notes` TEXT NULL,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_availability_response_poll_user` (`poll_id`, `user_id`),
  CONSTRAINT `fk_availability_response_poll` FOREIGN KEY (`poll_id`) REFERENCES `availability_polls` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_availability_response_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add a new permission for managing availability polls
INSERT INTO `permissions` (`permission_key`, `description`)
VALUES ('AVAILABILITY_MANAGE', 'Kann Verfügbarkeitsabfragen erstellen und die Ergebnisse einsehen.');