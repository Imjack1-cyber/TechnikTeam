-- Flyway migration V61, Part 5: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/model/Announcement.java',
'## 1. File Overview & Purpose

This is a Plain Old Java Object (POJO) that serves as the data model for an announcement on the Digital Bulletin Board. It represents a record from the `announcements` table and includes fields for its title, content, author, and creation date.

## 2. Architectural Role

This class is part of the **Model Tier**. It is used to transfer announcement data between the `AnnouncementDAO`, `AnnouncementService`, and the API controllers (`AdminAnnouncementResource`, `PublicAnnouncementResource`).

## 3. Key Dependencies & Libraries

- `java.time.LocalDateTime`: Used for the `createdAt` timestamp.

## 4. In-Depth Breakdown

- **Fields:**
    - `id`: The primary key.
    - `title`: The user-visible title of the announcement.
    - `content`: The main body of the announcement, which supports Markdown formatting.
    - `authorUserId`: The ID of the admin who created the post.
    - `createdAt`: The timestamp of when the post was created.
- **Transient Fields:**
    - `authorUsername`: Populated by a JOIN in the DAO to display the author''s name without needing a separate query.'),

('src/main/java/de/model/EventRole.java',
'## 1. File Overview & Purpose

This POJO represents a reusable, predefined role that can be assigned to a user within the context of a specific event (e.g., "Audio Tech", "Lighting Operator"). It corresponds to a record in the `event_roles` table.

## 2. Architectural Role

This class is part of the **Model Tier**. It is managed via the `AdminEventRoleResource` and used in the `Event` model to structure the assigned team list.

## 3. Key Dependencies & Libraries

- `java.time.LocalDateTime`: For the `createdAt` timestamp.

## 4. In-Depth Breakdown

- **Fields:**
    - `id`: The primary key.
    - `name`: The user-visible name of the role.
    - `description`: A brief explanation of the role''s responsibilities.
    - `iconClass`: A FontAwesome CSS class for a representative icon.
    - `createdAt`: The timestamp of when the role was defined.'),

('src/main/java/de/model/EventPhoto.java',
'## 1. File Overview & Purpose

This POJO represents the metadata for a single photo uploaded to an event''s gallery. It corresponds to a record in the `event_photos` junction table, linking a `File` record to an `Event` record.

## 2. Architectural Role

This class is part of the **Model Tier**. It is used to transfer photo metadata between the `EventPhotoDAO`, `EventGalleryService`, and the `PublicEventGalleryResource` API.

## 3. Key Dependencies & Libraries

- `java.time.LocalDateTime`: For the `uploadedAt` timestamp.

## 4. In-Depth Breakdown

- **Fields:**
    - `id`: The primary key of the gallery entry.
    - `eventId`: Foreign key to the `events` table.
    - `fileId`: Foreign key to the `files` table, pointing to the actual image file record.
    - `uploaderUserId`: The ID of the user who uploaded the photo.
    - `caption`: An optional user-provided caption for the photo.
- **Transient Fields:**
    - `filepath`: The path to the image file, populated by a JOIN for easy access.
    - `uploaderUsername`: The name of the uploader, populated by a JOIN.'),

('src/main/java/de/model/PageDocumentation.java',
'## 1. File Overview & Purpose

This POJO represents a single user-facing help page. It corresponds to a record in the `page_documentation` table and contains all the structured content needed to render a complete help article, including its title, features, and links to related help pages and technical wiki articles.

## 2. Architectural Role

This class is part of the **Model Tier**. It is managed via the `AdminDocumentationResource` and displayed to users via the `PublicDocumentationResource` and the `/help` pages.

## 3. Key Dependencies & Libraries

- `java.time.LocalDateTime`: For timestamps.

## 4. In-Depth Breakdown

- **Fields:**
    - `pageKey`: A unique, URL-safe string identifier for the page (e.g., "admin_users").
    - `title`: The human-readable title of the help page.
    - `pagePath`: The actual frontend route that this page documents (e.g., "/admin/mitglieder").
    - `features`: A Markdown-enabled text block describing the page''s functionality.
    - `relatedPages`: A JSON string containing an array of `pageKey`s for other relevant help pages.
    - `adminOnly`: A boolean flag to restrict visibility of the help page to admins.
    - `wikiEntryId`: A nullable foreign key linking this help page to a more detailed technical article in the `wiki_documentation` table.
- **Transient Fields:**
    - `wikiLink`: A URL string generated on the backend to provide a direct link to the associated wiki page.'),

('src/main/java/de/model/TrainingRequest.java',
'## 1. File Overview & Purpose

This POJO represents a user-initiated request for a new training course. It corresponds to a record in the `training_requests` table and captures the requested topic and the original requester.

## 2. Architectural Role

This class is part of the **Model Tier**. It is used to transfer data about training requests between the DAOs, services, and the relevant public and admin API resources.

## 3. Key Dependencies & Libraries

- `java.time.LocalDateTime`: For the `createdAt` timestamp.

## 4. In-Depth Breakdown

- **Fields:**
    - `id`: Primary key.
    - `topic`: The user-submitted topic for the desired training.
    - `requesterUserId`: The ID of the user who created the request.
- **Transient Fields:**
    - `requesterUsername`: The name of the user who created the request, populated by a JOIN.
    - `interestCount`: The total number of users who have registered interest in this topic, calculated via a subquery in the DAO.');
COMMIT;