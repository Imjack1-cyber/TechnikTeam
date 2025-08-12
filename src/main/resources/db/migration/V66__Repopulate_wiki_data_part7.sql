-- Flyway migration V66, Part 7: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/api/v1/AdminEventDebriefingResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the API endpoints for managing post-event `EventDebriefing` reports. It allows authorized users (admins or event leaders) to create, view, and update these reports.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/veranstaltungen/:eventId/debriefing` and `/admin/debriefings` pages. It delegates complex business logic, such as data enrichment and notifications, to the `EventDebriefingService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `EventDebriefingService`: The service that handles the creation, updating, and data enrichment of debriefings.
- `EventDAO`, `EventDebriefingDAO`: Used by the service and controller for database access.

## 4. In-Depth Breakdown

- **`getAllDebriefings()`**: `GET /debriefings` - Retrieves a list of all debriefing reports.
- **`getDebriefingForEvent(...)`**: `GET /{eventId}/debriefing` - Fetches the specific debriefing for a given event.
- **`saveDebriefing(...)`**: `POST /{eventId}/debriefing` - This is an "upsert" endpoint. It creates a new debriefing if one doesn''t exist for the event, or updates the existing one if it does.'),

('src/main/java/de/api/v1/AdminEventResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `Event` entities. It handles complex multipart requests that include both JSON data for the event and an optional file upload for attachments.

## 2. Architectural Role

This is a key component of the **Web/API Tier**. It serves as the backend for the main event management page (`/admin/veranstaltungen`). It delegates the complex, transactional logic of saving an event and all its related data (skills, reservations, attachments) to the `EventService`.

## 3. Key Dependencies & Libraries

- **Spring Web (`@RequestPart`)**: Used to handle multipart form data, separating the JSON part from the file part.
- `EventService`: The service that contains the transactional logic for creating, updating, and cloning events.
- `EventDAO`: Used for basic event data retrieval and deletion.
- `EventUpdateRequest` (DTO): A record used to represent the structured JSON data part of the request.

## 4. In-Depth Breakdown

- **`getAllEvents()`**: `GET /` - Retrieves a list of all events.
- **`createEvent(...)`**: `POST /` - Handles the creation of a new event. It receives a multipart request and passes the event data and optional file to the `eventService`.
- **`updateEvent(...)`**: `POST /{id}` - Handles updating an existing event, also using a multipart request.
- **`cloneEvent(...)`**: `POST /{id}/clone` - Triggers the `eventService` to create a deep copy of an existing event, including its skill requirements and tasks.
- **`deleteEvent(...)`**: `DELETE /{id}` - Deletes an event.'),

('src/main/java/de/api/v1/AdminEventRoleResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing reusable `EventRole` entities. It is the backend for the `/admin/event-roles` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates database operations directly to the `EventRoleDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `EventRoleDAO`: The DAO for all database operations related to event roles.

## 4. In-Depth Breakdown

- **`getAllRoles()`**: `GET /` - Retrieves all defined event roles.
- **`createRole(...)`**: `POST /` - Creates a new event role.
- **`updateRole(...)`**: `PUT /{id}` - Updates an existing role.
- **`deleteRole(...)`**: `DELETE /{id}` - Deletes a role.'),

('src/main/java/de/api/v1/AdminFeedbackResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative API endpoints for managing general `FeedbackSubmission`s. It is the backend for the Kanban-style feedback board on the `/admin/feedback` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles fetching all submissions and updating their status.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `FeedbackSubmissionDAO`: The DAO for all database operations related to feedback.

## 4. In-Depth Breakdown

- **`getAllSubmissions()`**: `GET /` - Retrieves all feedback submissions, ordered correctly for display on the Kanban board.
- **`updateStatus(...)`**: `PUT /{id}/status` - Updates the status of a single feedback submission (e.g., from "NEW" to "VIEWED"). This is triggered when a card is moved on the board or updated in the modal.'),

('src/main/java/de/api/v1/AdminFileResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative API for managing files and `FileCategory` entities. It handles file uploads, deletions, and category management.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/dateien` page. It delegates file storage and database logic to the `FileService`.

## 3. Key Dependencies & Libraries

- **Spring Web (`@RequestParam`, `@RequestPart`)**: For handling multipart file uploads.
- `FileService`: The service that contains the logic for securely storing and deleting files, including validation.
- `FileDAO`: Used by the service and for category management.

## 4. In-Depth Breakdown

- **`uploadFile(...)`**: `POST /` - Handles a multipart request to upload a new file, assign it to a category, and set its required role.
- **`deleteFile(...)`**: `DELETE /{id}` - Deletes a file record from the database and the corresponding physical file from the disk.
- **`createCategory(...)`**: `POST /categories` - Creates a new file category.
- **`deleteCategory(...)`**: `DELETE /categories/{id}` - Deletes a category.
- **`getAllFiles()`**: `GET /` - Retrieves all files, regardless of role, for the admin view.
- **`getAllCategories()`**: `GET /categories` - Retrieves all defined file categories.'),

('src/main/java/de/api/v1/AdminFormDataResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a specialized, aggregated API endpoint that serves up all the data needed to populate the administrative user creation and editing forms. It fetches all available roles and permissions in a single request.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It acts as an efficient data source for the frontend''s `AdminUsersPage`, reducing the number of initial HTTP requests required to render the user modal.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `RoleDAO`, `PermissionDAO`: The DAOs used to fetch the form data.

## 4. In-Depth Breakdown

- **`getFormDataForUserForms()`**: `GET /` - The single endpoint in this controller.
    - It calls `roleDAO.getAllRoles()` to get the list of all possible user roles.
    - It calls `permissionDAO.getAllPermissions()` to get a flat list of all permissions.
    - It then processes the permissions list, grouping them by their prefix (e.g., "USER_", "EVENT_") into a `Map` structure.
    - It returns a single JSON object containing both the `roles` list and the `groupedPermissions` map.'),

('src/main/java/de/api/v1/AdminNotificationResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the API endpoints for the real-time notification system. It includes an endpoint for clients to subscribe to Server-Sent Events (SSE) and an endpoint for administrators to send broadcast notifications.

## 2. Architectural Role

This is a key component of the **Web/API Tier** for real-time features. It is the backend for the `/admin/benachrichtigungen` page and the primary entry point for clients to receive live updates.

## 3. Key Dependencies & Libraries

- **Spring Web (`SseEmitter`)**: The core Spring class for handling Server-Sent Events.
- `NotificationService`: The service that manages all SSE connections and broadcasting logic.
- `NotificationRequest` (DTO): A record used for validating the incoming notification payload.

## 4. In-Depth Breakdown

- **`handleSse(...)`**: `GET /sse` - This is the subscription endpoint. When a client connects, this method registers their connection (as an `SseEmitter`) with the `NotificationService` and keeps the connection open to receive push notifications.
- **`sendNotification(...)`**: `POST /` - This endpoint allows an admin to send a notification. It takes a `NotificationRequest` DTO, determines the target audience (all users, event participants, etc.), and calls the `notificationService` to push the message to all relevant connected clients.');
COMMIT;