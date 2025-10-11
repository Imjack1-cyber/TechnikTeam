-- Flyway migration V131: Ensure the 'uuid' column exists in the 'polls' table.
-- This script is idempotent and safely adds the column and populates it if it was missed
-- during a previous failed migration, resolving schema validation errors.

-- Step 1: Add the column if it doesn't exist. It's added as NULLABLE first to handle existing rows.
SET @s = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE table_schema = DATABASE()
        AND table_name = 'polls'
        AND column_name = 'uuid'
    ) > 0,
    "SELECT 'Column uuid already exists in polls table.' AS ' ';",
    "ALTER TABLE `polls` ADD COLUMN `uuid` VARCHAR(36) NULL UNIQUE AFTER `id`;"
));

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Step 2: Populate UUIDs for any existing polls that might have a NULL value
UPDATE `polls` SET `uuid` = UUID() WHERE `uuid` IS NULL;

-- Step 3: Now that all values are populated, make the column NOT NULL as originally intended.
-- It's done in a separate step to avoid errors on tables that already have data but are missing the column.
ALTER TABLE `polls` MODIFY COLUMN `uuid` VARCHAR(36) NOT NULL;

-- Step 4: Ensure the UNIQUE constraint is also present, in case the initial ADD COLUMN failed to set it.
SET @s = (SELECT IF(
    (SELECT COUNT(*)
        FROM INFORMATION_SCHEMA.STATISTICS
        WHERE table_schema = DATABASE()
        AND table_name = 'polls'
        AND index_name = 'uuid'
    ) > 0,
    "SELECT 'Unique index on uuid already exists.' AS ' ';",
    "ALTER TABLE `polls` ADD UNIQUE INDEX `uuid` (`uuid`);"
));

PREPARE stmt FROM @s;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;