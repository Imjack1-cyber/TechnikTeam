-- Flyway migration V105: Refactor login_attempts for IP-based lockout to prevent DoS

-- Drop the old table which was username-based
DROP TABLE IF EXISTS `login_attempts`;

-- Create a new table that tracks failures by IP address and username
CREATE TABLE `login_attempts` (
  `ip_address` VARCHAR(45) NOT NULL,
  `username` VARCHAR(50) NOT NULL,
  `attempts` INT NOT NULL DEFAULT 1,
  `last_attempt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ip_address`, `username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;