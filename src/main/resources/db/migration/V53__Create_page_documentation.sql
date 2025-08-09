-- Flyway migration V53: Create table for page documentation

CREATE TABLE `page_documentation` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `page_key` VARCHAR(100) NOT NULL UNIQUE COMMENT 'e.g., "dashboard", "admin_users"',
    `title` VARCHAR(255) NOT NULL,
    `page_path` VARCHAR(255) NOT NULL COMMENT 'The frontend route, e.g., "/home"',
    `features` TEXT NOT NULL COMMENT 'Markdown-enabled description of features',
    `related_pages` TEXT NULL COMMENT 'JSON array of related page keys',
    `admin_only` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add a new permission for managing this documentation
INSERT INTO `permissions` (`permission_key`, `description`)
VALUES ('DOCUMENTATION_MANAGE', 'Kann die Hilfs- und Dokumentationsseiten der Anwendung bearbeiten.');