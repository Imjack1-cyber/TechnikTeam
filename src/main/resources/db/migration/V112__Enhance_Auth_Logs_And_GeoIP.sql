-- Flyway migration V112: Enhance authentication logs and add GeoIP table

-- Step 1: Add new columns to the authentication_logs table
ALTER TABLE `authentication_logs`
ADD COLUMN `user_agent` TEXT NULL DEFAULT NULL AFTER `ip_address`,
ADD COLUMN `device_type` VARCHAR(50) NULL DEFAULT NULL COMMENT 'e.g., Desktop, Mobile, Tablet' AFTER `user_agent`,
ADD COLUMN `country_code` VARCHAR(2) NULL DEFAULT NULL COMMENT 'ISO 3166-1 alpha-2 country code' AFTER `device_type`;

-- Step 2: Create a table to manage GeoIP filtering rules
CREATE TABLE `geoip_rules` (
  `country_code` VARCHAR(2) NOT NULL COMMENT 'ISO 3166-1 alpha-2 country code',
  `rule_type` ENUM('ALLOW', 'BLOCK') NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`country_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 3: Add a user-provided name to a known device/IP combination.
-- This enhances the existing known IPs table into a known devices table.
ALTER TABLE `user_known_ips`
ADD COLUMN `device_name` VARCHAR(100) NULL DEFAULT NULL AFTER `ip_address`;