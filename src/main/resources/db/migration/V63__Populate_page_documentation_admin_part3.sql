-- Flyway migration V63, Part 3: Populate admin-facing page documentation

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`, `wiki_entry_id`) VALUES
('admin_achievements', 'Admin Abzeichen', '/admin/achievements',
'## Features
- **Abzeichen verwalten:** Erstelle, bearbeite und lösche alle Abzeichen (Achievements), die Benutzer verdienen können.
- **System-Schlüssel:** Definiere einen einzigartigen `achievement_key`, über den das System ein Abzeichen programmatisch verleihen kann.

## Use Cases
- Erweitere das Gamification-System der Anwendung.
- Schaffe neue Anreize für Engagement und Teilnahme im Team.',
'["profile"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminAchievementResource.java')),

('admin_defective_items', 'Admin Defekte Artikel', '/admin/defekte',
'## Features
- **Übersicht:** Listet alle Lagerartikel auf, von denen mindestens ein Exemplar als defekt markiert ist.
- **Status-Management:** Ermöglicht die Bearbeitung des Defekt-Status eines Artikels.

## Use Cases
- Behalte den Überblick über alle reparaturbedürftigen Geräte.
- Koordiniere Reparaturen und Wartungsarbeiten.',
'["admin_storage", "admin_damage_reports"]',
1, NULL),

('admin_damage_reports', 'Admin Schadensmeldungen', '/admin/damage-reports',
'## Features
- **Übersicht:** Zeigt alle von Benutzern eingereichten, noch offenen Schadensmeldungen.
- **Bearbeitung:** Erlaubt Admins, eine Meldung zu bestätigen (und den Artikel als defekt zu markieren) oder abzulehnen.

## Use Cases
- Verarbeite schnell und effizient Meldungen von Benutzern über defektes Equipment.
- Stelle sicher, dass keine Schadensmeldung verloren geht.',
'["admin_defective_items", "storage_details"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminDamageReportResource.java')),

('admin_checklist_templates', 'Admin Checklisten-Vorlagen', '/admin/checklist-templates',
'## Features
- **Vorlagen erstellen:** Definiere wiederverwendbare Checklisten-Vorlagen für Standardaufgaben (z.B. "Bühnenaufbau Aula").
- **Items verwalten:** Füge jeder Vorlage beliebig viele Checklistenpunkte hinzu.

## Use Cases
- Standardisiere wiederkehrende Arbeitsabläufe bei Events.
- Erstelle "Pre-Flight-Checklisten", um sicherzustellen, dass vor einem Event nichts vergessen wird.',
'["admin_events"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminChecklistTemplateResource.java')),

('admin_matrix', 'Admin Qualifikations-Matrix', '/admin/matrix',
'## Features
- **Visuelle Übersicht:** Zeigt eine Matrix aller Benutzer und aller Lehrgangs-Termine.
- **Teilnahme-Management:** Ermöglicht es, per Klick auf eine Zelle die Teilnahme eines Benutzers an einem Meeting zu bestätigen oder zu widerrufen.
- **Anmerkungen:** Füge Anmerkungen zu einer Teilnahme hinzu (z.B. "entschuldigt gefehlt").

## Use Cases
- Verfolge den Ausbildungsfortschritt des gesamten Teams auf einen Blick.
- Pflege die Anwesenheitslisten für alle Lehrgänge an einem zentralen Ort.',
'["admin_courses"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/MatrixResource.java')),

('admin_reports', 'Admin Berichte', '/admin/berichte',
'## Features
- **Visualisierungen:** Zeigt Grafiken zum Event-Trend und den aktivsten Benutzern.
- **CSV-Export:** Bietet die Möglichkeit, detaillierte Berichte (z.B. zur Event-Teilnahme, Materialnutzung) als CSV-Datei herunterzuladen.
- **KPIs:** Zeigt wichtige Kennzahlen wie den Gesamtwert des Lagers.

## Use Cases
- Analysiere die Entwicklung und das Wachstum des Technik-Teams über die Zeit.
- Erstelle Auswertungen und Statistiken für Jahresberichte oder Präsentationen.',
'[]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/ReportResource.java')),

('admin_changelogs', 'Admin Changelogs', '/admin/changelogs',
'## Features
- **Changelog-Verwaltung:** Erstelle und bearbeite die "Was ist neu?"-Einträge, die Benutzern nach einem Update angezeigt werden.
- **Markdown-Editor:** Verfasse die Einträge mit Markdown für eine ansprechende Formatierung.

## Use Cases
- Informiere die Benutzer über neue Features und wichtige Änderungen in der Anwendung.',
'["changelogs"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminChangelogResource.java')),

('admin_log', 'Admin Aktions-Log', '/admin/log',
'## Features
- **Audit Trail:** Protokolliert alle wichtigen administrativen Aktionen, die im System durchgeführt werden.
- **Detailansicht:** Zeigt an, wer wann was getan hat.

## Use Cases
- Nachvollziehbarkeit und Sicherheit: Überprüfe, wer kritische Änderungen vorgenommen hat.
- Fehlersuche: Analysiere vergangene Aktionen, um Probleme zu verstehen.',
'[]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/LogResource.java')),

('admin_system', 'Admin System', '/admin/system',
'## Features
- **Live-Statistiken:** Zeigt die aktuelle CPU-Auslastung, RAM-Nutzung und Festplattenbelegung des Servers.
- **Laufzeit & Energie:** Informiert über die Server-Laufzeit und den Batteriestatus (falls vorhanden).

## Use Cases
- Überwache die Systemgesundheit des Anwendungsservers.
- Diagnostiziere Performance-Probleme.',
'[]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/SystemResource.java')),

('admin_wiki', 'Admin Wiki', '/admin/wiki',
'## Features
- **Technische Dokumentation:** Ein Ort zur Pflege der internen, technischen Dokumentation der Anwendung.
- **Hierarchische Struktur:** Organisiere die Dokumentation in einer Ordner- und Dateistruktur.
- **Markdown-Editor:** Erstelle und bearbeite die Wiki-Seiten mit Markdown.

## Use Cases
- Dokumentiere die Software-Architektur und wichtige Code-Teile.
- Schaffe eine Wissensdatenbank für Entwickler und Administratoren.',
'[]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/WikiResource.java')),

('admin_documentation', 'Admin Hilfeseiten', '/admin/documentation',
'## Features
- **Hilfeseiten verwalten:** Erstelle und bearbeite die Inhalte für die benutzerfreundlichen Hilfeseiten.
- **Verknüpfungen:** Verlinke Hilfeseiten untereinander und mit der technischen Wiki.
- **Sichtbarkeit:** Lege fest, ob eine Hilfeseite für alle Benutzer oder nur für Admins sichtbar ist.

## Use Cases
- Erstelle eine umfassende Anleitung für die Benutzer der Anwendung.
- Halte die Dokumentation aktuell, wenn neue Features hinzukommen.',
'["help_list", "help_details"]',
1, NULL);