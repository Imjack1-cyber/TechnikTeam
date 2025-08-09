-- Flyway migration V52: Create table for related storage items

CREATE TABLE `storage_item_relations` (
    `item_id` INT NOT NULL,
    `related_item_id` INT NOT NULL,
    PRIMARY KEY (`item_id`, `related_item_id`),
    FOREIGN KEY (`item_id`) REFERENCES `storage_items`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`related_item_id`) REFERENCES `storage_items`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;