-- Adds suspension-related columns to the `users` table.
-- Using a status column is efficient for querying suspended users.
-- Using a nullable TIMESTAMP for `suspended_until` allows for both temporary and indefinite suspensions.
ALTER TABLE users
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER role_id,
ADD COLUMN suspended_until TIMESTAMP NULL DEFAULT NULL AFTER status,
ADD COLUMN suspended_reason TEXT NULL DEFAULT NULL AFTER suspended_until;

-- Add an index for potentially querying expired suspensions.
CREATE INDEX idx_users_suspended_until ON users(suspended_until);