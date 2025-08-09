-- Flyway migration V76, Part 16: Overhaul Technical Wiki Documentation (Frontend Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/pages/QrActionPage.jsx',
'## 1. File Overview & Purpose

This React component renders the specialized **QR Action** page (`/lager/qr-aktion/:itemId`). It provides a highly simplified, mobile-first interface for quickly checking an item in or out of the inventory.

## 2. Architectural Role

This is a **View** component that uses the `MinimalLayout` to provide a focused UI without the main application navigation, as it''s intended to be accessed directly via QR code scan.

## 3. Key Features & Logic

- **Data Fetching:** It makes two parallel API calls using `useApi`:
    1.  `/api/v1/public/storage/:itemId` to get the details of the specific item being acted upon.
    2.  `/api/v1/public/events` to get a list of active events for the optional "Assign to Event" dropdown.
- **Transaction Form:** It presents a simple form to specify the quantity, an optional event, and notes.
- **Submission:** The form has two distinct submit buttons ("Entnehmen" and "Einräumen"). The component captures which button was clicked to set the `type` of the transaction and sends the data to the `POST /api/v1/public/storage/transactions` endpoint.

## 4. State Management

- All data is managed by two instances of the `useApi` hook.'),

('frontend/src/pages/SearchResultsPage.jsx',
'## 1. File Overview & Purpose

This React component renders the **Search Results** page (`/suche`). It displays the results of a global search query.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Query Parsing:** It uses the `useSearchParams` hook from React Router to get the search query (`q=...`) from the URL.
- **Data Fetching:** It uses the `useApi` hook to call the `/api/v1/public/search` endpoint, passing the query as a parameter.
- **Result Rendering:** It maps over the array of results returned from the API. For each result, it displays the title, a snippet, the result type (e.g., "Veranstaltung"), and a direct link to the item''s detail page. It also uses a helper function to show a relevant icon for each result type.

## 4. State Management

- The component''s data is managed by the `useApi` hook.'),

('frontend/src/pages/StorageItemDetailsPage.jsx',
'## 1. File Overview & Purpose

This React component renders the detailed view for a single inventory item (`/lager/details/:itemId`). It provides a comprehensive overview of the item, including its history and future availability.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** It makes multiple API calls using `useApi`:
    - `/api/v1/public/storage/:id` to get the core item details.
    - `/api/v1/public/storage/:id/history` to get the transaction and maintenance logs.
    - `/api/v1/public/storage/:id/reservations` to get data for the availability calendar.
- **Tabbed Interface:** Manages state to switch between different tabs:
    - **History:** Shows the transaction log.
    - **Maintenance:** Shows the maintenance log.
    - **Availability:** Renders the `ReservationCalendar` component to show future bookings.
    - **Related Items:** Renders the `RelatedItemsTab` to show associated equipment.
- **Interactive Elements:**
    - **Lightbox:** Clicking the item''s image opens a full-screen `Lightbox` view.
    - **Damage Report:** The "Schaden melden" button opens a `DamageReportModal` for submitting a new report.

## 4. State Management

- All data is managed by multiple instances of the `useApi` hook.
- UI state (active tab, modal visibility) is managed with `useState` hooks.'),

('frontend/src/pages/StoragePage.jsx',
'## 1. File Overview & Purpose

This React component renders the main **Inventory** page (`/lager`). It displays the entire equipment inventory and provides the primary interface for checking items in and out.

## 2. Architectural Role

This is a key **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to call the aggregated `/api/v1/public/storage` endpoint, which returns both the inventory items (grouped by location) and the list of active events for the transaction modal.
- **Interactive Filtering:**
    - It uses `useState` hooks to manage state for a live search term, a category filter, and a status filter.
    - A `useMemo` hook is used to efficiently compute the `filteredData` whenever the source data or any filter changes.
- **Cart System:**
    - It manages a `cart` array in its `useState`.
    - "Entnehmen" (Checkout) and "Einräumen" (Checkin) buttons on each item add that item to the cart.
    - A Floating Action Button (FAB) shows the number of items in the cart and opens the `CartModal`.
- **Bulk Transactions:** The `CartModal` allows the user to review all items in their cart and submit them as a series of transactions with a single click. The `handleBulkTransactionSubmit` function sends multiple parallel requests to the `POST /api/v1/public/storage/transactions` endpoint.
- **Responsive View:** It provides both a detailed desktop table and a compact mobile card list view.

## 4. State Management

- **Inventory Data:** Managed by the `useApi` hook.
- **Filters & Cart:** Managed by `useState` hooks.
- **Filtered Results:** Derived state computed using `useMemo`.'),

('frontend/src/pages/TeamDirectoryPage.jsx',
'## 1. File Overview & Purpose

This React component renders the **Team Directory** (`/team`), a searchable list of all team members.

## 2. Architectural Role

This is a **View** component in the frontend application.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get a list of all users from `/api/v1/users`.
- **Live Search:** A simple text input allows users to filter the directory by name in real-time.
- **Crew Card Modal:** Clicking the "Crew-Karte ansehen" button for a user opens a modal.
    - The `CrewCardModal` component is a separate component that takes a `userId` as a prop.
    - It then triggers its own `useApi` call to the `/api/v1/public/profile/:userId` endpoint to fetch the specific user''s qualifications and achievements for display.

## 4. State Management

- **User List:** Managed by the `useApi` hook in the main component.
- **Search Term & Modal State:** Managed by `useState` hooks.
- **Crew Card Data:** Managed by a separate `useApi` instance within the modal component.');
COMMIT;