-- Flyway migration V24: Remove the user_passkeys table and all related data.

DROP TABLE IF EXISTS `user_passkeys`;