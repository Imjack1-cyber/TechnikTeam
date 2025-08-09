-- Flyway migration V78, Part 18: Overhaul Technical Wiki Documentation (Frontend Admin Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/pages/admin/AdminStoragePage.jsx',
'## 1. File Overview & Purpose

This component renders the main administrative page for **Inventory Management** (`/admin/lager`). It provides a full CRUD interface for all `StorageItem` entities.

## 2. Architectural Role

This is a key **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get the list of all storage items from `/api/v1/storage`.
- **Modal-Based Editing (`StorageItemModal`):**
    - This is a multi-purpose modal that can switch between different "modes" (`create`, `edit`, `defect`, `repair`, `relations`).
    - The `edit` mode allows changing all core details of an item.
    - The `defect` and `repair` modes provide specialized forms for managing an item''s defective stock count.
    - The `relations` mode allows an admin to manage which other items are considered "related" to this one.
- **Actions:** The main table provides buttons for all actions: Edit, Defect, Repair, QR Code, and Delete.
- **QR Code Generation:** The "QR" button opens a modal that displays a QR code (generated using the `qrcode.react` library) which links to the public `/lager/qr-aktion/:itemId` page for that item.

## 4. State Management

- **Item List:** Managed by the `useApi` hook.
- **Modal State:** A single `useState` object manages the modal''s visibility, the item being edited, and the current `mode` of the modal.'),

('frontend/src/pages/admin/AdminSystemPage.jsx',
'## 1. File Overview & Purpose

This component renders the **System Information** page (`/admin/system`), which displays live statistics about the server''s health.

## 2. Architectural Role

This is an **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to periodically fetch data from the `/api/v1/system/stats` endpoint.
- **Data Display:** It displays metrics like CPU load, RAM usage, and disk space in a clean, readable format within `card` elements. Helper functions are used to format byte values into Gigabytes and numbers into percentages.

## 4. State Management

- All system statistics are managed by the `useApi` hook. The hook''s `reload` function could be called on an interval to create a live-updating dashboard.'),

('frontend/src/pages/admin/AdminTrainingRequestsPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative page for viewing and managing **User-Initiated Training Requests** (`/admin/training-requests`).

## 2. Architectural Role

This is an **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get all training requests from `/api/v1/admin/training-requests`. The backend enriches this data with a count of how many users have registered interest in each topic.
- **Data Display:** It shows a table of requested topics, who originally requested them, and how many other users are also interested.
- **Deletion:** Allows an admin to delete a request, for example, after a corresponding course has been created or if the request is deemed irrelevant.

## 4. State Management

- The list of requests is managed by the `useApi` hook.'),

('frontend/src/pages/admin/AdminUsersPage.jsx',
'## 1. File Overview & Purpose

This component renders the main **User Management** page (`/admin/mitglieder`). It provides a full CRUD interface for all `User` accounts.

## 2. Architectural Role

This is a key **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:**
    - Uses `useApi` to get the list of all users from `/api/v1/users`.
    - Uses the `useAdminData` hook to pre-fetch data needed for the `UserModal` (roles and permissions).
- **Modal-Based Editing (`UserModal`):**
    - The `UserModal` is a complex modal with tabs for "General", "Permissions", and "Admin Notes".
    - The "Permissions" tab uses the `PermissionsTab` component to render a nested, expandable checklist of all available permissions.
    - On submit, it calls the appropriate `POST` or `PUT` endpoint on `/api/v1/users`.
- **Password Reset:** The "Passwort Reset" button calls `POST /api/v1/users/:id/reset-password`. It then displays the new, temporary password in a separate modal for the admin to copy and securely transmit to the user.

## 4. State Management

- **User List & Prerequisite Data:** Managed by `useApi` and `useAdminData` hooks.
- **Modal State:** Managed with `useState` hooks.'),

('frontend/src/pages/admin/AdminVenuesPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative page for managing **Venues** (Veranstaltungsorte) at `/admin/venues`.

## 2. Architectural Role

This is an **Admin View** component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get all venues from `/api/v1/admin/venues`.
- **Modal-Based Editing:** Uses a modal to handle creating and editing venues.
- **File Upload:** The modal form includes a file input for uploading a map image. The `handleSubmit` function constructs a `FormData` object to send both the JSON venue data and the image file in a single multipart request.

## 4. State Management

- **Venue List:** Managed by the `useApi` hook.
- **Modal State:** Managed by `useState`.'),

('frontend/src/pages/admin/AdminWikiPage.jsx',
'## 1. File Overview & Purpose

This component renders the administrative interface for the **Technical Wiki** (`/admin/wiki`). It provides a two-pane layout with a file tree navigator and a content editor/viewer.

## 2. Architectural Role

This is a complex **Admin View** component that functions like a small single-page application itself.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get the hierarchical file tree structure from `/api/v1/wiki`.
- **File Tree Navigation:** It recursively renders the file tree structure. Clicking a file triggers an API call via `apiClient` to `/api/v1/wiki/:id` to fetch that specific page''s content.
- **Editing vs. Viewing Mode:**
    - It uses a local `isEditing` state to toggle between a read-only view and an editing view.
    - The read-only view uses `react-markdown` to render the content.
    - The editing view uses a simple `<textarea>` as a controlled component for the Markdown content.
- **CRUD Operations:**
    - **Save:** Calls `PUT /api/v1/wiki/:id` to save the content from the editor.
    - **Delete:** Calls `DELETE /api/v1/wiki/:id` to delete the current page.
    - **Create:** A "+" button opens a modal (`WikiPageModal`) to create a new page by specifying its filename under a parent path. This calls `POST /api/v1/wiki`.

## 4. State Management

- **File Tree:** Managed by the `useApi` hook.
- **Currently Selected Page & Content:** Managed by `useState` hooks.
- **UI State:** `useState` is used for `isEditing` and the new page modal visibility.');
COMMIT;