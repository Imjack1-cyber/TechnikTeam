-- Flyway migration V110: Add JTI and expiry columns for session tracking and create the blocklist table

-- Add JTI and expiry columns to the authentication log to track sessions
ALTER TABLE `authentication_logs`
ADD COLUMN `jti` VARCHAR(255) NULL DEFAULT NULL AFTER `event_type`,
ADD COLUMN `token_expiry` TIMESTAMP NULL DEFAULT NULL AFTER `jti`;

-- Create table for JWT blocklist to handle manual logouts
CREATE TABLE `jwt_blocklist` (
  `jti` VARCHAR(255) NOT NULL COMMENT 'The unique JWT ID',
  `expiry` TIMESTAMP NOT NULL COMMENT 'The original expiry timestamp of the token',
  PRIMARY KEY (`jti`),
  INDEX `idx_jwt_blocklist_expiry` (`expiry`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;