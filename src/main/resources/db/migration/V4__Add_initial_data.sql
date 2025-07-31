-- Flyway migration V4: Add essential seed data.

-- Add default roles
INSERT INTO `roles` (`id`, `role_name`) VALUES (1, 'ADMIN'), (2, 'NUTZER');

-- Add all permissions
INSERT INTO `permissions` (`id`, `permission_key`, `description`) VALUES
(1, 'ACCESS_ADMIN_PANEL', 'Globaler Zugriff auf alle Admin-Funktionen. Überschreibt alle anderen Berechtigungen.'),
(2, 'USER_CREATE', 'Kann neue Benutzer anlegen.'),
(3, 'USER_READ', 'Kann Benutzerliste und -details einsehen.'),
(4, 'USER_UPDATE', 'Kann Benutzerprofile und deren Berechtigungen bearbeiten.'),
(5, 'USER_DELETE', 'Kann Benutzer löschen.'),
(6, 'USER_PASSWORD_RESET', 'Kann Passwörter anderer Benutzer zurücksetzen.'),
(7, 'EVENT_CREATE', 'Kann neue Events erstellen.'),
(8, 'EVENT_READ', 'Kann die Event-Verwaltungsliste einsehen.'),
(9, 'EVENT_UPDATE', 'Kann bestehende Events bearbeiten (Details, Personalbedarf, Material).'),
(10, 'EVENT_DELETE', 'Kann Events löschen.'),
(11, 'EVENT_MANAGE_ASSIGNMENTS', 'Kann Benutzer zu Events zuweisen.'),
(12, 'EVENT_MANAGE_TASKS', 'Kann Aufgaben innerhalb eines Events erstellen, bearbeiten und löschen.'),
(13, 'COURSE_CREATE', 'Kann neue Lehrgangs-Vorlagen erstellen.'),
(14, 'COURSE_READ', 'Kann Lehrgangs-Vorlagen und deren Termine einsehen.'),
(15, 'COURSE_UPDATE', 'Kann Lehrgangs-Vorlagen bearbeiten.'),
(16, 'COURSE_DELETE', 'Kann Lehrgangs-Vorlagen löschen.'),
(17, 'STORAGE_CREATE', 'Kann neue Lagerartikel anlegen.'),
(18, 'STORAGE_READ', 'Kann die Lager-Verwaltungsliste einsehen.'),
(19, 'STORAGE_UPDATE', 'Kann Lagerartikel bearbeiten, inkl. Defekt-Status.'),
(20, 'STORAGE_DELETE', 'Kann Lagerartikel löschen.'),
(21, 'KIT_CREATE', 'Kann neue Material-Kits/Koffer erstellen.'),
(22, 'KIT_READ', 'Kann die Kit-Verwaltungsliste einsehen.'),
(23, 'KIT_UPDATE', 'Kann Kits und deren Inhalte bearbeiten.'),
(24, 'KIT_DELETE', 'Kann Kits löschen.'),
(25, 'QUALIFICATION_READ', 'Kann die Qualifikations-Matrix einsehen.'),
(26, 'QUALIFICATION_UPDATE', 'Kann Teilnahme und Qualifikationen in der Matrix bearbeiten.'),
(27, 'FILE_CREATE', 'Kann Dateien hochladen und Kategorien erstellen.'),
(28, 'FILE_READ', 'Kann die Datei-Verwaltungsseite einsehen.'),
(29, 'FILE_DELETE', 'Kann Dateien und Kategorien löschen.'),
(30, 'LOG_READ', 'Kann das Admin-Aktionsprotokoll einsehen.'),
(31, 'REPORT_READ', 'Kann die Berichts- und Analyse-Seite einsehen.'),
(32, 'SYSTEM_READ', 'Kann die Systemstatus-Seite einsehen.'),
(33, 'ACHIEVEMENT_CREATE', 'Kann neue Erfolge und Abzeichen erstellen.'),
(34, 'ACHIEVEMENT_UPDATE', 'Kann bestehende Erfolge und Abzeichen bearbeiten.'),
(35, 'ACHIEVEMENT_DELETE', 'Kann Erfolge und Abzeichen löschen.'),
(36, 'FILE_MANAGE', 'Kann die Datei-Verwaltungsseite einsehen und grundlegende Operationen durchführen.'),
(37, 'FILE_UPDATE', 'Kann den Inhalt von Dateien (z.B. im Markdown-Editor) bearbeiten und neue Versionen hochladen.'),
(38, 'ACHIEVEMENT_VIEW', 'Ermöglicht das Anzeigen des Admin-Menüpunkts für Abzeichen.'),
(39, 'ADMIN_DASHBOARD_ACCESS', 'Ermöglicht das Anzeigen des Admin-Menüpunkts für das Dashboard.');

-- Create the default admin user with username 'admin' and password 'admin123'
-- The password hash is for 'admin123'
INSERT INTO `users` (`id`, `username`, `password_hash`, `role_id`, `theme`) VALUES
(1, 'admin', '$2a$10$odf1koglTR0zdzyPbDDkzOuw2XSKu19ylygl561RQ336KpABBFccu', 1, 'light');

-- Grant the master admin permission to the admin user
INSERT INTO `user_permissions` (`user_id`, `permission_id`) VALUES (1, 1);