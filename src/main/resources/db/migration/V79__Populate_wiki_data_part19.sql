-- Flyway migration V79, Part 19: Overhaul Technical Wiki Documentation (Frontend Misc)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/App.jsx',
'## 1. File Overview & Purpose

This is the root component of the authenticated application. It sets up the main layout, including the `Sidebar` and `Header`, and provides the `<Outlet />` for React Router to render the content of the current route. It also manages global UI elements like the `ToastContainer` and the "What''s New" `ChangelogModal`.

## 2. Architectural Role

This is the main **Layout/Root** component for the authenticated part of the frontend application.

## 3. Key Features & Logic

- **Layout Structure:** Defines the primary HTML structure with the sidebar, a mobile header, the main content area, and an overlay for mobile navigation.
- **Mobile Navigation:** Manages the `isNavOpen` state to toggle the mobile sidebar and the overlay. It includes `useEffect` hooks to add/remove a class to the `<body>` tag and to automatically close the nav on route changes.
- **Changelog Modal:**
    - On mount, if the user is authenticated, it calls the `useCallback`-wrapped `fetchChangelog` function.
    - This function hits the `/api/v1/public/changelog/latest-unseen` endpoint.
    - If an unseen changelog is returned, it sets the state to display the `ChangelogModal`.
    - The modal''s close handler calls `POST /api/v1/public/changelog/:id/mark-seen` to ensure the modal doesn''t appear again for that user.
- **Warning Notifications:** It uses the `useNotifications` hook to listen for real-time "Warning" level notifications and displays the `WarningNotification` component when one is received.
- **Toast Notifications:** It includes the `ToastContainer` and wraps the entire layout in a `ToastProvider` to make the toast context available to all child components.

## 4. State Management

- **Local State (`useState`)**: Manages `isNavOpen`, `changelog`, and `isChangelogVisible`.
- **Custom Hooks**: Uses `useNotifications` to manage warning notifications.
- **Global State (`useAuthStore`)**: Checks `isAuthenticated` before fetching the changelog.
- **Context (`ToastProvider`)**: Provides the toast notification functionality to the entire app.'),

('frontend/src/main.jsx',
'## 1. File Overview & Purpose

This is the main entry point for the React application. Its primary responsibilities are to perform initial setup and then render the application into the DOM.

## 2. Architectural Role

This is the **Application Bootstrap** file for the frontend.

## 3. Key Features & Logic

- **`initializeApp()`**: An asynchronous function that runs before the React application is rendered.
    - **API Client Setup:** It injects the `logout` function from the `authStore` into the `apiClient`. This is a crucial step that allows the `apiClient` to automatically log out the user if it ever receives a 401 Unauthorized response from the backend.
    - **Session Fetching:** It calls `fetchUserSession()` from the `authStore`. This function makes a request to the `/api/v1/auth/me` endpoint to retrieve the current user''s data. This ensures that if a user refreshes the page, their session is re-established before any components are rendered.
- **Rendering:** After `initializeApp` completes, it uses `ReactDOM.createRoot().render()` to mount the `RouterProvider` (from React Router) into the `<div id="root"></div>` element in `index.html`.

## 4. State Management

- This file is the primary consumer of the `useAuthStore`. It directly calls `getState()` and `dispatch` actions (`fetchUserSession`) to initialize the application''s authentication state.'),

('frontend/src/pages/error/ErrorPage.jsx',
'## 1. File Overview & Purpose

This component renders a user-friendly, stylized **500 Internal Server Error** page. It is configured as the `errorElement` for the main application route in `router/index.jsx`, meaning it will be automatically displayed by React Router if an unhandled error occurs during rendering or data loading in any of the child routes.

## 2. Architectural Role

This is a specialized **View** component for error handling.

## 3. Key Features & Logic

- **Error Catching:** Uses the `useRouteError` hook from React Router to catch the error object that was thrown.
- **Stylized Display:** It renders its content inside a `<div class="terminal">` to create a "hacker terminal" aesthetic, making the error page more engaging than a standard blank page.
- **Typing Animation:** It uses the `useTypingAnimation` custom hook to display a series of diagnostic messages one character at a time, enhancing the terminal theme. The actual error message from the `useRouteError` hook is dynamically inserted into this animation.
- **Recovery Action:** Once the typing animation is complete, a "Zum Dashboard" button becomes visible, providing the user with a clear path to navigate away from the error page.

## 4. State Management

- The typing animation and its completion state are managed by the `useTypingAnimation` hook.'),

('frontend/src/pages/error/ForbiddenPage.jsx',
'## 1. File Overview & Purpose

This component renders a **403 Forbidden** error page (`/forbidden`). It is displayed when an authenticated user tries to access a resource they do not have the necessary permissions for.

## 2. Architectural Role

This is a specialized **View** component for handling authorization errors.

## 3. Key Features & Logic

- **Navigation Target:** The `AdminRoute` component explicitly navigates to this page if a non-admin user attempts to access an admin route.
- **Stylized Display:** Similar to the `ErrorPage`, it uses a full-screen terminal aesthetic with a typing animation to display a security-themed message, informing the user that their access attempt was denied and logged.
- **User Personalization:** It retrieves the current user''s username from the `useAuthStore` to include it in the animated text, making the message feel more specific and informative.

## 4. State Management

- The typing animation is managed by the `useTypingAnimation` hook.
- It reads from the `useAuthStore` to get the current username.'),

('frontend/src/pages/error/NotFoundPage.jsx',
'## 1. File Overview & Purpose

This component renders a **404 Not Found** error page. It is configured as the wildcard route (`*`) in `router/index.jsx` and is displayed whenever a user navigates to a URL that does not match any other defined route.

## 2. Architectural Role

This is a specialized **View** component for handling routing errors.

## 3. Key Features & Logic

- **Path Awareness:** It uses the `useLocation` hook from React Router to get the incorrect path that the user tried to access.
- **Stylized Display:** It uses the terminal aesthetic and `useTypingAnimation` hook to display a message that simulates a "file not found" error from a command-line interface, dynamically including the incorrect path in the message.
- **Recovery Action:** Provides a clear link back to the main dashboard.

## 4. State Management

- The typing animation is managed by the `useTypingAnimation` hook.');
COMMIT;