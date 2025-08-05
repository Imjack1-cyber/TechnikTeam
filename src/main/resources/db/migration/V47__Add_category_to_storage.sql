-- Flyway migration V47: Add category field to storage items for better filtering

ALTER TABLE `storage_items`
ADD COLUMN `category` VARCHAR(100) NULL DEFAULT NULL AFTER `compartment`;

-- Add an index for faster filtering by category
ALTER TABLE `storage_items`
ADD INDEX `idx_storage_item_category` (`category`);