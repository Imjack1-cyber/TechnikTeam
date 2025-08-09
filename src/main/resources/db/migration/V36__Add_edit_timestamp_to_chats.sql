-- Flyway migration V36: Add edited_at timestamp to chat messages

ALTER TABLE `event_chat_messages`
ADD COLUMN `edited_at` TIMESTAMP NULL DEFAULT NULL AFTER `edited`;

ALTER TABLE `chat_messages`
ADD COLUMN `edited_at` TIMESTAMP NULL DEFAULT NULL AFTER `edited`;