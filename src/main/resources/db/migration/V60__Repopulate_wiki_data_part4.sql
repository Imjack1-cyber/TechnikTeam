-- Flyway migration V60, Part 4: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/technikteam/util/DaoUtils.java',
'## 1. File Overview & Purpose

This is a small utility class that provides common helper methods for DAO classes. Its purpose is to encapsulate reusable database-related logic, reducing code duplication across the DAO layer.

## 2. Architectural Role

This is a utility class for the **DAO (Data Access) Tier**.

## 3. Key Dependencies & Libraries

- **`java.sql.ResultSet`**: The standard JDBC class it operates on.

## 4. In-Depth Breakdown

- **`hasColumn(ResultSet rs, String columnName)`**: This is a crucial helper method used in many DAOs. It safely checks if a given `ResultSet` contains a column with a specific name (case-insensitively). This is extremely useful in DAOs that perform complex JOINs where a column might be present in some results but not others (e.g., `leader_username`). Using this check before calling `rs.getString(...)` prevents a `SQLException` if the column doesn''t exist for a particular row.'),

('src/main/java/de/technikteam/util/FileSignatureValidator.java',
'## 1. File Overview & Purpose

This is a security utility class designed to validate uploaded files based on their "magic numbers" (the first few bytes of the file) rather than just their file extension or MIME type. This provides a strong defense against attacks where a malicious executable file is renamed to something benign like `.jpg`.

## 2. Architectural Role

This is a cross-cutting **Security/Utility** component. It is used by the `FileService` before any file is saved to the disk to ensure the file''s actual content matches its declared type.

## 3. Key Dependencies & Libraries

- **Spring (`MultipartFile`)**: The object representing the uploaded file.

## 4. In-Depth Breakdown

- **`MAGIC_NUMBERS` (static Map)**: A map that stores the known byte signatures for a whitelist of allowed MIME types (JPEG, PNG, PDF, etc.).
- **`isFileTypeAllowed(MultipartFile file)`**: The main validation method.
    1.  It gets the MIME type declared by the browser (e.g., `image/jpeg`).
    2.  It looks up the expected byte signature(s) for that MIME type in the `MAGIC_NUMBERS` map. If the type is not in the map, the file is rejected.
    3.  It reads the first few bytes from the actual file''s input stream.
    4.  It compares these actual bytes with the expected signature. If they match, the file is considered valid and the method returns `true`.
    5.  If they do not match, it logs a security warning and returns `false`, preventing the file from being processed further.'),

('src/main/java/de/technikteam/util/MarkdownUtil.java',
'## 1. File Overview & Purpose

This is a **deprecated** utility class. Its original purpose was to sanitize Markdown content to prevent XSS attacks. However, it has been replaced by the much more robust **OWASP Java HTML Sanitizer**, which is configured in `SanitizerConfig.java`.

## 2. Architectural Role

This is a **Legacy/Utility** class. It is no longer used in security-sensitive contexts. It is kept for historical reference or for potential use in non-security-related text transformations.

## 3. Key Dependencies & Libraries

- None of significance to the current application.

## 4. In-Depth Breakdown

The `transform` method is now a simple pass-through and performs no sanitization. All sanitization logic has been moved to the `PolicyFactory` beans defined in `SanitizerConfig.java`.'),

('src/main/java/de/technikteam/util/NavigationRegistry.java',
'## 1. File Overview & Purpose

This class is a centralized, static registry for all navigation links in the application''s sidebar. It defines the complete set of possible navigation items and provides a single method to generate a user-specific list of links based on their assigned permissions. This approach ensures a single source of truth for the site''s navigation structure and access control.

## 2. Architectural Role

This is a **Configuration/Utility** class that primarily supports the **Web/Controller Tier**. It is called by the `AuthService` (when generating the `/auth/me` response) to provide the frontend with the correct navigation items for the logged-in user.

## 3. Key Dependencies & Libraries

- `Permissions`: The class containing all permission key constants.
- `NavigationItem` (Model): The object used to represent each link.
- `User` (Model): The user object, which contains the permissions used for filtering.

## 4. In-Depth Breakdown

- **`ALL_ITEMS` (static List)**: A static list that is initialized once with `NavigationItem` objects for every possible link in the application, for both the user and admin sections. Each item is defined with its label, URL, icon, and the required permission key. Links available to all authenticated users have a `null` permission.
- **`getNavigationItemsForUser(User user)`**: The core method. It filters the master `ALL_ITEMS` list down to only those items the provided user is authorized to see. An item is included if:
    1.  Its required permission is `null`.
    2.  The user has the master `ACCESS_ADMIN_PANEL` permission.
    3.  The user''s set of permissions contains the specific permission required by the item.'),

('src/main/java/de/technikteam/util/PasswordPolicyValidator.java',
'## 1. File Overview & Purpose

This is a utility class for enforcing a strong password policy. It provides a single static method to validate a given password against a set of predefined complexity rules (minimum length, character types). This ensures that all new passwords set in the application meet the required security standards.

## 2. Architectural Role

This is a cross-cutting **Security/Utility** component. It is used in the **Web/API Tier** by the `UserResource` (for creating new users) and `PublicProfileResource` (for password changes) to validate passwords before they are hashed and stored.

## 3. Key Dependencies & Libraries

- `java.util.regex.Pattern`: Used to define the regular expressions for checking character types.

## 4. In-Depth Breakdown

- **Static Patterns & Constants**: The class pre-compiles `Pattern` objects for efficient checking of required character types (uppercase, lowercase, digit, special character) and defines a `MIN_LENGTH`.
- **`ValidationResult` (Inner Class)**: A simple record-like class to return both a boolean `isValid` status and a user-friendly `message` explaining the result.
- **`validate(String password)`**: The main validation logic. It checks the password against each rule and builds a list of human-readable error messages. If any rules fail, it concatenates the errors into a single, comprehensive message (e.g., "Das Passwort muss mindestens 10 Zeichen lang sein, mindestens einen Großbuchstaben enthalten.") and returns it in the `ValidationResult`.');
COMMIT;