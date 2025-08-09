-- Flyway migration V22: Remove the insecure, hardcoded default admin user

-- First remove permissions to avoid foreign key constraint issues.
DELETE up FROM user_permissions up
JOIN users u ON up.user_id = u.id
WHERE u.username = 'admin';

-- Then delete the user.
DELETE FROM users WHERE username = 'admin';