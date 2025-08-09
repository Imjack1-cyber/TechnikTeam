-- Flyway migration V35: Add edit and delete tracking to direct messages

ALTER TABLE `chat_messages`
ADD COLUMN `edited` BOOLEAN NOT NULL DEFAULT FALSE AFTER `status`,
ADD COLUMN `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE AFTER `edited`,
ADD COLUMN `deleted_at` TIMESTAMP NULL DEFAULT NULL AFTER `is_deleted`,
ADD COLUMN `deleted_by_user_id` INT NULL AFTER `deleted_at`,
ADD CONSTRAINT `fk_chat_message_deleted_by` FOREIGN KEY (`deleted_by_user_id`) REFERENCES `users`(`id`) ON DELETE SET NULL;