-- Flyway migration V43: Add announcement flag to event chat messages

ALTER TABLE `event_chat_messages`
ADD COLUMN `is_announcement` BOOLEAN NOT NULL DEFAULT FALSE AFTER `message_text`;