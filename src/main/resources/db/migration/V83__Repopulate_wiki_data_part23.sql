-- Flyway migration V83, Part 23: Overhaul Technical Wiki Documentation (Frontend Components & UI)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/components/profile/ProfileEventHistory.jsx',
'## 1. File Overview & Purpose

This is a presentational component that displays the user''s **event participation history** on their profile page.

## 2. Architectural Role

This is a **View** sub-component, used within `ProfilePage`.

## 3. Key Features & Logic

- **Props-Based:** It receives the `eventHistory` array as a prop.
- **Rendering:** It renders a table displaying the event name, date, and the user''s status for that event.
- **Feedback Link:** For events that are "ABGESCHLOSSEN" (Completed) and where the user was "ZUGEWIESEN" (Assigned), it renders a link to the `/feedback/event/:id` page, prompting the user to provide feedback.'),

('frontend/src/components/profile/ProfileQualifications.jsx',
'## 1. File Overview & Purpose

This is a presentational component that displays the user''s **earned qualifications** on their profile page.

## 2. Architectural Role

This is a **View** sub-component, used within `ProfilePage`.

## 3. Key Features & Logic

- **Props-Based:** It receives the `qualifications` array as a prop.
- **Rendering:** It renders a simple table listing the names of the courses the user has completed and their status.'),

('frontend/src/components/profile/ProfileSecurity.jsx',
'## 1. File Overview & Purpose

This component handles the **security-related aspects** of the user''s profile, such as changing their password. The Passkey/WebAuthn functionality has been removed and is currently disabled.

## 2. Architectural Role

This is a **View** sub-component, used within `ProfilePage`.

## 3. Key Features & Logic

- **Password Change:** Provides a link to the dedicated `/passwort` page.
- **Passkey (Disabled):** Displays a placeholder message indicating that the passwordless login feature is being reworked. The "Register New Device" button is present but disabled.'),

('frontend/src/components/storage/DamageReportModal.jsx',
'## 1. File Overview & Purpose

This component provides the **modal dialog for a user to report damage** to a specific inventory item.

## 2. Architectural Role

This is a **UI Container Component** used by the `StorageItemDetailsPage`.

## 3. Key Features & Logic

- **Controlled Form:** It is a controlled form that manages the `description` of the damage in its local `useState`.
- **Submission:** On submit, it calls the `POST /api/v1/public/storage/:itemId/report-damage` endpoint.
- **Success Callback:** On a successful submission, it calls the `onSuccess` prop to close the modal and notify the parent component.'),

('frontend/src/components/storage/ReservationCalendar.jsx',
'## 1. File Overview & Purpose

This component renders a simple, read-only **monthly calendar to display future reservations** for a single inventory item.

## 2. Architectural Role

This is a **View** sub-component used within the `StorageItemDetailsPage`.

## 3. Key Features & Logic

- **Data Transformation:** It takes a `reservations` prop (a list of events with start/end times) and uses `useMemo` to convert them into a more easily searchable format (`reservationIntervals`).
- **Date Calculation (`date-fns`):** Like the main calendar, it uses `date-fns` to calculate the days to display in the grid.
- **Rendering:** It iterates through each day of the month grid. For each day, it checks if the day falls within any of the reservation intervals. If it does, it applies a `.reserved` CSS class to visually mark the day and adds a `title` attribute to show which event it''s reserved for.'),

('frontend/src/components/ui/ChangelogModal.jsx',
'## 1. File Overview & Purpose

This component is the **"What''s New" modal** that is automatically shown to a user after an application update.

## 2. Architectural Role

This is a reusable **UI Component**, managed and displayed by the root `App.jsx` component.

## 3. Key Features & Logic

- **Props-Based:** It receives a `changelog` object and an `onClose` callback as props.
- **Markdown Rendering:** It uses `react-markdown` to render the `changelog.notes`, allowing for richly formatted update descriptions.
- **Action:** The "Verstanden!" (Got it!) button simply calls the `onClose` callback. The parent `App` component is responsible for making the API call to mark the changelog as seen.'),

('frontend/src/components/ui/Lightbox.jsx',
'## 1. File Overview & Purpose

This component provides a simple, full-screen **lightbox for viewing images**.

## 2. Architectural Role

This is a reusable **UI Component**.

## 3. Key Features & Logic

- **Rendering:** When its `src` prop is not null, it renders a full-screen overlay with the image centered.
- **Closing:** It can be closed by clicking the overlay, clicking the "×" button, or by pressing the "Escape" key. The Escape key functionality is implemented in a `useEffect` hook that adds and removes a global keydown event listener.'),

('frontend/src/components/ui/Modal.jsx',
'## 1. File Overview & Purpose

This is the generic, reusable **Modal** component used throughout the entire application for dialogs and forms.

## 2. Architectural Role

This is a fundamental, reusable **UI Component**.

## 3. Key Features & Logic

- **Conditional Rendering:** It only renders its content if the `isOpen` prop is `true`.
- **Layout:** It creates the standard modal structure: a semi-transparent overlay (`modal-overlay`) and a centered content box (`modal-content`).
- **Closing:** It provides multiple ways to close:
    - Clicking the overlay calls the `onClose` prop.
    - Clicking the "×" button calls `onClose`.
    - Pressing the "Escape" key triggers the `onClose` prop (managed via a `useEffect` hook).
- **Content Projection:** It uses the `children` prop to render any content passed into it from the parent component.'),

('frontend/src/components/ui/StatusBadge.jsx',
'## 1. File Overview & Purpose

This is a simple, presentational component that renders a **colored status badge**.

## 2. Architectural Role

This is a reusable **UI Component**.

## 3. Key Features & Logic

- **Props-Based:** It takes a single `status` string as a prop.
- **CSS Class Logic:** It contains a `getStatusClass` function that uses a `switch` statement to map different status strings (e.g., "LAUFEND", "GEPLANT", "ABGESCHLOSSEN") to specific CSS classes (`status-warn`, `status-ok`, `status-info`) which control the badge''s color.'),

('frontend/src/components/ui/ThemeSwitcher.jsx',
'## 1. File Overview & Purpose

This component is the **light/dark theme toggle button** found in the sidebar.

## 2. Architectural Role

This is a reusable **UI Component**.

## 3. Key Features & Logic

- **Global State Interaction:** It reads the current `theme` and the `setTheme` function from the global `useAuthStore`.
- **Icon Toggling:** It conditionally renders either a moon icon or a sun icon based on the current theme.
- **Action:** When clicked, it calls the `setTheme` function from the auth store, which handles both updating the global state and making the API call to persist the user''s preference.'),

('frontend/src/components/ui/ToastContainer.jsx',
'## 1. File Overview & Purpose

This component is responsible for rendering the **toast notifications** (small, non-blocking pop-up messages) in the corner of the screen.

## 2. Architectural Role

This is a **UI Component** that is rendered once in the root `App.jsx` component.

## 3. Key Features & Logic

- **Context Consumption:** It uses the `useToast` hook to get the current array of active `toasts` from the `ToastContext`.
- **Rendering:** It maps over the `toasts` array and renders a `Toast` component for each one.
- **`Toast` Sub-Component:**
    - The inner `Toast` component manages its own visibility with a `useEffect` hook. When mounted, it sets itself to visible, and after a timeout, it fades out.
    - It applies the correct CSS class (`toast-success`, `toast-danger`, etc.) based on the `type` prop.
    - If a `url` prop is provided, it wraps the toast in a `<Link>` tag, making the entire notification clickable.'),

('frontend/src/components/ui/WarningNotification.jsx',
'## 1. File Overview & Purpose

This component displays a high-priority, blocking **Warning Notification** modal. It is used for critical, real-time alerts sent from the server.

## 2. Architectural Role

This is a specialized **UI Component**, managed by the `useNotifications` hook and rendered in `App.jsx`.

## 3. Key Features & Logic

- **Attention-Grabbing:** It is designed to be highly disruptive to get the user''s immediate attention.
    - It plays a looping audio alert (`attention.mp3`).
    - A `useEffect` hook adds a `warning-flash` class to the `<body>`, causing the entire page background to flash red.
- **Lifecycle Management:** The `useEffect` hook returns a cleanup function that stops the audio and removes the flashing class when the component is unmounted (i.e., when the user dismisses it).');
COMMIT;