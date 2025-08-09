-- Flyway migration V69, Part 10: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/technikteam/api/v1/auth/AuthResource.java',
'## 1. File Overview & Purpose

This `@RestController` handles all primary authentication and session management endpoints. It is responsible for user login, logout, and retrieving the current user''s session data.

## 2. Architectural Role

This is a core component of the **Security Tier**, operating at the **Web/API Tier**. It is the main entry point for users to authenticate with the application.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- **Spring Security (`@AuthenticationPrincipal`)**: For accessing the currently authenticated user details.
- `AuthService`: The service containing the logic for JWT creation, validation, and cookie management.
- `LoginAttemptService`: The service used to track and block failed login attempts.
- `UserDAO`: For validating user credentials.
- `LoginRequest` (DTO): A record for the login request payload.

## 4. In-Depth Breakdown

- **`login(...)`**: `POST /login` - This is the main login endpoint.
    1.  It first checks if the user is currently locked out via the `LoginAttemptService`.
    2.  If not locked out, it calls `userDAO.validateUser()` to check the credentials against the database.
    3.  On success, it clears any failed login attempts, calls `authService.addJwtCookie()` to set the secure, HttpOnly authentication cookie on the response, and returns the `User` object.
    4.  On failure, it records the failed attempt and returns an HTTP 401 (Unauthorized) error.
- **`getCurrentUser(...)`**: `GET /me` - This endpoint is called by the frontend on application load to establish the user''s session. It uses the `@AuthenticationPrincipal` (populated by the `JwtAuthFilter`) to get the current user, retrieves their authorized navigation items from the `NavigationRegistry`, and returns both in the response.
- **`logout(...)`**: `POST /logout` - Calls `authService.clearJwtCookie()` to send a response header that immediately expires the authentication cookie on the client''s browser, effectively logging them out.'),

('src/main/java/de/technikteam/api/v1/auth/LoginRequest.java',
'## 1. File Overview & Purpose

This is a simple **Data Transfer Object (DTO)** implemented as a Java `record`. Its purpose is to define a strongly-typed structure for the JSON payload of a login request.

## 2. Architectural Role

This class is part of the **Model Tier**, specifically for the **Web/API Tier**. It is used as the `@RequestBody` in the `AuthResource`''s login method.

## 3. Key Dependencies & Libraries

- **Jakarta Validation (`@NotBlank`)**: Provides declarative validation, ensuring that both username and password fields are not empty.
- **Swagger/OpenAPI (`@Schema`)**: Provides metadata for the auto-generated API documentation, describing the fields and marking them as required.

## 4. In-Depth Breakdown

- **`record`**: Using a Java record automatically generates a final class with a constructor, getters, `equals()`, `hashCode()`, and `toString()` methods, reducing boilerplate code for this simple, immutable data container.'),

('src/main/java/de/technikteam/api/v1/dto/CategoryRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for a request to create a new file category.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `AdminFileResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation (`@NotBlank`, `@Size`)**: For declarative validation of the category name.

## 4. In-Depth Breakdown

- The record ensures that any request to create a category must contain a non-blank `name` field that is between 2 and 100 characters long.'),

('src/main/java/de/technikteam/api/v1/dto/EventDebriefingDTO.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for a request to create or update an `EventDebriefing`.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `AdminEventDebriefingResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation (`@NotNull`)**: Ensures that the core feedback fields are not null.

## 4. In-Depth Breakdown

- The record defines the fields for what went well, what to improve, equipment notes, and a list of user IDs for standout crew members. This provides a clear, strongly-typed contract for the API endpoint.'),

('src/main/java/de/technikteam/api/v1/dto/EventUpdateRequest.java',
'## 1. File Overview & Purpose

This is a comprehensive Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for the JSON part of a multipart request to create or update an `Event`.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestPart` in the `AdminEventResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: For declarative validation of key fields like `name` and `eventDateTime`.

## 4. In-Depth Breakdown

- This record aggregates all possible data for an event into a single object:
    - Core event details (name, times, location, etc.).
    - Skill requirements (`requiredCourseIds`, `requiredPersons`).
    - Item reservations (`itemIds`, `quantities`).
- This simplifies the controller logic, as all this data can be deserialized and validated automatically from a single JSON object.'),

('src/main/java/de/technikteam/api/v1/dto/GeneralFeedbackRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for a user submitting general feedback.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `PublicFeedbackResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: For declarative validation of the `subject` and `content` fields.

## 4. In-Depth Breakdown

- The record ensures that all general feedback submissions have a non-blank subject and content, enforcing data integrity at the API boundary.'),

('src/main/java/de/technikteam/api/v1/dto/MeetingRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for creating or updating a `Meeting`.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `MeetingResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Provides validation rules, such as `@FutureOrPresent` to ensure a meeting is not scheduled in the past.

## 4. In-Depth Breakdown

- This record encapsulates all the necessary data for a meeting and uses validation annotations to enforce business rules directly on the DTO, simplifying validation logic in the controller.'),

('src/main/java/de/technikteam/api/v1/dto/NotificationRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for an administrative request to send a broadcast notification.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `AdminNotificationResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Provides validation rules for the notification fields.
- **Swagger/OpenAPI (`@Schema`)**: The `allowableValues` attribute is used to document the valid options for `level` and `targetType` in the API documentation.

## 4. In-Depth Breakdown

- This record defines all the parameters for a notification: its content (`title`, `description`), its severity (`level`), and its audience (`targetType`, `targetId`). This structured approach makes the notification sending process robust and easy to validate.'),

('src/main/java/de/technikteam/api/v1/dto/PasswordChangeRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for a request from a user to change their own password.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `PublicProfileResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Ensures all three password fields are not blank.

## 4. In-Depth Breakdown

- The record contains the user''s `currentPassword` (for verification), the `newPassword`, and a `confirmPassword`. The service logic will then validate that the current password is correct and that the new password and confirmation match.'),

('src/main/java/de/technikteam/api/v1/dto/ProfileChangeRequestDTO.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for a user submitting a request to change their profile data.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `PublicProfileResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Includes an `@Email` annotation for basic email format validation.

## 4. In-Depth Breakdown

- This record contains all the fields a user is allowed to request changes for. The fields are nullable (`Integer`, not `int`) because a user might only want to change one field at a time. The service logic will compare the non-null values in this DTO against the user''s current data to determine what has actually changed.'),

('src/main/java/de/technikteam/api/v1/dto/UserCreateRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for an administrative request to create a new user.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `UserResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Provides comprehensive validation rules for all fields, ensuring data integrity before the service layer is even invoked.

## 4. In-Depth Breakdown

- This record defines all the necessary fields for creating a new user, including their username, initial password, role, and initial set of individual permissions. Using a dedicated DTO for creation allows for stricter validation (e.g., making the password non-blank).'),

('src/main/java/de/technikteam/api/v1/dto/UserUpdateRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for an administrative request to update an existing user.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `UserResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Provides validation rules for the user fields.

## 4. In-Depth Breakdown

- This DTO is similar to the `UserCreateRequest` but notably lacks a `password` field, as password changes are handled by a separate, dedicated "reset password" endpoint. This follows the principle of specific DTOs for specific actions.'),

('src/main/java/de/technikteam/api/v1/dto/WikiUpdateRequest.java',
'## 1. File Overview & Purpose

This is a Data Transfer Object (DTO) implemented as a Java `record`. It defines the structure for a request to update the content of a wiki page.

## 2. Architectural Role

This class is part of the **Model Tier** and is used in the **Web/API Tier** as the `@RequestBody` for the `WikiResource`.

## 3. Key Dependencies & Libraries

- **Jakarta Validation**: Ensures that the `content` field is not null.

## 4. In-Depth Breakdown

- This simple record encapsulates the new Markdown content for a wiki page, providing a clear and type-safe API contract for the update operation.');
COMMIT;