-- Flyway migration V34: Add status tracking to chat messages for read receipts

ALTER TABLE `chat_messages`
ADD COLUMN `status` ENUM('SENT', 'DELIVERED', 'READ') NOT NULL DEFAULT 'SENT' AFTER `message_text`;

-- Add an index for performance on status updates

ALTER TABLE `chat_messages` ADD INDEX `idx_chat_messages_conversation_status` (`conversation_id`, `status`);