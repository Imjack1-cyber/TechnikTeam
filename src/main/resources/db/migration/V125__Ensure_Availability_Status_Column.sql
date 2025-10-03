-- Flyway migration V125: Ensure the 'status' column in availability_responses is correct.
-- This script is idempotent and safely adds/modifies the column as needed,
-- taking over the logic from the problematic V124 migration.

-- Add the column if it doesn't exist
SET @s = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE table_schema = DATABASE()
        AND table_name = 'availability_responses'
        AND column_name = 'status'
    ) > 0,
    "SELECT 'Column status already exists in availability_responses.' AS ' ';",
    "ALTER TABLE `availability_responses` ADD COLUMN `status` ENUM('AVAILABLE', 'UNAVAILABLE', 'MAYBE') NULL DEFAULT NULL AFTER `guest_name`;"
));

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Ensure the column is nullable with the correct type, as intended by V123.
-- This corrects any state where the column might exist but with the wrong definition.
ALTER TABLE `availability_responses` MODIFY COLUMN `status` ENUM('AVAILABLE', 'UNAVAILABLE', 'MAYBE') NULL DEFAULT NULL;