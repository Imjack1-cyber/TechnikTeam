-- Flyway migration V82, Part 22: Overhaul Technical Wiki Documentation (Frontend Components)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/components/events/ChecklistTab.jsx',
'## 1. File Overview & Purpose

This component renders the **Inventory Checklist** tab on the `EventDetailsPage`. It provides an interactive checklist for tracking equipment during event load-in and load-out.

## 2. Architectural Role

This is a **View** sub-component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch the initial checklist items for the event from `/api/v1/events/:id/checklist`.
- **Real-Time Updates:** Uses the `useWebSocket` hook to connect to `/ws/checklist/:id`. It listens for `checklist_update` events and updates the local state in real-time when another user changes an item''s status.
- **Status Changes:** When the user changes the status of an item in the dropdown, it sends a `PUT` request to `/api/v1/events/:id/checklist/:itemId/status`. The component''s state is then updated by the incoming WebSocket broadcast, ensuring consistency across all clients.
- **Generate from Reservations:** Provides a button that calls `POST /api/v1/events/:id/checklist/generate` to populate the checklist from the event''s material reservations.'),

('frontend/src/components/events/EventGalleryTab.jsx',
'## 1. File Overview & Purpose

This component renders the **Photo Gallery** tab on the `EventDetailsPage` for completed events. It displays uploaded photos and provides an interface for participants to upload new ones.

## 2. Architectural Role

This is a **View** sub-component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch all photo metadata for the event from `/api/v1/public/events/:id/gallery`.
- **Photo Upload:**
    - It conditionally renders an "Upload Photo" button only if the current user was a participant in the event.
    - The button opens a `PhotoUploadModal` which handles the file selection and submission to `POST /api/v1/public/events/:id/gallery`.
- **Photo Deletion:** It renders a delete button on each photo. This button is only visible if the current user is the uploader, an admin, or the event leader.
- **Lightbox:** Clicking on any photo opens it in a full-screen `Lightbox` view.

## 4. State Management

- **Photo List & Modals:** Managed by `useApi` and `useState` hooks.'),

('frontend/src/components/layout/ErrorLayout.jsx',
'## 1. File Overview & Purpose

This is a specialized **Layout Component** used by React Router to wrap all error pages (403, 404, 500). Its primary purpose is to apply a consistent, minimal theme and layout to these pages, ensuring they match the user''s selected theme even if the full application fails to render.

## 2. Architectural Role

This is a **Layout** component.

## 3. Key Features & Logic

- **Theme Application:** It directly accesses the `authStore` state via `getState()` to retrieve the user''s theme. It then sets the `data-theme` attribute on the `<html>` element. This is crucial because error pages render outside the main `<App />` component, so they don''t have access to the normal theme context.
- **Styling:** It wraps the error page content (rendered via `<Outlet />` or `children`) in a `<div class="error-page-wrapper">` which provides the necessary styling for the terminal-themed error pages.'),

('frontend/src/components/layout/Header.jsx',
'## 1. File Overview & Purpose

This component renders the **mobile-only header** that appears at the top of the screen on small viewports.

## 2. Architectural Role

This is a **Layout** component, part of the main `App` layout.

## 3. Key Features & Logic

- **Hamburger Menu:** It contains the hamburger menu button (`mobile-nav-toggle`). Clicking this button calls the `onNavToggle` function passed down from the `App` component, which manages the mobile sidebar''s open/closed state.
- **Logo & Profile Link:** It displays the application logo (which links home) and a link to the user''s profile page, showing their selected profile icon.'),

('frontend/src/components/layout/MinimalLayout.jsx',
'## 1. File Overview & Purpose

This is a specialized **Layout Component** for pages that should be displayed without the main application sidebar and header, such as the printable `PackKitPage` or the mobile-focused `QrActionPage`.

## 2. Architectural Role

This is a **Layout** component.

## 3. Key Features & Logic

- **Simplicity:** It simply renders the child route''s content (via `<Outlet />`) inside a basic, centered content wrapper. This provides a clean slate for pages that need a unique, uncluttered layout.'),

('frontend/src/components/layout/Sidebar.jsx',
'## 1. File Overview & Purpose

This component renders the main **sidebar navigation** for the application.

## 2. Architectural Role

This is a key **Layout** component, part of the main `App` layout.

## 3. Key Features & Logic

- **Data Source:** It reads the `user` object and the `navigationItems` array directly from the global `useAuthStore`. The `navigationItems` array is pre-filtered by the backend to only contain links the user is authorized to see.
- **Rendering:** It iterates through the `navigationItems` list and renders a `NavLink` for each. It separates the links into "User" and "Admin" sections.
- **Active Link Styling:** It uses the `isActive` property provided by `NavLink` to apply the `active-nav-link` class to the currently active route.
- **Global Search:** It includes the site-wide search bar. On submit, it navigates the user to the `/suche` page with the search term as a query parameter.
- **User Actions:** It displays the logged-in user''s name, a link to their profile, the logout button, and the `ThemeSwitcher` component.'),

('frontend/src/components/profile/ProfileAchievements.jsx',
'## 1. File Overview & Purpose

This is a presentational component that displays the **user''s earned achievements** on their profile page.

## 2. Architectural Role

This is a **View** sub-component, used within `ProfilePage`.

## 3. Key Features & Logic

- **Props-Based:** It receives the `achievements` array as a prop.
- **Rendering:** It maps over the array and displays each achievement in its own card, showing the icon, name, description, and the date it was earned.'),

('frontend/src/components/profile/ProfileDetails.jsx',
'## 1. File Overview & Purpose

This is a stateful component that manages the display and editing of a user''s **core profile data** on their profile page.

## 2. Architectural Role

This is a **View** and **Form** sub-component, used within `ProfilePage`.

## 3. Key Features & Logic

- **Controlled Form:** It manages the form data for email, class year, etc., in its local `useState`.
- **Change Request Flow:**
    1.  When the user clicks "Save," it doesn''t submit directly. Instead, it compares the current form data with the original `user` prop to detect what has changed.
    2.  It then opens a `ConfirmationModal`, showing the user a summary of their requested changes.
    3.  Only after the user confirms in this modal does it send the `POST` request to `/api/v1/public/profile/request-change`.
- **Update Propagation:** After a successful submission, it calls the `onUpdate` function (passed down from `ProfilePage`) to trigger a full refresh of the profile data.
- **Chat Color:** It also contains a separate, simple form for updating the user''s chat color, which calls the `PUT /api/v1/public/profile/chat-color` endpoint directly.');
COMMIT;