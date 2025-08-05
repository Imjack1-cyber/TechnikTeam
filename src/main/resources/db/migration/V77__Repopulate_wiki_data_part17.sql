-- Flyway migration V77, Part 17: Overhaul Technical Wiki Documentation (Frontend Admin Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/pages/admin/AdminAnnouncementsPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative page for managing **Announcements** (`/admin/announcements`). It provides a full CRUD interface for the bulletin board.

## 2. Architectural Role

This is an **Admin View** component in the frontend.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get all announcements from `/api/v1/admin/announcements`.
- **Modal-Based Editing:**
    - Clicking "Neue Mitteilung" or "Bearbeiten" opens the `AnnouncementModal`.
    - The modal contains a controlled form for the title and content.
    - On submit, it calls either `POST` or `PUT` to the `/api/v1/admin/announcements` endpoint.
- **Deletion:** The "Löschen" button triggers a confirmation dialog and then calls `DELETE /api/v1/admin/announcements/:id`.
- **Markdown Preview:** It renders existing announcements using `react-markdown` to show admins what the final post will look like.

## 4. State Management

- **Announcement List:** Managed by the `useApi` hook.
- **Modal State:** Uses `useState` to control the modal''s visibility and which announcement is being edited.'),

('frontend/src/pages/admin/AdminCoursesPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative page for managing **Course Templates** (`/admin/lehrgaenge`). These are the blueprints for training sessions, not the specific dates.

## 2. Architectural Role

This is an **Admin View** component in the frontend.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get all course templates from `/api/v1/courses`.
- **CRUD Operations:** Provides a full CRUD interface via a modal (`Modal`).
    - **Create/Edit:** The modal contains a form for the course name, abbreviation, and description. It calls `POST` or `PUT` to `/api/v1/courses`.
    - **Delete:** A delete button with a confirmation dialog calls `DELETE /api/v1/courses/:id`.
- **Navigation:** Provides a "Meetings" button for each course that links to the `AdminMeetingsPage` for that specific course, allowing admins to schedule actual training dates.

## 4. State Management

- **Course List:** Managed by the `useApi` hook.
- **Modal State:** Managed by `useState`.'),

('frontend/src/pages/admin/AdminDashboardPage.jsx',
'## 1. File Overview & Purpose

This component renders the main **Admin Dashboard** (`/admin/dashboard`), providing administrators with a high-level overview of the application''s status.

## 2. Architectural Role

This is an **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to make a single call to the aggregated `/api/v1/admin/dashboard` endpoint.
- **Widget-Based Layout:** It uses the reusable `Widget` component to display different pieces of information in a grid.
- **Data Visualization:** It uses the `EventTrendChart` component (which wraps `react-chartjs-2`) to display a line chart of event frequency over the past 12 months.
- **Quick Links:** The widgets provide direct links to the relevant management pages (e.g., "Alle Events anzeigen").

## 4. State Management

- All dashboard data is managed by the `useApi` hook.'),

('frontend/src/pages/admin/AdminDocumentationPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative page for managing the user-facing **Page Documentation** (`/admin/documentation`). It allows admins to create, edit, and delete the help articles that appear on the `/help` pages.

## 2. Architectural Role

This is an **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:** It makes two parallel API calls:
    1.  `/api/v1/admin/documentation` to get the list of documentation pages to display in the main table.
    2.  `/api/v1/wiki/list` to get a flat list of all technical wiki articles, which is used to populate the "Technical Wiki Article" dropdown in the modal.
- **Modal-Based Editing:** All CRUD operations are handled through the `DocumentationModal`.
    - The modal form allows editing of all fields, including title, content (Markdown), category, and related pages.
    - A multi-select input is used for linking related help pages.
    - A dropdown is used for linking a help page to a technical wiki article.
- **Submission:** The modal calls the appropriate `POST`, `PUT`, or `DELETE` endpoints on `/api/v1/admin/documentation`.

## 4. State Management

- **Doc & Wiki Lists:** Managed by `useApi` hooks.
- **Modal State:** Managed by `useState`.'),

('frontend/src/pages/admin/AdminEventsPage.jsx',
'## 1. File Overview & Purpose

This component renders the main administrative page for **Event Management** (`/admin/veranstaltungen`). It provides a full CRUD interface for all events.

## 2. Architectural Role

This is a key **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:**
    - Uses `useApi` to get the list of all events from `/api/v1/events`.
    - It uses the `useAdminData` hook to pre-fetch all data needed for the creation/editing modal (lists of users, courses, and storage items). This prevents the modal from having to make its own data calls.
- **Modal-Based Editing (`EventModal`):**
    - The `EventModal` is a large, complex component with a tabbed interface ("General" and "Details & Requirements").
    - It uses the `DynamicSkillRows` and `DynamicItemRows` components to manage the lists of personnel and material requirements.
    - On submit, it constructs a `FormData` object to send both the JSON event data and an optional file upload in a single multipart request to the backend.
- **Actions:** Provides buttons for editing, cloning, deleting, and accessing the debriefing for each event.

## 4. State Management

- **Event List:** Managed by the `useApi` hook.
- **Form Data for Modal:** The `useAdminData` hook manages fetching this prerequisite data.
- **Modal State:** `useState` controls the modal''s visibility and which event is being edited.'),

('frontend/src/pages/admin/AdminMeetingsPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative page for managing specific **Meeting** dates for a given course (`/admin/lehrgaenge/:courseId/meetings`).

## 2. Architectural Role

This is an **Admin View** component.

## 3. Key Features & Logic

- **Contextual Data Fetching:** It uses the `courseId` from the URL parameters (via `useParams`) to fetch only the meetings for that specific course from `/api/v1/meetings?courseId=...`. It also fetches a list of all users to populate the "Leader" dropdown in the modal.
- **Modal-Based Editing:** A modal is used for creating and editing meetings.
- **Actions:** Provides buttons for editing, cloning, and deleting meeting dates.

## 4. State Management

- **Meeting List & User List:** Managed by `useApi` hooks.
- **Modal State:** Managed by `useState`.');
COMMIT;