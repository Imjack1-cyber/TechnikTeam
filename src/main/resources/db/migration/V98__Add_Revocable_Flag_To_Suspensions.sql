-- Flyway migration V98: Add context to existing USER_SUSPEND logs to make them revocable

UPDATE admin_logs
SET context = JSON_OBJECT(
    'revocable', TRUE,
    'userId', CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(details, '(ID: ', -1), ')', 1) AS UNSIGNED)
)
WHERE action_type = 'USER_SUSPEND' AND status = 'ACTIVE' AND context IS NULL;