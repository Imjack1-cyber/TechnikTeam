ALTER TABLE preflight_checklist_items
ADD COLUMN storage_item_id INT NULL DEFAULT NULL AFTER item_text,
ADD CONSTRAINT fk_checklist_item_storage_item
    FOREIGN KEY (storage_item_id) REFERENCES storage_items(id)
    ON DELETE SET NULL;

ALTER TABLE preflight_checklist_items
MODIFY COLUMN item_text VARCHAR(255) NULL;