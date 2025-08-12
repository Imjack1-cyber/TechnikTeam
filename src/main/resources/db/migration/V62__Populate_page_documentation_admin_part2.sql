-- Flyway migration V62, Part 2: Populate admin-facing page documentation

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`, `wiki_entry_id`) VALUES
('admin_events', 'Admin Event-Verwaltung', '/admin/veranstaltungen',
'## Features
- **Event-Übersicht:** Listet alle Events mit Datum und Status auf.
- **Event erstellen:** Öffnet einen detaillierten Dialog zur Erstellung eines neuen Events, inklusive Definition von Personalbedarf und Materialreservierungen.
- **Event bearbeiten:** Ermöglicht die Änderung aller Aspekte eines bestehenden Events.
- **Event klonen:** Erstellt eine Kopie eines Events als Vorlage für eine wiederkehrende Veranstaltung.
- **Event löschen:** Entfernt ein Event und alle zugehörigen Daten permanent.
- **Debriefing:** Link zur Nachbereitungsseite für abgeschlossene Events.

## Use Cases
- Plane und organisiere alle Veranstaltungen der Schule.
- Verwalte den gesamten Lebenszyklus eines Events von der Planung bis zur Nachbereitung.
- Erstelle schnell neue Events auf Basis alter Vorlagen.',
'["admin_debriefings_list", "admin_event_roles", "admin_venues"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminEventResource.java')),

('admin_debriefing_details', 'Admin Event-Debriefing', '/admin/veranstaltungen/:eventId/debriefing',
'## Features
- **Strukturierte Erfassung:** Formular zur Eingabe von Feedback in den Kategorien "Was lief gut?", "Was kann verbessert werden?" und "Anmerkungen zum Material".
- **Hervorhebung von Mitgliedern:** Ermöglicht die Auswahl von Team-Mitgliedern, die sich besonders ausgezeichnet haben.
- **Ansichts- & Bearbeitungsmodus:** Zeigt gespeicherte Debriefings an und erlaubt autorisierten Benutzern (Admins, Event-Leiter) die Bearbeitung.

## Use Cases
- Sammle strukturiertes Feedback nach einem Event, um zukünftige Veranstaltungen zu verbessern.
- Erkenne und würdige herausragende Leistungen von Team-Mitgliedern.
- Dokumentiere Probleme mit dem Equipment, die während des Events aufgetreten sind.',
'["admin_events", "admin_debriefings_list"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminEventDebriefingResource.java')),

('admin_debriefings_list', 'Admin Debriefing-Übersicht', '/admin/debriefings',
'## Features
- **Zentrale Übersicht:** Zeigt alle eingereichten Debriefings für alle vergangenen Veranstaltungen.
- **Schnellzugriff:** Ermöglicht den direkten Sprung zur Detailansicht eines Debriefings.

## Use Cases
- Analysiere das Feedback mehrerer Veranstaltungen, um wiederkehrende Muster oder Probleme zu erkennen.
- Nutze die gesammelten Erkenntnisse zur langfristigen Verbesserung der Team-Prozesse.',
'["admin_events", "admin_debriefing_details"]',
1, NULL),

('admin_event_roles', 'Admin Event-Rollen', '/admin/event-roles',
'## Features
- **Rollen-Verwaltung:** Erstelle, bearbeite und lösche wiederverwendbare Rollen (z.B. "Tontechnik", "Lichttechnik").
- **Icon-Zuweisung:** Weise jeder Rolle ein passendes FontAwesome-Icon für eine bessere visuelle Darstellung zu.

## Use Cases
- Definiere einen Standardkatalog von Verantwortlichkeiten für Events.
- Strukturiere die Team-Zuweisung bei der Event-Planung.',
'["admin_events"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminEventRoleResource.java')),

('admin_venues', 'Admin Veranstaltungsorte', '/admin/venues',
'## Features
- **Orte verwalten:** Erstelle, bearbeite und lösche häufig genutzte Veranstaltungsorte.
- **Zusatzinformationen:** Speichere Adressen, Notizen und Ansprechpartner für jeden Ort.
- **Raumplan-Upload:** Lade ein Bild (z.B. einen Grundriss oder Lageplan) für jeden Ort hoch.

## Use Cases
- Erstelle eine zentrale Datenbank aller relevanten Veranstaltungsorte.
- Vereinfache die Event-Planung durch schnellen Zugriff auf Ortsdetails und Pläne.',
'["admin_events"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminVenueResource.java')),

('admin_courses', 'Admin Lehrgangs-Vorlagen', '/admin/lehrgaenge',
'## Features
- **Vorlagen-Verwaltung:** Erstelle, bearbeite und lösche Lehrgangs-Vorlagen (z.B. "Grundkurs Licht").
- **Detail-Management:** Klicke auf "Meetings", um die spezifischen Termine für eine Vorlage zu verwalten.

## Use Cases
- Strukturiere das Ausbildungsangebot des Technik-Teams.
- Definiere die Qualifikationen, die für Events benötigt werden können.',
'["admin_meetings", "admin_matrix"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/CourseResource.java')),

('admin_meetings', 'Admin Meetings', '/admin/lehrgaenge/:courseId/meetings',
'## Features
- **Terminplanung:** Plane, bearbeite und lösche spezifische Termine (Meetings) für einen Lehrgang.
- **Detail-Zuweisung:** Lege für jeden Termin einen Ort, eine Zeit und einen Leiter fest.
- **Klonen:** Erstelle schnell Kopien von Terminen für wiederkehrende Schulungen.

## Use Cases
- Organisiere die konkreten Schulungstermine für das Team.
- Verwalte den Kalender der Ausbildungsveranstaltungen.',
'["admin_courses"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/MeetingResource.java')),

('admin_storage', 'Admin Lagerverwaltung', '/admin/lager',
'## Features
- **Artikel-Verwaltung:** Erstelle, bearbeite und lösche alle Artikel im Inventar.
- **Detail-Management:** Definiere alle Eigenschaften eines Artikels, inklusive Name, Ort, Kategorie, Mengen und Preis.
- **Defekt-Management:** Markiere Artikel als defekt oder repariert.
- **Beziehungs-Management:** Lege fest, welche Artikel oft zusammen verwendet werden (Zubehör).
- **QR-Code-Ansicht:** Zeige einen QR-Code für schnelle Aktionen an.

## Use Cases
- Pflege den kompletten Inventarstamm der Technik-AG.
- Halte die Bestandszahlen und den Zustand des Equipments aktuell.',
'["admin_defective_items", "storage_details"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/StorageResource.java')),

('admin_files', 'Admin Dateien', '/admin/dateien',
'## Features
- **Datei-Upload:** Lade neue Dateien hoch und weise sie einer Kategorie und Sichtbarkeitsstufe (Alle/Admin) zu.
- **Kategorie-Verwaltung:** Erstelle und lösche Dateikategorien.
- **Löschen:** Entferne nicht mehr benötigte Dateien.

## Use Cases
- Verwalte den zentralen Dokumentenpool für das Team.
- Stelle sicher, dass sensible Dokumente nur für Admins sichtbar sind.',
'["files"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminFileResource.java')),

('admin_kits', 'Admin Kit-Verwaltung', '/admin/kits',
'## Features
- **Kit erstellen:** Definiere neue Kits (Koffer, Material-Sets) mit Namen und Beschreibung.
- **Inhalt zusammenstellen:** Weise jedem Kit eine Liste von Lagerartikeln mit den jeweiligen Mengen zu.
- **QR-Code:** Generiere einen QR-Code, der direkt zu einer druckbaren Packliste für das Kit führt.

## Use Cases
- Standardisiere Materialzusammenstellungen für wiederkehrende Aufgaben.
- Vereinfache das Packen für Events durch vordefinierte Listen.',
'["pack_kit", "admin_storage"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/KitResource.java')),

('admin_feedback', 'Admin Feedback-Board', '/admin/feedback',
'## Features
- **Kanban-Board:** Zeigt alle allgemeinen Feedback-Einreichungen in einem visuellen Board mit den Spalten Neu, Gesehen, Geplant, Erledigt und Abgelehnt.
- **Drag-and-Drop:** Verschiebe Feedback-Karten zwischen den Spalten, um ihren Status zu aktualisieren.
- **Detailansicht:** Klicke auf eine Karte, um den vollständigen Inhalt und den Einsender zu sehen.

## Use Cases
- Verarbeite und organisiere Feature-Wünsche und Bug-Reports von Benutzern.
- Behalte den Überblick über den Bearbeitungsstatus von Feedback.',
'["feedback"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminFeedbackResource.java')),

('admin_notifications', 'Admin Benachrichtigungen', '/admin/benachrichtigungen',
'## Features
- **Benachrichtigung erstellen:** Verfasse Nachrichten mit Titel, Beschreibung und Dringlichkeitsstufe.
- **Zielgruppen-Auswahl:** Sende Benachrichtigungen an alle Benutzer, nur an Teilnehmer eines bestimmten Events oder nur an Teilnehmer eines Meetings.
- **Notfall-Warnungen:** Versende "Warning"-Benachrichtigungen, die beim Empfänger einen Alarmton und einen blinkenden Bildschirm auslösen.

## Use Cases
- Informiere das Team schnell über wichtige, zeitkritische Ereignisse.
- Sende gezielte Erinnerungen an die Teilnehmer eines bevorstehenden Events.
- Alarmiere das Team in einem echten Notfall.',
'[]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/AdminNotificationResource.java'));