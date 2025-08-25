-- Flyway migration V111: Add columns and tables for TOTP 2FA and known IPs

-- Add columns to users table for TOTP
ALTER TABLE `users`
ADD COLUMN `is_totp_enabled` BOOLEAN NOT NULL DEFAULT FALSE AFTER `fcm_token`,
ADD COLUMN `totp_secret` VARCHAR(255) NULL DEFAULT NULL AFTER `is_totp_enabled`;

-- Create table to store known IP addresses for users to bypass 2FA
CREATE TABLE `user_known_ips` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `ip_address` VARCHAR(45) NOT NULL,
    `last_seen` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_ip` (`user_id`, `ip_address`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create table to store one-time backup codes
CREATE TABLE `user_backup_codes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `code_hash` VARCHAR(255) NOT NULL,
    `is_used` BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
    INDEX `idx_user_backup_codes_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;