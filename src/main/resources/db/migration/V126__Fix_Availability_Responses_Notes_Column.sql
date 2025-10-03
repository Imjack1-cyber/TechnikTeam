-- Flyway migration V126: Idempotent fix for the 'notes' column in the availability_responses table.

-- This script checks if the 'notes' column exists before trying to add it.
-- This ensures the schema is correct, even if previous migrations failed or were inconsistent.
SET @s = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE table_schema = DATABASE()
        AND table_name = 'availability_responses'
        AND column_name = 'notes'
    ) > 0,
    "SELECT 'Column notes already exists in availability_responses.' AS ' ';",
    "ALTER TABLE `availability_responses` ADD COLUMN `notes` TEXT NULL DEFAULT NULL AFTER `status`;"
));

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Also ensure the column is of type TEXT NULL, as originally intended.
ALTER TABLE `availability_responses` MODIFY COLUMN `notes` TEXT NULL DEFAULT NULL;