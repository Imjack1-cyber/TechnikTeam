-- Flyway migration V102: Remove the unique constraint on the email column in the users table.

-- The name of the unique key constraint is 'email' as defined in V2__Add_keys_and_indexes.sql
ALTER TABLE `users` DROP INDEX `email`;