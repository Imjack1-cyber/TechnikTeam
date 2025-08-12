-- Flyway migration V71, Part 11: Overhaul Technical Wiki Documentation (Backend API DTOs)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/api/v1/public_api/PublicAnnouncementResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public-facing API endpoint for viewing announcements from the Digital Bulletin Board. It is the backend for the `/bulletin-board` page.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It handles read-only requests for announcement data and delegates to the `AnnouncementService`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `AnnouncementService`: The service used to fetch all announcements.

## 4. In-Depth Breakdown

- **`getAllAnnouncements()`**: `GET /` - Fetches all announcements from the service and returns them to the client. The service/DAO layer ensures they are ordered newest first.'),

('src/main/java/de/api/v1/public_api/PublicCalendarEntriesResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a public API endpoint that serves a combined list of upcoming events and meetings, formatted specifically for use in a client-side calendar component like FullCalendar.js or the custom calendar view in this application.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the data source for the `/kalender` page. It aggregates data from both the `EventDAO` and `MeetingDAO`.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `EventDAO`, `MeetingDAO`: The DAOs used to fetch all upcoming calendar entries.

## 4. In-Depth Breakdown

- **`getCalendarEntries()`**: `GET /entries` - The main method.
    1.  It fetches all active/upcoming `Event` objects.
    2.  It fetches all upcoming `Meeting` objects.
    3.  It iterates through both lists, transforming each object into a standardized `Map` with common keys (`id`, `title`, `start`, `end`, `type`, `url`). This creates a uniform structure that the frontend can easily consume to render different types of calendar entries.'),

('src/main/java/de/api/v1/public_api/PublicCalendarResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a public endpoint that generates and serves an **iCalendar (.ics) file**. This allows users to subscribe to the application''s schedule with their personal calendar applications (e.g., Google Calendar, Outlook).

## 2. Architectural Role

This is a specialized component of the **Web/API Tier**. Instead of JSON, it produces a `text/calendar` response.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `EventDAO`, `MeetingDAO`: The DAOs used to fetch the event and meeting data.
- **iCal4j**: A third-party library used to programmatically construct the `.ics` file format.

## 4. In-Depth Breakdown

- **`getICalendarFeed(...)`**: `GET /calendar.ics` - The main method.
    1.  It creates a new iCal4j `Calendar` object.
    2.  It fetches all upcoming events and meetings from the DAOs.
    3.  For each event and meeting, it creates a `VEvent` component and populates it with standard iCalendar properties like `SUMMARY` (title), `DTSTART` (start time), `DTEND` (end time), `LOCATION`, `DESCRIPTION`, and a `URL` that links back to the details page within the application.
    4.  It uses a `CalendarOutputter` to serialize the `Calendar` object into the correct `.ics` format.
    5.  It sets the appropriate HTTP headers (`Content-Type: text/calendar`) and streams the file content to the client.'),

('src/main/java/de/api/v1/public_api/PublicChangelogResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the public-facing API endpoints for the `Changelog` feature. It allows users to view changelogs and interact with the "seen" status.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It serves the `/changelogs` page and the "What''s New" modal.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ChangelogDAO`: The DAO for all changelog-related database operations.

## 4. In-Depth Breakdown

- **`getAllPublishedChangelogs()`**: `GET /` - Retrieves all changelogs.
- **`getLatestUnseenChangelog(...)`**: `GET /latest-unseen` - A specific endpoint that finds the most recent changelog the currently logged-in user has not yet marked as seen. This is used to trigger the "What''s New" modal.
- **`markAsSeen(...)`**: `POST /{id}/mark-seen` - Updates the database to record that the current user has seen a specific changelog entry.'),

('src/main/java/de/api/v1/public_api/PublicChatResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides the primary API endpoints for the direct messaging and group chat feature. It handles fetching conversations and messages, creating new conversations, and managing participants. Real-time messaging itself is handled by the `ChatWebSocketHandler`.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It is the backend for the `/chat` page, providing the data needed to render the conversation list and message history.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining REST endpoints.
- `ChatDAO`: The DAO for all chat-related database operations.
- `FileService`: Used to handle file uploads within chats.

## 4. In-Depth Breakdown

- **`getConversations(...)`**: `GET /conversations` - Retrieves the conversation list for the current user.
- **`getConversationById(...)`**: `GET /conversations/{id}` - Fetches detailed information for a single conversation, including its participant list.
- **`getMessages(...)`**: `GET /conversations/{id}/messages` - Retrieves the message history for a conversation.
- **`findOrCreateConversation(...)`**: `POST /conversations` - Creates a new 1-on-1 conversation or finds the existing one between two users.
- **`createGroupConversation(...)`**: `POST /conversations/group` - Creates a new group chat.
- **`addParticipants(...)`**: `POST /conversations/{id}/participants` - Adds new members to an existing group chat.
- **`uploadChatFile(...)`**: `POST /upload` - Handles file uploads for chat, storing the file and returning its metadata.'),

('src/main/java/de/api/v1/public_api/PublicDashboardResource.java',
'## 1. File Overview & Purpose

This `@RestController` provides a single, aggregated API endpoint for the main user dashboard (`/home` page). It fetches all the personalized data required for the dashboard widgets.

## 2. Architectural Role

This is a component of the **Web/API Tier**. It serves as the data source for the `DashboardPage.jsx` component.

## 3. Key Dependencies & Libraries

- **Spring Web**: For defining the REST endpoint.
- `EventDAO`, `EventTaskDAO`: The DAOs used to fetch the user''s assigned events, open tasks, and recommended events.

## 4. In-Depth Breakdown

- **`getDashboardData(...)`**: `GET /` - The main method. It makes several calls to the DAOs to fetch different slices of data for the current user and bundles them into a single JSON response object for efficiency.');
COMMIT;