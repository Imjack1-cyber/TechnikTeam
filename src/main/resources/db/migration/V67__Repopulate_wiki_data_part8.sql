-- Flyway migration V67, Part 8: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/api/v1/AdminRequestResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative endpoints for managing user-submitted `ProfileChangeRequest`s. It allows admins to view pending requests and to either approve or deny them.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/requests` page. It delegates the transactional logic of approving/denying requests to the `ProfileRequestService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ProfileRequestService`: The service containing the business logic for processing change requests.
- `ProfileChangeRequestDAO`: Used by the service and controller for database access.

## 4. In-Depth Breakdown

- **`getPendingRequests()`**: `GET /pending` - Fetches all requests with a "PENDING" status for display in the admin UI.
- **`approveRequest(...)`**: `POST /{id}/approve` - Approves a request. The service handles updating the user''s profile, marking the request as "APPROVED", and sending notifications.
- **`denyRequest(...)`**: `POST /{id}/deny` - Denies a request, which simply updates its status in the database.'),

('src/main/java/de/api/v1/AdminStorageRelationsResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative API for managing relationships between `StorageItem`s. It is the backend for the "Related Items" management modal in the admin storage interface.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is scoped under `/api/v1/admin/storage/{itemId}/relations`. It delegates all logic to the `StorageItemRelationService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `StorageItemRelationService`: The service that handles the transactional logic of updating item relationships.

## 4. In-Depth Breakdown

- **`getRelatedItems(...)`**: `GET /` - Fetches the list of items currently related to the specified `itemId`.
- **`updateRelatedItems(...)`**: `PUT /` - Takes a list of `relatedItemIds` and completely overwrites the existing relationships for the specified `itemId`.'),

('src/main/java/de/api/v1/AdminTrainingRequestResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative endpoints for managing user-submitted `TrainingRequest`s. It allows admins to view and delete these requests.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/training-requests` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `TrainingRequestService`: The service for handling the business logic of training requests.

## 4. In-Depth Breakdown

- **`getAllRequests()`**: `GET /` - Retrieves all training requests, including a count of how many users are interested in each.
- **`deleteRequest(...)`**: `DELETE /{id}` - Deletes a training request.'),

('src/main/java/de/api/v1/AdminVenueResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `Venue` entities. It handles multipart requests for creating/updating venues with an optional map image upload.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/admin/venues` page.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints and handling multipart data.
- `VenueDAO`: The DAO for all database operations related to venues.
- `FileService`: Used to handle the storage of the uploaded map image.

## 4. In-Depth Breakdown

- **`getAllVenues()`**: `GET /` - Retrieves all defined venues.
- **`createVenue(...)`**: `POST /` - Creates a new venue. It takes a JSON part for the venue data and an optional file part for the map image.
- **`updateVenue(...)`**: `PUT /{id}` - Updates an existing venue.
- **`deleteVenue(...)`**: `DELETE /{id}` - Deletes a venue.'),

('src/main/java/de/api/v1/ChecklistResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the API endpoints for interacting with an event''s inventory checklist. This is a user-facing API, used by participants of an event on the `EventDetailsPage`.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is scoped under `/api/v1/events/{eventId}/checklist`. It delegates all database logic to the `ChecklistDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ChecklistDAO`: The DAO for all checklist-related database operations.

## 4. In-Depth Breakdown

- **`getChecklist(...)`**: `GET /` - Retrieves all checklist items for the specified event.
- **`generateChecklist(...)`**: `POST /generate` - A utility endpoint that populates or refreshes the event''s checklist based on the items currently reserved for that event.
- **`updateStatus(...)`**: `PUT /{checklistItemId}/status` - Updates the status of a single checklist item (e.g., from "PENDING" to "PACKED_OUT").'),

('src/main/java/de/api/v1/CourseResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `Course` templates. It is the backend for the `/admin/lehrgaenge` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates all database operations directly to the `CourseDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `CourseDAO`: The DAO for all database operations related to course templates.

## 4. In-Depth Breakdown

- **`getAllCourses()`**: `GET /` - Retrieves all course templates.
- **`createCourse(...)`**: `POST /` - Creates a new course template.
- **`updateCourse(...)`**: `PUT /{id}` - Updates an existing template.
- **`deleteCourse(...)`**: `DELETE /{id}` - Deletes a template.'),

('src/main/java/de/api/v1/KitResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the administrative CRUD endpoints for managing `InventoryKit` entities. It is the backend for the `/admin/kits` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It delegates all database operations directly to the `InventoryKitDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `InventoryKitDAO`: The DAO for all database operations related to inventory kits.

## 4. In-Depth Breakdown

- **`getAllKits()`**: `GET /` - Retrieves all kits, including their item contents.
- **`createKit(...)`**: `POST /` - Creates a new kit.
- **`updateKit(...)`**: `PUT /{id}` - Updates a kit''s metadata (name, description, etc.). Note: Updating kit *items* is handled by a separate endpoint.
- **`deleteKit(...)`**: `DELETE /{id}` - Deletes a kit.');
COMMIT;