-- Flyway migration V84, Part 24: Overhaul Technical Wiki Documentation (Frontend Context, Hooks, Router)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/context/ToastContext.jsx',
'## 1. File Overview & Purpose

This file defines and exports the **Toast Context** and its associated `ToastProvider` and `useToast` hook. It provides a global, application-wide system for creating toast notifications from any component.

## 2. Architectural Role

This is a core **State Management** component using React''s Context API.

## 3. Key Features & Logic

- **`ToastContext`**: Created with `createContext` to hold the toast state and functions.
- **`ToastProvider`**: The wrapper component that provides the context to its children.
    - It uses `useState` to manage an array of `toasts`.
    - It exposes an `addToast` function. When called, this function adds a new toast object (with a unique ID, message, type, etc.) to the state array and sets a `setTimeout` to automatically remove that toast from the array after 5 seconds.
- **`useToast()`**: The custom hook that components use to access the `addToast` function from the context. It includes a check to ensure it''s used within a `ToastProvider`.'),

('frontend/src/hooks/useAdminData.js',
'## 1. File Overview & Purpose

This custom hook provides a centralized and efficient way for admin components to fetch the prerequisite data they need for forms (e.g., lists of users, roles, courses, items).

## 2. Architectural Role

This is a **Data Fetching Hook** in the frontend application.

## 3. Key Features & Logic

- **Data Aggregation:** The main `useAdminData` hook makes multiple parallel API calls using `Promise.all` to fetch all necessary data sources at once.
- **Permission-Aware Fetching:** It reads the current user''s permissions from the `useAuthStore` and conditionally skips API calls for data the user is not allowed to see. For example, if a user doesn''t have `COURSE_READ` permission, it won''t attempt to fetch the courses list.
- **State Management:** It manages a single state object that contains all the fetched data, a `loading` flag, and an `error` state.
- **Granular Hooks:** The file also exports smaller, more focused hooks like `useAdminRolesAndPermissions` for components that only need a subset of the admin data, improving performance and separation of concerns.'),

('frontend/src/hooks/useApi.js',
'## 1. File Overview & Purpose

This is a generic, reusable custom hook for managing the state of an API call. It encapsulates the standard pattern of handling data, loading, and error states for any asynchronous data-fetching operation.

## 2. Architectural Role

This is a fundamental **Data Fetching Hook** used by almost every page and component in the application that needs to retrieve data from the backend.

## 3. Key Features & Logic

- **State Management:** It uses `useState` to manage three key pieces of state: `data`, `loading`, and `error`. The `loading` state is importantly initialized to `true` to prevent race conditions and "component suspended" errors with React.lazy and routing.
- **`fetchData` Function:**
    - It uses `useCallback` to memoize the data-fetching function.
    - It wraps the `apiCall` (passed in as an argument) in a `try...catch...finally` block.
    - On success, it sets the `data`.
    - On failure, it sets the `error` message.
    - In the `finally` block, it always sets `loading` to `false`.
- **`useEffect` Hook:** It calls `fetchData` once when the component mounts and whenever the `apiCall` function itself changes.
- **Return Value:** It returns an object containing the current `data`, `loading` state, `error` state, and a `reload` function (which is simply a reference to `fetchData`) that allows components to manually trigger a data refresh.'),

('frontend/src/hooks/useNotifications.js',
'## 1. File Overview & Purpose

This custom hook encapsulates the logic for connecting to the server''s real-time notification stream (Server-Sent Events) and handling incoming messages.

## 2. Architectural Role

This is a **Real-Time Data Hook** used by the root `App.jsx` component to listen for global notifications.

## 3. Key Features & Logic

- **SSE Connection:** It uses the native browser `EventSource` API to establish a persistent connection to the `/api/v1/admin/notifications/sse` endpoint.
- **Event Listeners:**
    - It sets up an `onmessage` listener for generic messages.
    - It adds a specific event listener for the `"notification"` event type.
- **Message Handling:** When a `"notification"` event is received:
    - If the notification `level` is `"Warning"`, it uses `useState` to set the `warningNotification` state, which causes the `App` component to render the disruptive `WarningNotification` modal.
    - For other levels, it calls the `addToast` function from the `useToast` context to display a less intrusive toast message. It also passes along any `url` from the notification payload to make the toast clickable.
- **Lifecycle Management:** The `useEffect` hook returns a cleanup function that calls `events.close()`, ensuring the SSE connection is terminated when the `App` component unmounts.'),

('frontend/src/hooks/useTypingAnimation.js',
'## 1. File Overview & Purpose

This custom hook implements a **typing animation effect**. It takes an array of text lines and "types" them out one character at a time, simulating a terminal or command-line interface.

## 2. Architectural Role

This is a **UI/Animation Hook** used by the stylized error pages (`ErrorPage`, `NotFoundPage`, `ForbiddenPage`).

## 3. Key Features & Logic

- **State Management:** It uses `useState` to manage the `renderedLines` (the portion of text currently visible) and an `isComplete` flag.
- **Animation Logic:** It uses a series of nested `setTimeout` calls to achieve the animation.
    - The main function `typeLine` handles moving from one line to the next.
    - The inner function `typeChar` handles adding one character at a time to the current line.
- **Cleanup:** The `useEffect` hook returns a cleanup function that clears all pending `setTimeout`s when the component unmounts, preventing memory leaks.
- **Scrolling:** It exposes a `containerRef` that should be attached to the scrollable element. It automatically scrolls this container to the bottom as new lines are typed.'),

('frontend/src/hooks/useWebSocket.js',
'## 1. File Overview & Purpose

This is a generic, reusable custom hook for managing a **WebSocket connection**. It handles connecting, receiving messages, and automatically attempting to reconnect if the connection is dropped.

## 2. Architectural Role

This is a fundamental **Real-Time Data Hook** used by components that require two-way real-time communication, such as `MessageView` and `ChecklistTab`.

## 3. Key Features & Logic

- **Connection Management:**
    - It uses `useRef` to hold a persistent reference to the WebSocket object across renders.
    - A `useEffect` hook establishes the connection when the component mounts and the `url` is provided.
- **Event Handlers:** It sets up all the standard WebSocket event handlers:
    - `onopen`: Updates the `readyState` and logs a success message.
    - `onmessage`: Parses the incoming JSON data and calls the `onMessage` callback function that was passed in from the parent component.
    - `onclose`: Logs the closure and schedules a reconnection attempt after a 5-second delay (unless it was a specific auth-related closure code).
    - `onerror`: Logs errors and closes the connection.
- **Cleanup:** The `useEffect` hook returns a cleanup function that properly closes the WebSocket connection when the component unmounts, preventing reconnection attempts.
- **`sendMessage` Function:** It returns a memoized `sendMessage` function that the parent component can use to send JSON objects over the WebSocket.'),

('frontend/src/router/AdminRoute.jsx',
'## 1. File Overview & Purpose

This is a route wrapper component that protects all administrative routes. It checks if the currently logged-in user has admin privileges.

## 2. Architectural Role

This is a **Routing/Authorization** component.

## 3. Key Features & Logic

- **Authorization Check:** It reads the `isAdmin` boolean flag from the global `useAuthStore`.
- **Protected Content:** If `isAdmin` is `true`, it renders the `<Outlet />`, which allows React Router to render the nested admin page.
- **Access Denied:** If `isAdmin` is `false`, it uses the `<Navigate>` component from React Router to perform a client-side redirect to the `/forbidden` page, preventing the user from accessing the admin content.'),

('frontend/src/router/ProtectedRoute.jsx',
'## 1. File Overview & Purpose

This is a route wrapper component that protects all routes requiring authentication. It checks if a user is currently logged in.

## 2. Architectural Role

This is a **Routing/Authentication** component.

## 3. Key Features & Logic

- **Authentication Check:** It reads the `isAuthenticated` boolean flag from the global `useAuthStore`.
- **Protected Content:** If `isAuthenticated` is `true`, it renders its `children`, which is typically the main `<App />` layout.
- **Redirect to Login:** If `isAuthenticated` is `false`, it uses the `<Navigate>` component to redirect the user to the `/login` page. It also passes the user''s originally intended location in the state, so they can be redirected back to it after a successful login.'),

('frontend/src/router/index.jsx',
'## 1. File Overview & Purpose

This file contains the complete **routing configuration** for the entire frontend application, using `react-router-dom`.

## 2. Architectural Role

This is a core **Configuration** file for the frontend.

## 3. Key Features & Logic

- **`createBrowserRouter`**: The function used to define all the application''s routes.
- **Lazy Loading:** It uses `React.lazy()` for almost all page components. This enables code splitting, meaning the JavaScript for a specific page is only downloaded when the user navigates to it, improving initial load times. `Suspense` is used implicitly by React Router to handle the loading state.
- **Layout Nesting:** It defines a nested route structure:
    - The root path (`/`) is protected by `ProtectedRoute` and renders the main `App` layout.
    - All standard user-facing pages are defined as children of this root route.
    - The `/admin` path is further protected by the `AdminRoute` component. All admin pages are nested as children of this route.
- **Specialized Layouts:** It defines separate top-level routes for pages that need a `MinimalLayout` (like `/pack-kit`) or an `ErrorLayout` (like `/forbidden` and `/not-found`).
- **Error Handling:** It configures the `errorElement` for the main route to point to the `ErrorPage`, which will catch any rendering errors within the protected application.');
COMMIT;