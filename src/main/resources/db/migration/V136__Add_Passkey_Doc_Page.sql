-- Flyway migration V136: Add documentation page for the passkey feature

INSERT INTO `page_documentation` (`page_key`, `title`, `page_path`, `features`, `related_pages`, `admin_only`)
VALUES ('feature_passkeys', 'Passkeys (Passwortloser Login)', '/profil',
'## Was ist ein Passkey?
Ein Passkey ersetzt dein Passwort. Statt ein Passwort einzutippen, kannst du dich mit den gleichen biometrischen Daten (z.B. Fingerabdruck, Face ID) oder der PIN anmelden, die du zum Entsperren deines Geräts (Smartphone, Computer) verwendest.

## Vorteile
- **Sicherer:** Passkeys sind deutlich sicherer als Passwörter. Sie können nicht gestohlen oder erraten werden.
- **Einfacher:** Kein Merken von komplexen Passwörtern mehr.
- **Schneller:** Der Login erfolgt in der Regel schneller als die Eingabe von Benutzername und Passwort.

## Wie registriere ich einen Passkey?
1. Gehe zu deinem Profil und dort zum Tab "Sicherheit".
2. Klicke auf "Diesen Browser als Passkey hinzufügen".
3. Folge den Anweisungen deines Geräts oder Browsers. Du wirst eventuell aufgefordert, deinen Fingerabdruck zu scannen oder deine Geräte-PIN einzugeben.
4. Gib dem Passkey einen Namen, damit du ihn später wiedererkennst (z.B. "Mein iPhone" oder "Arbeitslaptop").

## FAQ
**Was passiert, wenn ich mein Gerät verliere?**
Du kannst dich weiterhin mit deinem Passwort anmelden und den verlorenen Passkey in deinem Profil löschen. Du solltest Passkeys auf mehreren Geräten registrieren, die du besitzt.

**Kann ich einen Passkey auf mehreren Geräten verwenden?**
Ja! Moderne Passkeys (sog. "passende" Passkeys) werden oft über deinen Account-Anbieter (z.B. Google, Apple) sicher zwischen deinen Geräten synchronisiert.',
'["profile", "password_change"]',
0);