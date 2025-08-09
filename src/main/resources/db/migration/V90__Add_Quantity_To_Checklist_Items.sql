ALTER TABLE preflight_checklist_items
ADD COLUMN quantity INT NULL DEFAULT NULL AFTER storage_item_id;