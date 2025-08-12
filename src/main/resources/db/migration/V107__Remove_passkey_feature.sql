-- Flyway migration V107: Completely remove the user_passkeys table and all related data.

DROP TABLE IF EXISTS `user_passkeys`;