-- Flyway migration V104: Add capacity and deadline features to meetings

ALTER TABLE `meetings`
ADD COLUMN `max_participants` INT NULL DEFAULT NULL AFTER `location`,
ADD COLUMN `signup_deadline` TIMESTAMP NULL DEFAULT NULL AFTER `max_participants`;