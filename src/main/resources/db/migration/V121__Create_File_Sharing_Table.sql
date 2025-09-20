-- Flyway migration V121: Add table for secure file sharing links

CREATE TABLE `file_sharing_links` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `file_id` INT NOT NULL,
  `token` VARCHAR(64) NOT NULL,
  `access_level` ENUM('PUBLIC', 'LOGGED_IN', 'ADMIN') NOT NULL,
  `expires_at` TIMESTAMP NULL DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_sharing_token` (`token`),
  KEY `idx_file_sharing_file_id` (`file_id`),
  CONSTRAINT `fk_file_sharing_file` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;