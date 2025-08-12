-- Flyway migration V72, Part 12: Overhaul Technical Wiki Documentation (Backend Public API)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/api/v1/public_api/PublicDocumentationResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public-facing API endpoints for viewing the `PageDocumentation`. It is the backend for the `/help` and `/help/:pageKey` pages.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles read-only requests for documentation.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `PageDocumentationService`: The service used to fetch documentation and handle authorization logic.

## 4. In-Depth Breakdown

- **`getAllDocs(...)`**: `GET /` - Fetches all documentation pages, automatically filtering out admin-only pages if the current user is not an admin.
- **`getDocByKey(...)`**: `GET /{pageKey}` - Fetches a single documentation page. It includes an authorization check to ensure non-admins cannot access admin-only documentation via a direct URL.'),

('src/main/java/de/api/v1/public_api/PublicEventGalleryResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public API endpoints for interacting with an event''s photo gallery. It allows participants to view, upload, and delete photos.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the "Gallery" tab on the `EventDetailsPage`. It delegates logic to the `EventGalleryService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints and handling multipart file uploads.
- `EventGalleryService`: The service containing the business and authorization logic for gallery operations.

## 4. In-Depth Breakdown

- **`getGallery(...)`**: `GET /{eventId}/gallery` - Retrieves all photo metadata for a given event''s gallery.
- **`uploadPhoto(...)`**: `POST /{eventId}/gallery` - Handles the upload of a new photo. The service layer performs authorization checks to ensure only participants of a completed event can upload.
- **`deletePhoto(...)`**: `DELETE /gallery/{photoId}` - Deletes a photo. The service layer ensures a user can only delete their own photos, unless they are an admin or the event leader.'),

('src/main/java/de/api/v1/public_api/PublicEventResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the main public-facing API endpoints for user interactions with events. It handles fetching event lists, viewing details, and signing up/off.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/veranstaltungen` and `/veranstaltungen/details/:eventId` pages.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `EventDAO`, `EventService`: For database operations and business logic related to events.
- `EventCustomFieldDAO`: For handling custom sign-up fields.
- `FileService`: For handling file uploads in the event chat.

## 4. In-Depth Breakdown

- **`getUpcomingEventsForUser(...)`**: `GET /` - Retrieves the list of upcoming events, enriched with the current user''s specific status (qualified, signed-up, assigned) for each.
- **`getEventDetails(...)`**: `GET /{id}` - Fetches all detailed information for a single event.
- **`signUpForEvent(...)`**: `POST /{id}/signup` - Handles the user sign-up process, including saving any responses to custom fields.
- **`signOffFromEvent(...)`**: `POST /{id}/signoff` - Handles the user sign-off process.
- **`getEventCustomFields(...)`**: `GET /{id}/custom-fields` - Fetches the custom questions for an event''s sign-up form.
- **`uploadEventChatFile(...)`**: `POST /{eventId}/chat/upload` - Handles file uploads specifically for the event chat.'),

('src/main/java/de/api/v1/public_api/PublicFeedbackResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public API endpoints for the feedback system. It allows users to submit general feedback and event-specific feedback, and to view their own submission history.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/feedback` and `/feedback/event/:eventId` pages.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `FeedbackSubmissionDAO`, `EventFeedbackDAO`: The DAOs for the two types of feedback.

## 4. In-Depth Breakdown

- **`getMyFeedbackSubmissions(...)`**: `GET /user` - Fetches the history of general feedback submissions for the current user.
- **`submitGeneralFeedback(...)`**: `POST /general` - Submits a new general feedback entry.
- **`getEventFeedbackForm(...)`**: `GET /forms` - Retrieves the necessary data to render the event-specific feedback form, including a check to see if the user has already submitted it.
- **`submitEventFeedback(...)`**: `POST /event` - Submits a user''s rating and comments for a specific event. **This includes a security check** to ensure a user can only submit feedback for events they were actually assigned to.'),

('src/main/java/de/api/v1/public_api/PublicFilesResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a public API endpoint for fetching the list of downloadable files.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/dateien` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `FileDAO`: The DAO used to retrieve files.

## 4. In-Depth Breakdown

- **`getFiles(...)`**: `GET /` - Fetches all files, grouped by category. The DAO method automatically filters the results based on the current user''s role, so non-admins will not see admin-only files in the list.'),

('src/main/java/de/api/v1/public_api/PublicFileStreamResource.java',
'## 1. File Overview & Purpose

This `@RestController` is a specialized controller for securely streaming file content (downloads and images) to the client. It acts as a secure gateway to the file system, performing validation and authorization before serving a file.

## 2. Architectural Role

This is a critical component of the **Web/API Tier** that bridges the gap to the physical **File System**. It handles byte-stream responses rather than JSON.

## 3. Key Dependencies & Libraries

- **Spring Web (`ResponseEntity<Resource>`)**: The core components for streaming file data.
- **DAOs (`FileDAO`, `AttachmentDAO`, etc.)**: Used to get file metadata and perform authorization checks (which are currently stubbed but would be implemented here).

## 4. In-Depth Breakdown

- **`downloadFile(...)`**: `GET /download/{id}` - The endpoint for general file downloads. It retrieves the file''s metadata from the database, then calls the `serveFile` helper.
- **`getImage(...)`**: `GET /images/{filename:.+}` - The endpoint for serving images. The `:.+` is a regex to ensure the filename extension is correctly captured.
- **`serveFile(...)` (private helper)**: This is the core security method.
    1.  It resolves the requested file path against the configured base upload directory.
    2.  **Path Traversal Protection**: It crucially checks that the final, resolved path is still *inside* the base upload directory. This prevents attacks where a user might try to access files outside this directory (e.g., `../../../../etc/passwd`).
    3.  It sets the appropriate `Content-Type` and `Content-Disposition` headers (`attachment` for downloads, `inline` for images) and streams the file''s bytes to the response.'),

('src/main/java/de/api/v1/public_api/PublicMeetingResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public API endpoints for user interactions with `Meeting` entities (training sessions).

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/lehrgaenge` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `MeetingDAO`, `MeetingAttendanceDAO`: For all database operations.

## 4. In-Depth Breakdown

- **`getUpcomingMeetings(...)`**: `GET /` - Retrieves the list of upcoming meetings, enriched with the current user''s sign-up status for each.
- **`handleMeetingAction(...)`**: `POST /{id}/{action}` - A single endpoint that handles both signing up (`action=signup`) and signing off (`action=signoff`) from a meeting.'),

('src/main/java/de/api/v1/public_api/PublicProfileResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the API endpoints for managing a user''s own profile. It handles fetching all data for the profile page, submitting change requests, and updating preferences like theme and password.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/profil` page and related pages like `/passwort`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- **Various DAOs**: It injects a wide range of DAOs to aggregate all the necessary data for a user''s profile (event history, qualifications, achievements, etc.).
- `ProfileRequestService`: To handle the logic of creating a profile change request.

## 4. In-Depth Breakdown

- **`getMyProfile()`**: `GET /` - Fetches all data for the currently authenticated user''s profile page.
- **`getUserProfile(...)`**: `GET /{userId}` - Fetches a public-safe subset of another user''s profile data. This is used for the "Crew Card" modal in the Team Directory.
- **`requestProfileChange(...)`**: `POST /request-change` - Submits a request for an admin to approve changes to the user''s profile.
- **`updateUserTheme(...)`**: `PUT /theme` - Updates the user''s theme preference.
- **`updateChatColor(...)`**: `PUT /chat-color` - Updates the user''s preferred chat message color.
- **`updatePassword(...)`**: `PUT /password` - Handles the secure password change process, including validating the current password and the new password policy.
- **`updateDashboardLayout(...)`**: `PUT /dashboard-layout` - Saves the user''s custom widget layout for their dashboard.'),

('src/main/java/de/api/v1/public_api/PublicSearchResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a single API endpoint for the global, site-wide search functionality.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/suche` page. It delegates the search logic to the `SearchService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `SearchService`: The service that orchestrates the search across multiple DAOs.

## 4. In-Depth Breakdown

- **`search(...)`**: `GET /` - Takes a `query` parameter. It includes a basic validation to ensure the query is at least 3 characters long. It then calls the `searchService` to perform the multi-table search and returns the aggregated results.'),

('src/main/java/de/api/v1/public_api/PublicStorageDetailsResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides public API endpoints for retrieving detailed information about a single `StorageItem`, including its history and future reservations.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/lager/details/:itemId` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `StorageDAO`, `StorageLogDAO`, `MaintenanceLogDAO`: The DAOs used to fetch all the detailed data for an item.
- `StorageItemRelationService`: The service used to fetch related items.

## 4. In-Depth Breakdown

- **`getStorageItemById(...)`**: `GET /{itemId}` - Retrieves the core details of a single item.
- **`getStorageItemHistory(...)`**: `GET /{itemId}/history` - Fetches both the transaction log and the maintenance log for the item.
- **`getStorageItemReservations(...)`**: `GET /{itemId}/reservations` - Fetches a list of all future events for which this item is reserved.
- **`getRelatedItems(...)`**: `GET /{itemId}/relations` - Fetches the list of items that are defined as "related" to this one.'),

('src/main/java/de/api/v1/public_api/PublicStorageResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public API endpoints for general inventory interaction. It handles fetching the data for the main `/lager` page and processing check-in/checkout transactions.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/lager` page and the `/lager/qr-aktion/:itemId` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `StorageService`: Contains the transactional business logic for processing check-ins/checkouts and creating damage reports.
- `StorageDAO`, `EventDAO`: Used to fetch the necessary data for the page view.

## 4. In-Depth Breakdown

- **`getStoragePageData()`**: `GET /` - Fetches all inventory items (grouped by location) and the list of active events to populate the main inventory page and its modals.
- **`performTransaction(...)`**: `POST /transactions` - The endpoint for the multi-item cart system. It receives a single transaction request and passes it to the `StorageService` to be processed atomically.
- **`reportDamage(...)`**: `POST /{itemId}/report-damage` - Allows a user to submit a damage report for an item.'),

('src/main/java/de/api/v1/public_api/PublicTrainingRequestResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public API endpoints for the user-initiated training request feature. It allows users to submit new requests and register their interest in existing ones.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the "Request a New Course" feature on the `/lehrgaenge` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `TrainingRequestService`: The service that handles the business logic for creating requests.

## 4. In-Depth Breakdown

- **`submitRequest(...)`**: `POST /` - Creates a new training request based on a user-submitted topic.
- **`registerInterest(...)`**: `POST /{id}/interest` - Allows a user to "upvote" an existing training request, incrementing its interest counter.');
COMMIT;