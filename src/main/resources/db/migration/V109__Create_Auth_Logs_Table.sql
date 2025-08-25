-- Flyway migration V109: Create table for authentication logs

CREATE TABLE `authentication_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` INT NULL DEFAULT NULL COMMENT 'NULL if login fails for a non-existent user',
  `username` VARCHAR(255) NOT NULL COMMENT 'The username provided during the attempt',
  `ip_address` VARCHAR(45) NULL,
  `event_type` ENUM('LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT') NOT NULL,
  `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_auth_log_user_id` (`user_id`),
  INDEX `idx_auth_log_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;