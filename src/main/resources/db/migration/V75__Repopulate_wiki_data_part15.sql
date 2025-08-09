-- Flyway migration V75, Part 15: Overhaul Technical Wiki Documentation (Frontend Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/pages/LehrgaengePage.jsx',
'## 1. File Overview & Purpose

This React component renders the main **Courses & Meetings** page (`/lehrgaenge`). It lists upcoming training sessions and allows users to sign up, sign off, or request new training topics.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch all upcoming meetings from `/api/v1/public/meetings`. This data is pre-enriched by the backend with the current user''s attendance status.
- **Actions:** Renders "Anmelden" (Sign Up) or "Abmelden" (Sign Off) buttons based on the user''s status for each meeting. Clicking these buttons calls the `POST /api/v1/public/meetings/:id/:action` endpoint.
- **Training Request:** Contains a button that opens the `RequestTrainingModal`, allowing users to submit a new training topic via the `POST /api/v1/public/training-requests` endpoint.
- **Responsive View:** Includes both a desktop table and a mobile card list view.

## 4. State Management

- **Meeting List:** Managed by the `useApi` hook.
- **Modal State:** Uses `useState` to control the visibility of the training request modal.'),

('frontend/src/pages/LoginPage.jsx',
'## 1. File Overview & Purpose

This React component renders the **Login** page (`/login`). It provides the form for username/password authentication.

## 2. Architectural Role

This is a **View** component in the frontend application and is the main entry point for unauthenticated users.

## 3. Key Features & Logic

- **Authentication Flow:**
    1.  The form is a controlled component, with `username` and `password` state managed by `useState`.
    2.  On submit, it calls the `login` function from the `useAuthStore`.
    3.  The `authStore.login` function calls the `POST /api/v1/auth/login` endpoint.
    4.  If successful, the backend sets an HttpOnly JWT cookie, and the `authStore` fetches the full user session.
    5.  An `useEffect` hook detects the change in `isAuthenticated` state and redirects the user to their dashboard (`/home`) or their originally intended page.
- **UI Features:** Includes a password visibility toggle.
- **Error Handling:** Displays error messages returned from the `authStore` if the login fails.

## 4. State Management

- **Form State:** Managed locally with `useState`.
- **Authentication State:** Interacts with the global `useAuthStore` to trigger the login process and react to authentication changes.'),

('frontend/src/pages/MeetingDetailsPage.jsx',
'## 1. File Overview & Purpose

This React component renders the detailed view for a single training meeting (`/lehrgaenge/details/:meetingId`).

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch the meeting''s details and its associated attachments from `/api/v1/public/meetings/:id`.
- **Content Display:** Renders all details of the meeting, including description (with Markdown support) and a list of downloadable attachments.

## 4. State Management

- The component''s state is managed by the `useApi` hook.'),

('frontend/src/pages/PackKitPage.jsx',
'## 1. File Overview & Purpose

This React component renders a special, printer-friendly **Packing List** for an inventory kit (`/pack-kit/:kitId`). It is designed to be accessed via QR code.

## 2. Architectural Role

This is a **View** component that uses the `MinimalLayout` to provide a clean, focused UI without the main application sidebar and header.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get the kit''s details and its item list from `/api/v1/public/kits/:id`.
- **Printable Checklist:** Renders the list of items with checkboxes, making it easy to use as a physical checklist when packing a case.
- **Print Styles:** Includes an embedded `<style>` block with `@media print` rules. These rules ensure that when the user prints the page, only the checklist area is visible, and all other UI elements (like the "Print" button) are hidden.

## 4. State Management

- The component''s state is managed by the `useApi` hook.'),

('frontend/src/pages/PasswordPage.jsx',
'## 1. File Overview & Purpose

This React component renders the dedicated form for a user to change their own password (`/passwort`).

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Controlled Form:** Manages the state for the current password, new password, and confirmation password fields using `useState`.
- **Validation:** Performs client-side checks to ensure the new password is at least 10 characters and that the new password and confirmation match.
- **Submission:** On submit, it calls the `PUT /api/v1/public/profile/password` endpoint with the form data. The backend performs the final, authoritative validation (checking the current password and enforcing the policy).

## 4. State Management

- All form state is managed locally with `useState` hooks.'),

('frontend/src/pages/ProfilePage.jsx',
'## 1. File Overview & Purpose

This React component renders the user''s main **Profile** page (`/profil`). It acts as a container, aggregating and displaying several sub-components that represent different aspects of the user''s profile.

## 2. Architectural Role

This is a **View** and **Layout** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to call the aggregated `/api/v1/public/profile` endpoint, which returns all data needed for the page in a single response.
- **Component Composition:** It renders several specialized child components, passing the relevant slice of the fetched data to each one:
    - `ProfileDetails`: For core user data.
    - `ProfileSecurity`: For password management.
    - `ProfileQualifications`: For the user''s completed courses.
    - `ProfileAchievements`: For earned badges.
    - `ProfileEventHistory`: For a list of past events.
- **Update Handling:** It provides an `onUpdate` callback function (which simply calls the `reload` function from `useApi`) to its child components. This allows a child component (like `ProfileDetails`) to trigger a full refresh of all profile data after a change is submitted.

## 4. State Management

- All profile data is managed by the `useApi` hook and passed down to child components as props.');
COMMIT;