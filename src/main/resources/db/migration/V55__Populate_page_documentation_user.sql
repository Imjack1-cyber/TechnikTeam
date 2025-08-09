-- Flyway migration V55: Populate user-facing page documentation

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`, `wiki_entry_id`) VALUES
('dashboard', 'Dashboard', '/home',
'## Features
- **Meine nächsten Einsätze:** Zeigt eine Liste der kommenden Veranstaltungen, für die du fest eingeteilt bist.
- **Meine offenen Aufgaben:** Listet alle dir zugewiesenen Aufgaben aus laufenden Events auf.
- **Für Dich empfohlen:** Schlägt dir Events vor, für die du qualifiziert bist, aber noch nicht angemeldet bist.
- **Weitere anstehende Veranstaltungen:** Eine allgemeine Liste kommender Events.

## Use Cases
- Erhalte einen schnellen Überblick über deine anstehenden Verpflichtungen.
- Behalte den Überblick über deine Aufgaben während eines Events.
- Finde neue Möglichkeiten, dich im Team zu engagieren.',
'["events", "lehrgaenge", "profile"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/service/AdminDashboardService.java')),

('team_directory', 'Team-Verzeichnis', '/team',
'## Features
- **Mitgliederliste:** Zeigt eine Liste aller aktuellen Mitglieder des Technik-Teams.
- **Suche:** Ermöglicht die schnelle Suche nach einem bestimmten Mitglied.
- **Crew-Karte:** Per Klick auf ein Mitglied kann dessen "Crew-Karte" mit Qualifikationen und Erfolgen eingesehen werden.

## Use Cases
- Lerne neue Mitglieder kennen.
- Finde schnell heraus, wer im Team welche Fähigkeiten besitzt.
- Event-Leiter können sich einen schnellen Überblick über die Kompetenzen ihrer Teammitglieder verschaffen.',
'["profile"]',
0, NULL),

('chat', 'Chat', '/chat',
'## Features
- **1-zu-1-Gespräche:** Führe private Chats mit anderen Team-Mitgliedern.
- **Gruppenchats:** Erstelle oder nimm an Gruppenchats für spezifische Themen oder Events teil.
- **Echtzeit-Kommunikation:** Nachrichten werden in Echtzeit zugestellt.
- **Lesebestätigungen:** Sieh, ob deine Nachricht zugestellt und gelesen wurde.
- **Nachrichten bearbeiten & löschen:** Korrigiere Tippfehler oder entferne Nachrichten.

## Use Cases
- Schnelle Absprachen mit anderen Mitgliedern.
- Koordination in kleineren Gruppen für spezifische Projekte.
- Informeller Austausch im Team.',
'[]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/websocket/ChatWebSocketHandler.java')),

('lehrgaenge', 'Lehrgänge', '/lehrgaenge',
'## Features
- **Übersicht:** Listet alle anstehenden Lehrgänge, Kurse und Meetings auf.
- **An- und Abmeldung:** Melde dich mit einem Klick für einen Termin an oder ab.
- **Statusanzeige:** Sieh auf einen Blick, für welche Termine du angemeldet bist.
- **Lehrgang anfragen:** Schlage ein Thema für einen neuen Lehrgang vor, wenn du etwas Bestimmtes lernen möchtest.

## Use Cases
- Erwerbe neue Qualifikationen, um an mehr Events teilnehmen zu können.
- Behalte den Überblick über alle Fortbildungsmöglichkeiten.
- Gib den Admins Feedback, welche Themen für das Team interessant sind.',
'["dashboard", "profile", "calendar"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/dao/MeetingDAO.java')),

('events', 'Veranstaltungen', '/veranstaltungen',
'## Features
- **Event-Übersicht:** Zeigt alle kommenden Veranstaltungen an.
- **Qualifikations-Check:** Zeigt dir an, ob du die nötigen Qualifikationen für ein Event besitzt.
- **An- und Abmeldung:** Melde dich für Events an, für die du qualifiziert bist.
- **Status-Tracking:** Verfolge deinen aktuellen Status für jedes Event (z.B. Angemeldet, Zugewiesen).

## Use Cases
- Finde heraus, bei welchen Events du mitarbeiten kannst.
- Melde deine Verfügbarkeit für anstehende Veranstaltungen.',
'["dashboard", "profile", "calendar"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/dao/EventDAO.java')),

('storage', 'Lager', '/lager',
'## Features
- **Inventarübersicht:** Listet das gesamte verfügbare Equipment auf, gruppiert nach Lagerort.
- **Live-Suche & Filter:** Finde schnell Artikel über die Suche oder filtere nach Kategorie und Verfügbarkeitsstatus.
- **Warenkorb-System:** Füge mehrere Artikel zu einem Warenkorb hinzu, um sie gesammelt zu entnehmen oder einzuräumen.
- **Detailansicht:** Klicke auf einen Artikel, um dessen Details, Verlauf und zukünftige Reservierungen zu sehen.

## Use Cases
- Überprüfe die Verfügbarkeit von Equipment für ein Event oder ein persönliches Projekt.
- Bereite eine Materialliste vor und buche alles mit einer einzigen Transaktion aus.
- Finde schnell heraus, wo ein bestimmter Artikel gelagert wird.',
'["storage_details"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/dao/StorageDAO.java')),

('storage_details', 'Lagerartikel Details', '/lager/details/:itemId',
'## Features
- **Detailinformationen:** Zeigt alle Daten zu einem Artikel, inklusive Gesamtmenge, verfügbarer Menge und Defektstatus.
- **Verlauf (Transaktionen):** Eine lückenlose Historie, wer den Artikel wann entnommen oder zurückgebracht hat.
- **Wartungshistorie:** Ein Protokoll aller Reparaturen und Wartungsarbeiten.
- **Verfügbarkeitskalender:** Ein Kalender, der anzeigt, an welchen zukünftigen Tagen der Artikel für Events reserviert ist.
- **Zubehör/Verknüpfte Artikel:** Zeigt eine Liste von Artikeln, die oft zusammen mit diesem Gerät benötigt werden.
- **Schaden melden:** Ermöglicht es, einen Defekt direkt am Artikel zu melden.

## Use Cases
- Verfolge den Weg eines Artikels nach.
- Überprüfe, ob ein Artikel für ein zukünftiges Datum verfügbar ist.
- Stelle sicher, dass du alle notwendigen Zubehörteile für einen Artikel mitnimmst.',
'["storage"]',
0, NULL),

('files', 'Dateien', '/dateien',
'## Features
- **Zentraler Downloadbereich:** Bietet Zugriff auf wichtige Dokumente, Vorlagen und Anleitungen.
- **Kategorien:** Die Dateien sind zur besseren Übersicht in Kategorien gruppiert.

## Use Cases
- Lade die neueste Version einer Checkliste oder eines Protokollformulars herunter.
- Greife auf Bedienungsanleitungen für komplexes Equipment zu.',
'[]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/dao/FileDAO.java')),

('calendar', 'Kalender', '/kalender',
'## Features
- **Monatsübersicht:** Zeigt alle Events und Lehrgänge in einer klassischen Monatsansicht.
- **Listenansicht (Mobil):** Eine chronologische Liste aller Termine für eine schnelle Übersicht auf dem Handy.
- **Kalender-Abonnement:** Bietet einen iCal-Link, um den Technik-Team Kalender mit deinem persönlichen Kalender (z.B. auf dem Handy oder PC) zu synchronisieren.

## Use Cases
- Plane deine Termine und sieh auf einen Blick, was in den nächsten Wochen ansteht.
- Halte deinen persönlichen Kalender automatisch auf dem neuesten Stand.',
'["events", "lehrgaenge"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/api/v1/public_api/PublicCalendarResource.java')),

('feedback', 'Feedback', '/feedback',
'## Features
- **Feedback einreichen:** Gib allgemeines Feedback, melde Fehler oder schlage neue Features für die App vor.
- **Status-Übersicht:** Verfolge den Status deiner eigenen Einreichungen (z.B. Neu, Gesehen, Geplant).

## Use Cases
- Hilf mit, die Anwendung zu verbessern.
- Melde einen Bug, den du gefunden hast.
- Teile eine Idee für eine neue Funktion, die dem Team helfen würde.',
'["admin_feedback"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/dao/FeedbackSubmissionDAO.java')),

('changelogs', 'Changelogs', '/changelogs',
'## Features
- **Versionshistorie:** Zeigt eine chronologische Liste aller wichtigen Änderungen und neuer Features in der Anwendung.
- **Detailansicht:** Beschreibt, was in jeder neuen Version verbessert oder hinzugefügt wurde.

## Use Cases
- Bleibe auf dem Laufenden über die Entwicklung der App.
- Entdecke neue Funktionen, die dir die Arbeit erleichtern können.',
'[]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/dao/ChangelogDAO.java')),

('profile', 'Mein Profil', '/profil',
'## Features
- **Stammdaten:** Verwalte deine persönlichen Daten wie E-Mail und Klasse.
- **Sicherheit:** Ändere dein Passwort.
- **Qualifikationen:** Sieh eine Liste aller Lehrgänge, die du erfolgreich absolviert hast.
- **Abzeichen:** Zeigt alle Erfolge an, die du durch deine Teilnahme und dein Engagement freigeschaltet hast.
- **Event-Historie:** Eine komplette Übersicht aller Events, an denen du teilgenommen hast.

## Use Cases
- Halte deine Kontaktdaten aktuell.
- Verfolge deinen Lernfortschritt und deine Erfolge im Team.
- Gib Feedback für vergangene Events.',
'["dashboard", "team_directory"]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/technikteam/api/v1/public_api/PublicProfileResource.java'));