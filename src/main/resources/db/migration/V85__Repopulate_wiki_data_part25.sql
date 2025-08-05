-- Flyway migration V85, Part 25: Overhaul Technical Wiki Documentation (Frontend Services & Store)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/services/apiClient.js',
'## 1. File Overview & Purpose

This is a singleton service that acts as a centralized wrapper around the native `fetch` API. It is used by every component and hook in the application to communicate with the backend REST API.

## 2. Architectural Role

This is a core **Service** component in the frontend application''s infrastructure layer.

## 3. Key Features & Logic

- **Standardized Responses:** Its `request` method ensures that all API calls have a consistent return format (`{ success: boolean, message: string, data: object }`).
- **Error Handling:** It contains centralized logic to handle different types of errors:
    - **Network Errors:** Catches `Failed to fetch` errors and returns a user-friendly message indicating the backend is likely offline.
    - **HTTP Errors (4xx, 5xx):** Parses the JSON error response from the backend and throws an `Error` with the message provided by the server.
    - **Non-JSON Responses:** Detects if the server returns something other than JSON (like an HTML error page) and provides a generic server connection error.
- **Automatic Logout:** The `setup` method allows the `authStore` to inject its `logout` function. The `request` method will automatically call this function if it ever receives an HTTP 401 (Unauthorized) response, ensuring that an invalid session is immediately cleared.
- **CSRF Protection (Stubbed):** It includes logic to read the `XSRF-TOKEN` from the document cookies and add it as an `X-XSRF-TOKEN` header to all state-changing requests (POST, PUT, DELETE). **However**, this is currently bypassed by the backend''s security configuration, which ignores CSRF for API routes.
- **Convenience Methods:** Provides simple `get`, `post`, `put`, and `delete` methods that pre-configure the `fetch` options.'),

('frontend/src/store/authStore.js',
'## 1. File Overview & Purpose

This file defines the global **authentication and session state** for the entire frontend application, using the **Zustand** state management library. It is the single source of truth for the currently logged-in user, their permissions, and UI preferences.

## 2. Architectural Role

This is the core **State Management** component for the application.

## 3. Key Dependencies & Libraries

- **Zustand (`create`)**: The function for creating a new state store.
- **Zustand Middleware (`persist`)**: Middleware that automatically persists parts of the store''s state to `localStorage`.
- `apiClient`: The service used for all authentication-related API calls.

## 4. In-Depth Breakdown

- **`create(persist(...))`**: The store is created and wrapped with the `persist` middleware.
    - **`partialize`**: This configuration option tells the `persist` middleware to *only* save the `theme` property to localStorage. The user session data is intentionally not persisted for security; it is always re-fetched on application load.
- **State Properties:**
    - `user`: The full user object returned from the backend.
    - `navigationItems`: The user-specific list of navigation links.
    - `isAuthenticated`: A boolean flag derived from the presence of a user object.
    - `isAdmin`: A boolean flag derived from the user''s role.
    - `theme`: The user''s selected UI theme (''light'' or ''dark'').
- **Actions (Functions):**
    - **`login(username, password)`**: Makes the `POST /api/v1/auth/login` call. On success, it calls `fetchUserSession` to populate the store.
    - **`logout()`**: Makes the `POST /api/v1/auth/logout` call to clear the backend cookie, then clears all local state and removes the item from `localStorage`.
    - **`fetchUserSession()`**: Makes the `GET /api/v1/auth/me` call. This is the primary function for populating the session state. It stores the user object and navigation items, and sets the `isAuthenticated` and `isAdmin` flags.
    - **`setTheme(newTheme)`**: Makes the `PUT /api/v1/public/profile/theme` call to persist the user''s theme preference to the database, then updates the local state.'),

('frontend/App.jsx', 'This file is a duplicate of `src/App.jsx` and its documentation is identical. See `src/App.jsx`.'),

('frontend/main.jsx', 'This file is a duplicate of `src/main.jsx` and its documentation is identical. See `src/main.jsx`.');
COMMIT;