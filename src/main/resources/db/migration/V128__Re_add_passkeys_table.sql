-- Flyway migration V128: Re-implement the user_passkeys table for WebAuthn support, ensuring it exists.

CREATE TABLE IF NOT EXISTS `user_passkeys` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `device_name` varchar(255) NOT NULL,
  `credential_id` varbinary(255) NOT NULL,
  `public_key` blob NOT NULL,
  `signature_count` BIGINT NOT NULL,
  `user_handle` varbinary(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_passkey_credential_id` (`credential_id`(255)),
  KEY `fk_passkey_user` (`user_id`),
  CONSTRAINT `fk_passkey_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;