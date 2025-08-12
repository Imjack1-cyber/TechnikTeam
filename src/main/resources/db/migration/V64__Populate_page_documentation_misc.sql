-- Flyway migration V64: Populate miscellaneous and special page documentation

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`, `wiki_entry_id`) VALUES
('event_details', 'Event Details', '/veranstaltungen/details/:eventId',
'## Features
- **Umfassende Übersicht:** Zeigt alle relevanten Informationen zu einem Event an einem Ort.
- **Tabs:** Gliedert die Informationen in die Bereiche Aufgaben, Inventar-Checkliste und Event-Chat.
- **Echtzeit-Chat:** Ermöglicht die Live-Kommunikation mit allen anderen zugewiesenen Team-Mitgliedern während eines Events.
- **Aufgabenliste:** Zeigt alle für das Event definierten Aufgaben und deren Status.
- **Checkliste:** Eine interaktive Liste des reservierten Materials zum Abhaken beim Ein- und Ausladen.
- **Galerie (nach Abschluss):** Zeigt eine Fotogalerie des Events.

## Use Cases
- Informiere dich umfassend über ein Event, für das du dich interessierst oder für das du eingeteilt bist.
- Koordiniere dich während des Events in Echtzeit mit deinem Team.',
'["events", "admin_debriefing_details"]',
0, NULL),

('meeting_details', 'Lehrgangs-Details', '/lehrgaenge/details/:meetingId',
'## Features
- **Termindetails:** Zeigt alle Informationen zu einem spezifischen Lehrgangs-Termin, inklusive Datum, Ort, Leiter und Beschreibung.
- **Anhänge:** Bietet Zugriff auf relevante Dokumente oder Präsentationen für den Lehrgang.

## Use Cases
- Informiere dich vor einem Lehrgang über die genauen Details und lade ggf. Schulungsmaterial herunter.',
'["lehrgaenge"]',
0, NULL),

('password_change', 'Passwort Ändern', '/passwort',
'## Features
- **Sichere Passwortänderung:** Ermöglicht es dir, dein eigenes Passwort zu ändern.
- **Validierung:** Stellt sicher, dass dein neues Passwort den Sicherheitsrichtlinien entspricht.

## Use Cases
- Ändere dein initiales, zufälliges Passwort nach der ersten Anmeldung.
- Ändere dein Passwort regelmäßig, um die Sicherheit deines Kontos zu gewährleisten.',
'["profile"]',
0, NULL),

('event_feedback_form', 'Event-Feedback', '/feedback/event/:eventId',
'## Features
- **Sterne-Bewertung:** Gib eine schnelle Bewertung für den Gesamteindruck des Events ab.
- **Text-Feedback:** Formuliere detaillierte Kommentare und Verbesserungsvorschläge.

## Use Cases
- Gib den Organisatoren nach einem Event eine schnelle und unkomplizierte Rückmeldung.',
'["profile", "admin_feedback"]',
0, NULL),

('search_results', 'Suchergebnisse', '/suche',
'## Features
- **Globale Suche:** Zeigt die Ergebnisse deiner Suche über alle Bereiche der Anwendung (Events, Lager, Doku, etc.) an.
- **Kategorisierung:** Gruppiert die Ergebnisse nach Typ für eine bessere Übersicht.
- **Direktlinks:** Führt dich mit einem Klick direkt zur Detailseite des gefundenen Eintrags.

## Use Cases
- Finde schnell Informationen, ohne durch die einzelnen Menüs navigieren zu müssen.',
'[]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/service/SearchService.java')),

('pack_kit', 'Kit Packliste', '/pack-kit/:kitId',
'## Features
- **Druckbare Ansicht:** Zeigt eine übersichtliche, für den Ausdruck optimierte Liste aller Artikel, die in ein Kit gehören.
- **Checklisten-Format:** Ermöglicht das Abhaken der einzelnen Positionen beim Packen.

## Use Cases
- Scanne den QR-Code auf einem Koffer, um sofort die Packliste auf deinem Handy zu sehen.
- Drucke die Liste aus, um das Packen für ein Event zu erleichtern.',
'["admin_kits"]',
0, NULL),

('qr_action', 'QR-Code Aktion', '/lager/qr-aktion/:itemId',
'## Features
- **Schnellaktionen:** Bietet eine extrem vereinfachte Oberfläche zur schnellen Entnahme oder Rückgabe eines Artikels.
- **Optimiert für Mobilgeräte:** Das Layout ist speziell für die schnelle Bedienung auf einem Handy konzipiert.

## Use Cases
- Scanne den QR-Code auf einem Lagerartikel, um ihn schnell auszubuchen, ohne einen Computer benutzen zu müssen.',
'["storage_details"]',
0, NULL),

('login', 'Login', '/login',
'## Features
- **Benutzer-Authentifizierung:** Ermöglicht die Anmeldung mit Benutzername und Passwort.

## Use Cases
- Greife auf deinen personalisierten Bereich der Anwendung zu.',
'[]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/api/v1/auth/AuthResource.java')),

('forbidden', 'Zugriff Verweigert (403)', '/forbidden',
'## Features
- **Sicherheits-Feedback:** Informiert dich darüber, dass du versucht hast, auf einen Bereich zuzugreifen, für den deine aktuelle Rolle oder deine Berechtigungen nicht ausreichen.

## Use Cases
- Tritt auf, wenn ein normaler Benutzer versucht, eine Admin-URL direkt aufzurufen.',
'[]',
0, NULL),

('not_found', 'Seite Nicht Gefunden (404)', '*',
'## Features
- **Fehlerseite:** Informiert dich darüber, dass die von dir aufgerufene URL nicht existiert.

## Use Cases
- Tritt auf, wenn du einen veralteten Link oder eine falsch eingegebene URL aufrufst.',
'[]',
0, NULL),

('error_page', 'Interner Serverfehler (500)', 'errorElement',
'## Features
- **Fehlerseite:** Informiert dich darüber, dass ein unerwarteter Fehler auf dem Server aufgetreten ist.

## Use Cases
- Tritt auf, wenn ein Programmierfehler oder ein unvorhergesehenes Problem im Backend auftritt.',
'[]',
0, (SELECT id FROM wiki_documentation WHERE file_path = 'src/main/java/de/config/GlobalExceptionHandler.java')),

('help_list', 'Hilfe-Übersicht', '/help',
'## Features
- **Inhaltsverzeichnis:** Listet alle verfügbaren Hilfeseiten der Anwendung auf.
- **Navigation:** Dient als zentraler Einstiegspunkt in die Anwendungsdokumentation.

## Use Cases
- Finde schnell eine Anleitung für die Seite, bei der du gerade nicht weiterweißt.',
'["help_details"]',
0, NULL),

('help_details', 'Hilfe-Detailseite', '/help/:pageKey',
'## Features
- **Detaillierte Erklärung:** Beschreibt die Funktionen und Anwendungsfälle einer spezifischen Seite.
- **Verknüpfungen:** Bietet Links zu verwandten Hilfeseiten und zur technischen Dokumentation in der Admin-Wiki.

## Use Cases
- Lerne im Detail, wie eine bestimmte Seite funktioniert und wofür du sie verwenden kannst.',
'["help_list"]',
0, NULL);