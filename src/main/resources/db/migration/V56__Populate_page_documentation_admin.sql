-- Flyway migration V56: Populate admin-facing page documentation

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`, `wiki_entry_id`) VALUES
('admin_dashboard', 'Admin Dashboard', '/admin/dashboard',
'## Features
- **Anstehende Events:** Schneller Überblick über die nächsten geplanten Veranstaltungen.
- **Niedriger Lagerbestand:** Warnt vor Artikeln, deren verfügbare Menge einen kritischen Schwellenwert unterschreitet.
- **Letzte Aktionen:** Zeigt die neuesten Einträge aus dem Admin-Aktionsprotokoll.
- **Event-Trend:** Eine Grafik, die die Anzahl der Events über die letzten 12 Monate visualisiert.

## Use Cases
- Erhalte einen schnellen Überblick über den Zustand und die Aktivitäten des Systems.
- Identifiziere proaktiv potenzielle Materialengpässe.
- Überwache die letzten administrativen Änderungen.',
'["admin_events", "admin_storage", "admin_log"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/service/AdminDashboardService.java')),

('admin_announcements', 'Admin Anschlagbrett', '/admin/announcements',
'## Features
- **Erstellen & Bearbeiten:** Verfasse neue Mitteilungen mit einem Markdown-Editor.
- **Verwalten:** Bearbeite oder lösche bestehende Mitteilungen.
- **Übersicht:** Sieh alle aktuellen Mitteilungen und wer sie wann erstellt hat.

## Use Cases
- Informiere das gesamte Team über wichtige, aber nicht zeitkritische Themen (z.B. neue Lagerordnung, anstehende Team-Treffen).
- Veröffentliche Protokolle oder Zusammenfassungen.',
'["bulletin_board"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/service/AnnouncementService.java')),

('admin_users', 'Admin Benutzerverwaltung', '/admin/mitglieder',
'## Features
- **Benutzerliste:** Zeigt alle registrierten Benutzer mit ihrer zugewiesenen Rolle.
- **Benutzer erstellen:** Lege neue Benutzerkonten an, vergib ein initiales Passwort und weise eine Rolle zu.
- **Benutzer bearbeiten:** Ändere Benutzerdetails, Rollen und individuelle Berechtigungen.
- **Passwort zurücksetzen:** Generiere ein neues, zufälliges Passwort für einen Benutzer.
- **Benutzer löschen:** Entferne Benutzerkonten permanent.

## Use Cases
- Verwalte den Mitgliederstamm des Technik-Teams.
- Passe Berechtigungen für einzelne Benutzer an, um ihnen spezielle Zugriffsrechte zu geben.
- Hilf Benutzern, die ihr Passwort vergessen haben.',
'["admin_requests"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/service/UserService.java')),

('admin_requests', 'Admin Anträge', '/admin/requests',
'## Features
- **Übersicht:** Listet alle von Benutzern gestellten Anträge auf Profiländerungen.
- **Genehmigen:** Übernimm die beantragten Änderungen mit einem Klick in das Benutzerprofil.
- **Ablehnen:** Lehne einen Antrag ab.

## Use Cases
- Überprüfe und verarbeite von Benutzern gewünschte Änderungen ihrer Stammdaten.
- Sorge für die Datenqualität und -konsistenz der Benutzerprofile.',
'["admin_users"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/service/ProfileRequestService.java')),

('admin_training_requests', 'Admin Lehrgangsanfragen', '/admin/training-requests',
'## Features
- **Anfragenübersicht:** Zeigt alle von Benutzern eingereichten Themenwünsche für neue Lehrgänge.
- **Interessenten-Zähler:** Zählt, wie viele Benutzer ihr Interesse an einem Thema bekundet haben.
- **Verwaltung:** Lösche bearbeitete oder irrelevante Anfragen.

## Use Cases
- Erkenne, welche Fortbildungen im Team am meisten nachgefragt werden.
- Plane zukünftige Lehrgänge basierend auf dem Bedarf der Mitglieder.',
'["lehrgaenge"]',
1, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/service/TrainingRequestService.java'));