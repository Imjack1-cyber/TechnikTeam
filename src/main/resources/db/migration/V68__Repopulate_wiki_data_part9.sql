-- Flyway migration V68, Part 9: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/api/v1/LogResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides an API endpoint for viewing the `AdminLog`. It is the backend for the `/admin/log` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It directly interacts with the `AdminLogDAO` to retrieve log data.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `AdminLogDAO`: The DAO for retrieving log entries.

## 4. In-Depth Breakdown

- **`getLogs(...)`**: `GET /` - Retrieves admin action logs. It accepts an optional `limit` query parameter to fetch only the most recent N entries, which is used by the admin dashboard widget.'),

('src/main/java/de/api/v1/MatrixResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the API endpoints for the Qualification Matrix feature. It serves the aggregated data needed to build the matrix view and handles updates to attendance records.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/matrix` page. It coordinates several DAOs to assemble the matrix data.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `UserDAO`, `CourseDAO`, `MeetingDAO`, `MeetingAttendanceDAO`: The various DAOs needed to fetch all users, all courses, all meetings for those courses, and all attendance records.

## 4. In-Depth Breakdown

- **`getMatrixData()`**: `GET /` - This is the main data-fetching endpoint. It queries all necessary tables and returns a single, large JSON object containing lists of users, courses, meetings grouped by course, and a map of all attendance records. The frontend then uses this data to construct the visual matrix.
- **`updateAttendance(...)`**: `PUT /attendance` - This endpoint is called when an admin clicks a cell in the matrix. It takes a `MeetingAttendance` object and updates the database, marking a user as having attended (or not attended) a specific meeting.'),

('src/main/java/de/api/v1/MeetingResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `Meeting` entities (specific training dates). It is the backend for the `/admin/lehrgaenge/:courseId/meetings` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates database operations to the `MeetingDAO` and business logic (like cloning) to the `EventService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `MeetingDAO`: The DAO for meeting-related database operations.
- `EventService`: Contains the logic for cloning a meeting.
- `MeetingRequest` (DTO): A record for validating incoming create/update requests.

## 4. In-Depth Breakdown

- **`getMeetingsForCourse(...)`**: `GET /` - Retrieves all meetings for a specific parent course.
- **`getMeetingById(...)`**: `GET /{id}` - Retrieves a single meeting''s details.
- **`createMeeting(...)`**: `POST /` - Creates a new meeting.
- **`updateMeeting(...)`**: `PUT /{id}` - Updates an existing meeting.
- **`cloneMeeting(...)`**: `POST /{id}/clone` - Clones a meeting to serve as a template for a new one.
- **`deleteMeeting(...)`**: `DELETE /{id}` - Deletes a meeting.'),

('src/main/java/de/api/v1/ReportResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides API endpoints for generating analytical reports and aggregated statistics. It is the backend for the `/admin/berichte` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates the execution of complex, aggregate SQL queries to the specialized `ReportDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ReportDAO`: The DAO containing all the analytical queries.

## 4. In-Depth Breakdown

- **`getDashboardReport()`**: `GET /dashboard` - A dedicated endpoint that aggregates several key metrics (event trend, user activity, inventory value) required specifically for the admin reports dashboard page. This provides all necessary data in a single API call.'),

('src/main/java/de/api/v1/StorageResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the primary administrative CRUD endpoints for managing `StorageItem` entities. It is the backend for the main `/admin/lager` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates database operations directly to the `StorageDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `StorageDAO`: The DAO for all inventory item database operations.

## 4. In-Depth Breakdown

- **`getAllItems()`**: `GET /` - Retrieves all items in the inventory.
- **`createItem(...)`**: `POST /` - Creates a new inventory item.
- **`updateItem(...)`**: `PUT /{id}` - Updates an existing item.
- **`deleteItem(...)`**: `DELETE /{id}` - Deletes an item.'),

('src/main/java/de/api/v1/SystemResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides an API endpoint to retrieve live statistics about the server''s system health. It is the backend for the `/admin/system` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates the task of collecting OS-level metrics to the `SystemInfoService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `SystemInfoService`: The service that interacts with the OS to get stats like CPU, RAM, and disk usage.

## 4. In-Depth Breakdown

- **`getSystemStats()`**: `GET /stats` - Calls the `systemInfoService` to gather current system statistics and returns them in a `SystemStatsDTO`.'),

('src/main/java/de/api/v1/UserResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the primary administrative CRUD and management endpoints for `User` accounts. It is the backend for the `/admin/mitglieder` page.

## 2. Architectural Role

This is a key component of the **Web/API Tier**. It handles all user management tasks, delegating transactional operations to the `UserService` and simple queries to the `UserDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `UserService`: For transactional creation and updates of users and their permissions.
- `UserDAO`: For fetching and deleting users.
- `LoginAttemptService`: For unlocking user accounts.
- `PasswordPolicyValidator`: For validating new passwords.
- `UserCreateRequest`, `UserUpdateRequest` (DTOs): For validating incoming request bodies.

## 4. In-Depth Breakdown

- **`getAllUsers()`**: `GET /` - Retrieves a list of all users.
- **`getUserById(...)`**: `GET /{id}` - Retrieves a single user with their full set of permissions.
- **`createUser(...)`**: `POST /` - Creates a new user after validating the password policy.
- **`updateUser(...)`**: `PUT /{id}` - Updates a user''s details and permissions.
- **`deleteUser(...)`**: `DELETE /{id}` - Deletes a user, with a check to prevent the deletion of the default admin.
- **`resetPassword(...)`**: `POST /{id}/reset-password` - Generates a new random password for a user and returns it in the API response.
- **`unlockUser(...)`**: `POST /{id}/unlock` - Clears the failed login attempts for a user, unlocking their account.'),

('src/main/java/de/api/v1/WikiResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for the technical `WikiEntry` documentation. It is the backend for the `/admin/wiki` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates data retrieval and persistence to the `WikiDAO` and the hierarchical tree generation to the `WikiService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `WikiService`: For generating the navigation tree structure.
- `WikiDAO`: For all database operations related to wiki pages.
- `WikiUpdateRequest` (DTO): For validating update requests.

## 4. In-Depth Breakdown

- **`getWikiTree()`**: `GET /` - Retrieves all wiki pages and structures them into a nested map for the frontend navigation tree.
- **`getWikiList()`**: `GET /list` - Retrieves a flat list of all wiki pages, used for populating dropdowns in other parts of the admin UI.
- **`getWikiEntryById(...)`**: `GET /{id}` - Fetches the content of a single wiki page.
- **`createWikiEntry(...)`**: `POST /` - Creates a new wiki page.
- **`updateWikiEntry(...)`**: `PUT /{id}` - Updates the content of an existing wiki page.
- **`deleteWikiEntry(...)`**: `DELETE /{id}` - Deletes a wiki page.');
COMMIT;