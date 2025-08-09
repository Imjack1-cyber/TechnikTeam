-- Flyway migration V86: Add detailed layout preferences to users table

-- We will reuse the existing dashboard_layout JSON column and expand its purpose.
-- No schema change is needed if the column already exists and is of a JSON or TEXT type.
-- This migration serves as a marker for the application logic change.
-- The default value will be handled by the application logic if the column is NULL.

ALTER TABLE `users` MODIFY `dashboard_layout` JSON NULL;