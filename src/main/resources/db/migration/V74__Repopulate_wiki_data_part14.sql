-- Flyway migration V74, Part 14: Overhaul Technical Wiki Documentation (Frontend Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/pages/EventDetailsPage.jsx',
'## 1. File Overview & Purpose

This is a large, multi-functional React component that renders the detailed view for a single event (`/veranstaltungen/details/:eventId`). It serves as a central hub for all event-related information and real-time interaction.

## 2. Architectural Role

This is a complex **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch all initial event data from `/api/v1/public/events/:id`.
- **Tabbed Interface:** Manages the state for switching between different information tabs:
    - **Tasks:** Displays a list of tasks for the event, including their status and assigned users.
    - **Inventory Checklist:** Renders the `ChecklistTab` component for managing equipment load-in/load-out.
    - **Event Chat:** Contains the real-time chat interface for event participants.
    - **Gallery:** (Visible only for completed events) Renders the `EventGalleryTab` component.
- **Real-Time Chat:**
    - Uses the `useWebSocket` hook to connect to the `/ws/chat/:eventId` endpoint.
    - Manages the state of chat messages, receiving new messages and updates (edits/deletions) via the WebSocket connection.
    - Handles sending new messages, including text and file uploads.
- **Authorization:** It reads the `user` object from the `useAuthStore` to determine if the current user has permission to perform actions like deleting chat messages or viewing the admin-only debriefing link.

## 4. State Management

- **Event Data:** Managed by the `useApi` hook.
- **UI State:** Uses `useState` for managing the `activeTab`, chat input, and other UI-related state.
- **Chat Messages:** Managed via `useState`, with updates pushed from the `useWebSocket` hook.
- **Global State:** Accesses the `user` object from `useAuthStore` for permissions.'),

('frontend/src/pages/EventFeedbackPage.jsx',
'## 1. File Overview & Purpose

This React component renders the dedicated form for submitting post-event feedback (`/feedback/event/:eventId`).

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to call `/api/v1/public/feedback/forms?eventId=...`. This endpoint provides the event details and checks if the user has already submitted feedback for this event.
- **Conditional Rendering:** If the user has already submitted feedback, it displays a "Thank You" message instead of the form.
- **Controlled Form:** Uses `useState` to manage the state of the star rating and the comments textarea.
- **Submission:** On form submit, it calls the `POST /api/v1/public/feedback/event` endpoint via `apiClient` to save the user''s response.

## 4. State Management

- **Form Data:** Managed via `useState` hooks.
- **Initial Data:** Fetched using the `useApi` hook.'),

('frontend/src/pages/EventsPage.jsx',
'## 1. File Overview & Purpose

This React component renders the main **Events** listing page (`/veranstaltungen`). It displays all upcoming events and allows users to sign up or sign off.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to call `/api/v1/public/events`, which returns a list of events enriched with the current user''s status (`userAttendanceStatus`) and qualification (`isUserQualified`) for each.
- **Action Buttons:** Conditionally renders a "Sign Up" or "Sign Off" button based on the `userAttendanceStatus` for each event. The "Sign Up" button is disabled if `isUserQualified` is false.
- **Sign-Up Modal:**
    - Before showing the sign-up modal, it makes an API call to `/api/v1/public/events/:id/custom-fields` to fetch any additional questions required for the sign-up.
    - The modal dynamically renders input fields for these custom questions.
- **Sign-Off Modal:** Shows a confirmation dialog before the user signs off.
- **Responsive View:** The component includes both a desktop table view (`desktop-table-wrapper`) and a mobile card view (`mobile-card-list`), with CSS media queries controlling which one is visible.

## 4. State Management

- **Event List:** Managed by the `useApi` hook.
- **Modal State:** Uses `useState` to control the visibility and content of the sign-up/sign-off modals.'),

('frontend/src/pages/FeedbackPage.jsx',
'## 1. File Overview & Purpose

This React component renders the **General Feedback** page (`/feedback`). It provides a form for users to submit general suggestions or bug reports and displays a history of their own past submissions.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to call `/api/v1/public/feedback/user` to retrieve the list of the current user''s past submissions.
- **Controlled Form:** The submission form for new feedback is a controlled component managed with `useState`.
- **Submission:** On submit, it calls `POST /api/v1/public/feedback/general` via `apiClient`.
- **Submission History:** It maps over the fetched submissions and displays each one with its current status (e.g., "NEW", "PLANNED").

## 4. State Management

- **Submission History:** Managed by the `useApi` hook.
- **New Feedback Form:** Managed by `useState` hooks.'),

('frontend/src/pages/FilesPage.jsx',
'## 1. File Overview & Purpose

This React component renders the public **Files & Documents** page (`/dateien`). It fetches and displays a categorized list of all files the current user is permitted to see.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to call `/api/v1/public/files`. The backend automatically filters out admin-only files for non-admin users. The data arrives grouped by category.
- **Rendering:** It iterates over the categories (the keys of the data object) and then iterates over the files within each category, rendering a download link for each file.

## 4. State Management

- The component''s state is managed by the `useApi` hook.'),

('frontend/src/pages/HelpDetailsPage.jsx',
'## 1. File Overview & Purpose

This component renders the detailed view for a single help article (`/help/:pageKey`). It displays the features, use cases, and links related to a specific page of the application.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:**
    - Uses `useApi` to fetch the specific documentation entry based on the `pageKey` from the URL via `/api/v1/public/documentation/:pageKey`.
    - Makes a second `useApi` call to fetch *all* documentation entries to resolve the titles for the "Related Pages" links.
- **Content Rendering:**
    - Uses `react-markdown` to render the `features` content.
- **Link Generation:**
    - Provides a direct link to the actual page being documented.
    - Provides a link to the associated technical wiki page, if one is defined.
- **Related Pages:** It parses the JSON array of related page keys and looks them up in the full list of docs to render a navigable list of related topics.

## 4. State Management

- All data is managed by two instances of the `useApi` hook.'),

('frontend/src/pages/HelpListPage.jsx',
'## 1. File Overview & Purpose

This component renders the main help page (`/help`), which acts as a "link tree" or table of contents for all user-facing documentation.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch all available documentation entries from `/api/v1/public/documentation`. The backend automatically filters out admin-only articles for non-admin users.
- **Grouping:** Uses `useMemo` to group the flat list of documentation pages into a nested object based on their `category` property.
- **Rendering:** It iterates over the grouped categories, creating a `card` for each category. Inside each card, it lists the links to the individual `HelpDetailsPage` for each article in that category.

## 4. State Management

- The component''s data is managed by the `useApi` hook.');
COMMIT;