-- Flyway migration V103: Update page documentation to mention FontAwesome links.

UPDATE `page_documentation`
SET `features` = '## Features
- **Stammdaten:** Verwalte deine persönlichen Daten wie E-Mail, Klasse und dein Profil-Icon. Ein Link hilft bei der Auswahl des Icons aus der FontAwesome-Bibliothek.
- **Sicherheit:** Ändere dein Passwort.
- **Qualifikationen:** Sieh eine Liste aller Lehrgänge, die du erfolgreich absolviert hast.
- **Abzeichen:** Zeigt alle Erfolge an, die du durch deine Teilnahme und dein Engagement freigeschaltet hast.
- **Event-Historie:** Eine komplette Übersicht aller Events, an denen du teilgenommen hast.

## Use Cases
- Halte deine Kontaktdaten aktuell.
- Verfolge deinen Lernfortschritt und deine Erfolge im Team.
- Gib Feedback für vergangene Events.'
WHERE `page_key` = 'profile';

UPDATE `page_documentation`
SET `features` = '## Features
- **Rollen-Verwaltung:** Erstelle, bearbeite und lösche wiederverwendbare Rollen (z.B. "Tontechnik", "Lichttechnik").
- **Icon-Zuweisung:** Weise jeder Rolle ein passendes FontAwesome-Icon für eine bessere visuelle Darstellung zu. Ein Link im Editor hilft bei der Suche.

## Use Cases
- Definiere einen Standardkatalog von Verantwortlichkeiten für Events.
- Strukturiere die Team-Zuweisung bei der Event-Planung.'
WHERE `page_key` = 'admin_event_roles';

UPDATE `page_documentation`
SET `features` = '## Features
- **Abzeichen verwalten:** Erstelle, bearbeite und lösche alle Abzeichen (Achievements), die Benutzer verdienen können.
- **System-Schlüssel:** Definiere einen einzigartigen `achievement_key`, über den das System ein Abzeichen programmatisch verleihen kann.
- **Icon-Auswahl:** Definiere eine FontAwesome-Icon-Klasse für jedes Abzeichen. Ein Link führt direkt zur Icon-Suche.

## Use Cases
- Erweitere das Gamification-System der Anwendung.
- Schaffe neue Anreize für Engagement und Teilnahme im Team.'
WHERE `page_key` = 'admin_achievements';