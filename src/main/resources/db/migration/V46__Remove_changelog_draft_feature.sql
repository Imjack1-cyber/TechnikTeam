-- Flyway migration V46: Simplify changelogs by removing the draft/published feature

ALTER TABLE `changelogs`
DROP COLUMN `is_published`;