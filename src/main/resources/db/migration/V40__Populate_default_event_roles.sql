-- Flyway migration V40: Populate with some default event roles

INSERT INTO `event_roles` (`name`, `description`, `icon_class`) VALUES
('Event-Leitung', 'Gesamtverantwortlicher für die Veranstaltung.', 'fa-user-tie'),
('Audio-Technik', 'Verantwortlich für Mikrofone, Mischpult und Beschallung.', 'fa-sliders-h'),
('Licht-Technik', 'Verantwortlich für Scheinwerfer, Lichtpult und Beleuchtung.', 'fa-lightbulb'),
('Video-Technik', 'Verantwortlich für Kameras, Projektoren und Videomischung.', 'fa-video'),
('Bühnenhelfer', 'Allgemeine Unterstützung auf und hinter der Bühne.', 'fa-users');
