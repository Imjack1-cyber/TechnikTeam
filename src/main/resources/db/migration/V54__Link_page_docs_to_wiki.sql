-- Flyway migration V54: Add a foreign key to link page documentation to the technical wiki

ALTER TABLE `page_documentation`
ADD COLUMN `wiki_entry_id` INT NULL DEFAULT NULL AFTER `admin_only`,
ADD CONSTRAINT `fk_pagedoc_wiki` FOREIGN KEY (`wiki_entry_id`) REFERENCES `wiki_documentation`(`id`) ON DELETE SET NULL;