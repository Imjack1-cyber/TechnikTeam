-- Flyway migration V70: Add category to page_documentation for link tree grouping

ALTER TABLE `page_documentation`
ADD COLUMN `category` VARCHAR(100) NULL DEFAULT 'Sonstiges' AFTER `wiki_entry_id`;

-- Let's categorize the existing pages
UPDATE `page_documentation` SET `category` = 'Allgemein' WHERE `page_key` IN ('dashboard', 'team_directory', 'chat', 'calendar', 'feedback', 'changelogs', 'profile', 'password_change', 'files', 'search_results');
UPDATE `page_documentation` SET `category` = 'Events & Lehrgänge' WHERE `page_key` IN ('lehrgaenge', 'events', 'event_details', 'meeting_details', 'event_feedback_form');
UPDATE `page_documentation` SET `category` = 'Lager & Material' WHERE `page_key` IN ('storage', 'storage_details', 'pack_kit', 'qr_action');
UPDATE `page_documentation` SET `category` = 'System & Hilfe' WHERE `page_key` IN ('help_list', 'help_details', 'login', 'forbidden', 'not_found', 'error_page');

UPDATE `page_documentation` SET `category` = 'Admin: Dashboard & Berichte' WHERE `page_key` IN ('admin_dashboard', 'admin_reports', 'admin_log', 'admin_system');
UPDATE `page_documentation` SET `category` = 'Admin: Benutzer & Anträge' WHERE `page_key` IN ('admin_users', 'admin_requests', 'admin_training_requests');
UPDATE `page_documentation` SET `category` = 'Admin: Events & Lehrgänge' WHERE `page_key` IN ('admin_events', 'admin_debriefing_details', 'admin_debriefings_list', 'admin_event_roles', 'admin_venues', 'admin_courses', 'admin_meetings', 'admin_matrix', 'admin_checklist_templates');
UPDATE `page_documentation` SET `category` = 'Admin: Lager & Material' WHERE `page_key` IN ('admin_storage', 'admin_defective_items', 'admin_damage_reports', 'admin_kits');
UPDATE `page_documentation` SET `category` = 'Admin: Inhalte & Kommunikation' WHERE `page_key` IN ('admin_announcements', 'admin_feedback', 'admin_notifications', 'admin_files', 'admin_changelogs', 'admin_wiki', 'admin_documentation');
UPDATE `page_documentation` SET `category` = 'Admin: Gamification' WHERE `page_key` IN ('admin_achievements');