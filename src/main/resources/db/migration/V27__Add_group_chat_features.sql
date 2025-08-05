-- Flyway migration V28: Enhance chat tables for group functionality and read receipts

-- Add columns to conversations table for group details
ALTER TABLE `chat_conversations`
    ADD COLUMN `is_group_chat` BOOLEAN NOT NULL DEFAULT FALSE AFTER `id`,
    ADD COLUMN `name` VARCHAR(100) DEFAULT NULL AFTER `is_group_chat`,
    ADD COLUMN `creator_id` INT DEFAULT NULL AFTER `name`,
    ADD CONSTRAINT `fk_chat_creator` FOREIGN KEY (`creator_id`) REFERENCES `users`(`id`) ON DELETE SET NULL;

-- Create a table to track read status of messages by user
CREATE TABLE `chat_message_read_status` (
    `message_id` BIGINT NOT NULL,
    `user_id` INT NOT NULL,
    `read_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`message_id`, `user_id`),
    FOREIGN KEY (`message_id`) REFERENCES `chat_messages`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;