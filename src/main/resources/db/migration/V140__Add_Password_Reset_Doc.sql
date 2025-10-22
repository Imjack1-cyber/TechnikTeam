-- Flyway migration V140: Add page documentation for the new admin password reset page

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`)
VALUES ('admin_password_resets', 'Admin Passwort-Anfragen', '/admin/users/password-resets',
'## Features
- **Übersicht:** Listet alle von Benutzern über die "Passwort vergessen"-Funktion gestellten Anfragen auf, die über eine Push-Benachrichtigung bestätigt werden sollen.
- **Manueller Reset:** Bietet eine Fallback-Option für Admins, das Passwort eines Benutzers direkt zurückzusetzen, falls dieser keinen Zugriff mehr auf sein Gerät hat.
- **Status-Tracking:** Admins können sehen, wann eine Anfrage gestellt wurde. Nach der Bearbeitung wird die Anfrage als "erledigt" markiert.

## Use Cases
- Bearbeite Passwort-Reset-Anfragen von Benutzern, die die App-Verifizierung nicht nutzen können.
- Behalte den Überblick über alle offenen Sicherheitsanfragen.',
'["admin_users"]',
1);