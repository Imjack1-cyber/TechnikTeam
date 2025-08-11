CREATE TABLE system_settings (
    setting_key VARCHAR(50) PRIMARY KEY NOT NULL,
    setting_value TEXT,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO system_settings (setting_key, setting_value) VALUES ('maintenance_mode', 'false');