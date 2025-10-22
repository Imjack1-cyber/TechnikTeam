-- Flyway migration V139: Create table for app-based identity verification requests

CREATE TABLE `identity_verification_requests` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `challenge_token` VARCHAR(64) NOT NULL,
  `request_type` ENUM('PASSWORD_RESET', 'MFA_LOGIN') NOT NULL,
  `status` ENUM('PENDING', 'APPROVED', 'DENIED', 'EXPIRED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
  `context` JSON NULL,
  `expires_at` TIMESTAMP NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_identity_verification_token` (`challenge_token`),
  KEY `idx_identity_verification_expires` (`expires_at`),
  CONSTRAINT `fk_identity_verification_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;