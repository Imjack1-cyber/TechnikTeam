-- Flyway migration V73, Part 13: Overhaul Technical Wiki Documentation (Frontend Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/pages/AnnouncementsPage.jsx',
'## 1. File Overview & Purpose

This React component renders the **Digital Bulletin Board** (`/bulletin-board`). It fetches and displays all announcements created by administrators.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses the `useApi` hook to call the `/api/v1/public/announcements` endpoint to retrieve all announcement posts.
- **Rendering:** It iterates over the fetched announcements and displays each one in a `card` element.
- **Markdown Support:** It uses the `react-markdown` library to safely render the content of each announcement, allowing for rich text formatting.

## 4. State Management

- The component''s state (announcements, loading, error) is managed by the `useApi` custom hook.'),

('frontend/src/pages/CalendarPage.jsx',
'## 1. File Overview & Purpose

This React component renders the main **Calendar** page (`/kalender`). It displays all upcoming events and training meetings in two different, responsive views.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses the `useApi` hook to call `/api/v1/public/calendar/entries` to get a unified list of all schedulable items.
- **iCal Subscription:** Provides a direct link to `/api/v1/public/calendar.ics` for users to subscribe to the feed in their external calendar applications.
- **Responsive Views:**
    - **Mobile:** Renders the `CalendarMobileView` component, which displays a simple, chronological list of upcoming dates.
    - **Desktop:** Renders the `CalendarDesktopView` component, which displays a traditional, interactive month-grid calendar.
- **CSS Media Queries:** Plain CSS is used to toggle the visibility of the mobile vs. desktop views based on screen width.

## 4. State Management

- The component''s state is managed by the `useApi` hook.'),

('frontend/src/pages/ChangelogPage.jsx',
'## 1. File Overview & Purpose

This React component renders the **Changelogs** page (`/changelogs`). It displays a historical list of all application updates and new features.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses the `useApi` hook to call `/api/v1/public/changelog` to get all entries.
- **Rendering:** It maps over the changelog entries, displaying the version, title, release date, and notes for each in a separate `card`.
- **Markdown Support:** Uses `react-markdown` to render the detailed notes for each version.

## 4. State Management

- The component''s state is managed by the `useApi` hook.'),

('frontend/src/pages/ChatPage.jsx',
'## 1. File Overview & Purpose

This React component renders the main **Chat** interface (`/chat` and `/chat/:conversationId`). It acts as a layout container for the `ConversationList` and `MessageView` components, managing their responsive behavior.

## 2. Architectural Role

This is a **View** and **Layout** component in the frontend application.

## 3. Key Features & Logic

- **Dynamic Layout:** The core logic of this component is to manage which pane (conversation list or message view) is visible on mobile devices.
- **Routing:** It uses the `useParams` hook from React Router to detect if a `conversationId` is present in the URL.
- **CSS-Driven Swapping:** The presence of a `conversationId` adds a CSS class (`message-view-visible`) to the main container. This class uses CSS transforms to slide the conversation list out of view and the message view into view, creating a smooth mobile navigation experience.
- **Component Rendering:**
    - If a `conversationId` is present, it renders the `MessageView` component, passing the ID as a prop.
    - If no `conversationId` is present, it displays a "Welcome" message in the main pane.
    - The `ConversationList` is always rendered but may be hidden by CSS on mobile.

## 4. State Management

- The component''s layout state is derived directly from the URL parameters via React Router.'),

('frontend/src/pages/DashboardPage.jsx',
'## 1. File Overview & Purpose

This React component renders the user''s main **Dashboard** (`/home`). It displays a personalized overview of the user''s current status, tasks, and relevant upcoming events.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses the `useApi` hook to call the aggregated `/api/v1/public/dashboard` endpoint, which provides all necessary data in a single request.
- **Personalized Widgets:**
    - **Recommended Events:** A prominent widget that shows events the user is qualified for but not yet signed up for.
    - **Assigned Events:** Shows the user''s next confirmed assignments.
    - **Open Tasks:** Lists tasks assigned to the user from currently running events.
    - **Upcoming Events:** A general list of other events.
- **Conditional Rendering:** Widgets are only displayed if they contain data (e.g., the "Recommended" widget only appears if there are recommendations).

## 4. State Management

- The component''s state (dashboard data, loading, error) is managed by the `useApi` hook.
- It also reads the current `user` object from the `useAuthStore` to display a personalized welcome message.');
COMMIT;