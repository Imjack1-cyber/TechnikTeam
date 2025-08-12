-- Flyway migration V65, Part 6: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/api/v1/AdminAchievementResource.java',
'## 1. File Overview & Purpose

This class is a Spring `@RestController` that exposes administrative **CRUD (Create, Read, Update, Delete) endpoints** for managing `Achievement` entities. It provides the API for the admin page where achievements (badges) are defined.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles incoming HTTP requests for the `/api/v1/achievements` path, delegates the business logic to the `AchievementDAO`, and returns standardized `ApiResponse` objects in JSON format.

## 3. Key Dependencies & Libraries

- **Spring Web (`@RestController`, `@GetMapping`, etc.)**: Core annotations for defining REST endpoints.
- `AchievementDAO`: The Data Access Object used for all database operations related to achievements.
- `ApiResponse` (Model): The standard DTO for JSON responses.
- **Swagger/OpenAPI**: Annotations like `@Tag` and `@Operation` are used to generate API documentation.

## 4. In-Depth Breakdown

- **`getAllAchievements()`**: Handles `GET /`. Fetches all achievement definitions from the DAO.
- **`createAchievement(...)`**: Handles `POST /`. Takes an `Achievement` object from the request body and saves it via the DAO.
- **`updateAchievement(...)`**: Handles `PUT /{id}`. Updates an existing achievement.
- **`deleteAchievement(...)`**: Handles `DELETE /{id}`. Deletes an achievement.'),

('src/main/java/de/api/v1/AdminAnnouncementResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing announcements on the Digital Bulletin Board. It is the backend for the `/admin/announcements` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles HTTP requests, delegates logic to the `AnnouncementService`, and returns standardized JSON responses.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `AnnouncementService`: The service layer containing the business logic for managing announcements, including sanitization and logging.
- `SecurityUser`: Used to get the currently authenticated admin user for logging purposes.

## 4. In-Depth Breakdown

- **`getAllAnnouncements()`**: `GET /` - Fetches all announcements.
- **`createAnnouncement(...)`**: `POST /` - Creates a new announcement. It passes the DTO and the current admin user to the service.
- **`updateAnnouncement(...)`**: `PUT /{id}` - Updates an existing announcement.
- **`deleteAnnouncement(...)`**: `DELETE /{id}` - Deletes an announcement.'),

('src/main/java/de/api/v1/AdminChangelogResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `Changelog` entries. It is the backend for the `/admin/changelogs` page, allowing admins to communicate updates to users.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles HTTP requests related to changelog management and delegates logic to the `ChangelogService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ChangelogService`: The service layer containing business logic for changelogs.

## 4. In-Depth Breakdown

- **`getAllChangelogs()`**: `GET /` - Retrieves all changelog entries.
- **`createChangelog(...)`**: `POST /` - Creates a new changelog entry.
- **`updateChangelog(...)`**: `PUT /{id}` - Updates an existing entry.
- **`deleteChangelog(...)`**: `DELETE /{id}` - Deletes an entry.'),

('src/main/java/de/api/v1/AdminChecklistTemplateResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `ChecklistTemplate` entities. It serves as the backend for the admin page where pre-flight checklist templates are created and managed.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles HTTP requests, delegates logic to the `ChecklistTemplateDAO`, and returns standardized JSON responses.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ChecklistTemplateDAO`: The DAO for all database operations related to checklist templates.

## 4. In-Depth Breakdown

- **`getAllTemplates()`**: `GET /` - Retrieves all templates.
- **`createTemplate(...)`**: `POST /` - Creates a new template and its associated items in a transaction.
- **`updateTemplate(...)`**: `PUT /{id}` - Updates a template and its items.
- **`deleteTemplate(...)`**: `DELETE /{id}` - Deletes a template.'),

('src/main/java/de/api/v1/AdminDamageReportResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides endpoints for administrators to manage user-submitted `DamageReport`s. It allows admins to view pending reports and then either confirm (which marks the item as defective) or reject them.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/damage-reports` page and delegates the complex, transactional logic to the `StorageService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `StorageService`: The service containing the business logic for confirming or rejecting a damage report.
- `DamageReportDAO`: Used to fetch the list of pending reports.

## 4. In-Depth Breakdown

- **`getPendingReports()`**: `GET /pending` - Fetches all reports with a "PENDING" status.
- **`confirmReport(...)`**: `POST /{reportId}/confirm` - Triggers the `storageService` to mark the associated item as defective and update the report''s status to "CONFIRMED".
- **`rejectReport(...)`**: `POST /{reportId}/reject` - Triggers the `storageService` to update the report''s status to "REJECTED" and record the admin''s notes.'),

('src/main/java/de/api/v1/AdminDashboardResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a single, aggregated API endpoint for the administrative dashboard. It fetches all the data required for the dashboard widgets in one call.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the sole data source for the `/admin/dashboard` frontend page. It delegates all data aggregation logic to the `AdminDashboardService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `AdminDashboardService`: The service that gathers data from multiple DAOs to build the `DashboardDataDTO`.

## 4. In-Depth Breakdown

- **`getDashboardData()`**: `GET /` - Calls the `dashboardService` to get a `DashboardDataDTO` containing upcoming events, low-stock items, recent logs, and event trend data, then returns it as a JSON response.'),

('src/main/java/de/api/v1/AdminDocumentationResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing the user-facing `PageDocumentation`. It is the backend for the `/admin/documentation` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles HTTP requests and delegates business logic to the `PageDocumentationService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `PageDocumentationService`: The service that handles the business logic for documentation pages.

## 4. In-Depth Breakdown

- **`getAllDocs()`**: `GET /` - Retrieves all documentation pages.
- **`getDocByKey(...)`**: `GET /{pageKey}` - Retrieves a single documentation page by its unique key.
- **`createDoc(...)`**: `POST /` - Creates a new documentation page.
- **`updateDoc(...)`**: `PUT /{id}` - Updates an existing page.
- **`deleteDoc(...)`**: `DELETE /{id}` - Deletes a page.');
COMMIT;