-- Flyway migration V80, Part 20: Overhaul Technical Wiki Documentation (Frontend Components & Admin Pages)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/components/admin/dashboard/EventTrendChart.jsx',
'## 1. File Overview & Purpose

This is a presentational React component responsible for rendering the **Event Trend Chart** on the Admin Dashboard. It uses the `react-chartjs-2` library to create a line chart.

## 2. Architectural Role

This is a **View** sub-component, specifically for data visualization within the `AdminDashboardPage`.

## 3. Key Features & Logic

- **Library Integration:** It imports and registers the necessary components from `chart.js` (like scales, elements, and tooltips).
- **Data Mapping:** It takes `trendData` (an array of `{ month: string, count: number }`) as a prop and transforms it into the specific `data` and `options` format required by Chart.js.
- **Rendering:** It renders the `<Line />` component from `react-chartjs-2`, passing the prepared data and options to it.'),

('frontend/src/components/admin/dashboard/Widget.jsx',
'## 1. File Overview & Purpose

This is a reusable React component that renders a single **Dashboard Widget**. It provides a standardized layout with a title, an icon, a content area, and an optional "View All" link.

## 2. Architectural Role

This is a reusable **UI Component** used throughout the `AdminDashboardPage`.

## 3. Key Features & Logic

- **Props-Based:** It is a purely presentational component that receives all its content via props: `icon`, `title`, `children` (for the main content), `linkTo`, and `linkText`.
- **Standardized Layout:** It renders its content inside a `<div class="card">`, ensuring a consistent look and feel for all dashboard widgets.'),

('frontend/src/components/admin/events/DynamicItemRows.jsx',
'## 1. File Overview & Purpose

This is a controlled React component that manages a dynamic list of **inventory item reservations** within a form. It allows an administrator to add, remove, and edit multiple item/quantity pairs.

## 2. Architectural Role

This is a **Form Component** used within the `EventModal`.

## 3. Key Features & Logic

- **Controlled Component:** It does not manage its own state. It receives the list of rows (`rows`) and a setter function (`setRows`) as props from its parent (`EventModal`).
- **Dynamic Rows:** It maps over the `rows` array to render a row for each item. Each row contains a `<select>` dropdown for the item and an `<input>` for the quantity.
- **State Manipulation:**
    - `handleAddRow`: Appends a new, empty item object to the `rows` array via the `setRows` prop.
    - `handleRemoveRow`: Removes an item from the array at a specific index.
    - `handleRowChange`: Updates a specific field (itemId or quantity) for an item at a specific index.'),

('frontend/src/components/admin/events/DynamicSkillRows.jsx',
'## 1. File Overview & Purpose

This is a controlled React component that manages a dynamic list of **skill requirements** for an event. It allows an administrator to add, remove, and edit multiple skill/personnel count pairs.

## 2. Architectural Role

This is a **Form Component** used within the `EventModal`.

## 3. Key Features & Logic

- **Controlled Component:** Like `DynamicItemRows`, it receives its state (`rows`, `setRows`) via props from the `EventModal`.
- **Functionality:** Its implementation is nearly identical to `DynamicItemRows`, but the inputs are tailored for selecting a `Course` and specifying the number of required persons.'),

('frontend/src/components/admin/events/EventModal.jsx',
'## 1. File Overview & Purpose

This is a large, stateful React component that provides the **modal dialog for creating and editing events**. It contains the entire form, including the tabbed interface and the dynamic row components.

## 2. Architectural Role

This is a key **UI Container Component** used by the `AdminEventsPage`.

## 3. Key Features & Logic

- **State Management:** It uses `useState` to manage all form data (`formData`), the state of the skill requirement rows (`skillRows`), the item reservation rows (`itemRows`), the selected file for upload, and the active tab.
- **Edit Mode:** It determines if it''s in "edit" or "create" mode based on whether an `event` prop is passed in. In edit mode, an `useEffect` hook populates the form with the existing event''s data.
- **Tabbed Interface:** Manages the `activeTab` state to show/hide the "General" and "Details & Bedarf" sections of the form.
- **Component Composition:** It renders the `DynamicSkillRows` and `DynamicItemRows` components, passing down the relevant state and state setters.
- **Submission:** The `handleSubmit` function assembles all the data from the various state variables into a `FormData` object. This is necessary to send both the JSON event data and the binary file data in a single multipart request to the backend.'),

('frontend/src/components/admin/events/TaskDependenciesForm.jsx',
'## 1. File Overview & Purpose

This is a controlled React component that renders a checklist of tasks, allowing an administrator to define **dependencies** for a specific event task (i.e., which other tasks must be completed first).

## 2. Architectural Role

This is a **Form Component** intended for use within a task editing modal (though not fully implemented in the provided code, it is present).

## 3. Key Features & Logic

- **Controlled Component:** It receives the list of selected dependencies (`selectedDependencies`, a `Set`) and an `onDependencyChange` callback function as props.
- **Rendering:** It receives a list of `allTasks` for the event and renders a checkbox for each one.
- **State Manipulation:** When a checkbox is toggled, it creates a new `Set` based on the previous selection, adds or deletes the toggled task ID, and calls the `onDependencyChange` prop with the new `Set` to update the parent component''s state.'),

('frontend/src/components/admin/kits/KitItemsForm.jsx',
'## 1. File Overview & Purpose

This is a stateful React component that provides the form for managing the **items within an inventory kit**. It is displayed within the accordion view on the `AdminKitsPage`.

## 2. Architectural Role

This is a **Form Component**.

## 3. Key Features & Logic

- **Local State:** Unlike the dynamic rows for events, this component manages its own state for the list of `items`. It initializes this state from the `kit.items` prop.
- **Dynamic Rows:** It provides the standard "add row" and "remove row" functionality for editing the list of items and their quantities.
- **Submission:** On submit, it calls the `PUT /api/v1/kits/:id/items` endpoint to overwrite the entire list of items for that kit. It then calls the `onUpdateSuccess` prop to trigger a data refresh on the parent page.'),

('frontend/src/components/admin/kits/KitModal.jsx',
'## 1. File Overview & Purpose

This component provides the **modal dialog for creating and editing the metadata** of an inventory kit (name, description, location).

## 2. Architectural Role

This is a **UI Container Component** used by the `AdminKitsPage`.

## 3. Key Features & Logic

- **State Management:** It is a controlled form that manages its state (`formData`) with `useState`.
- **Edit vs. Create Mode:** It checks for the presence of a `kit` prop to determine its mode and initialize its state.
- **Submission:** On submit, it calls either `POST /api/v1/kits` (for create) or `PUT /api/v1/kits/:id` (for edit) and then calls the `onSuccess` prop to close the modal and refresh the parent page.'),

('frontend/src/components/admin/matrix/AttendanceModal.jsx',
'## 1. File Overview & Purpose

This component provides the **modal dialog for updating a user''s attendance** for a specific training meeting. It is opened when an admin clicks a cell in the Qualification Matrix.

## 2. Architectural Role

This is a **UI Container Component** used by the `AdminMatrixPage`.

## 3. Key Features & Logic

- **State Management:** It is a controlled form that manages the state for the `attended` checkbox and the `remarks` textarea.
- **Submission:** On submit, it calls the `PUT /api/v1/matrix/attendance` endpoint with the user ID, meeting ID, and the new attendance status. It then calls the `onSuccess` prop to close the modal and refresh the matrix data.'),

('frontend/src/components/admin/reports/UserActivityChart.jsx',
'## 1. File Overview & Purpose

This is a presentational React component that renders a **horizontal bar chart** showing user activity (e.g., event participations).

## 2. Architectural Role

This is a **View** sub-component for data visualization, used within the `AdminReportsPage`.

## 3. Key Features & Logic

- **Library Integration:** It uses `react-chartjs-2` to render a `<Bar />` chart.
- **Data Mapping:** It takes `activityData` as a prop and transforms it into the data format required by Chart.js.
- **Configuration:** It sets the `indexAxis: ''y''` option to make the bar chart horizontal, which is often better for displaying lists of names.'),

('frontend/src/components/admin/storage/StorageItemModal.jsx',
'## 1. File Overview & Purpose

This is a highly versatile, stateful React component that provides the **modal dialog for all administrative actions on a storage item**. It can switch between multiple modes to show different forms.

## 2. Architectural Role

This is a key **UI Container Component** used by `AdminStoragePage` and `AdminDefectivePage`.

## 3. Key Features & Logic

- **Mode Management:** It uses a local `mode` state, initialized by the `initialMode` prop, to control which form is rendered. The available modes are `create`, `edit`, `defect`, `repair`, and `relations`.
- **Multi-Functional Rendering:** A `renderContent()` function contains a `switch` statement that returns the correct form JSX based on the current `mode`.
- **Component Composition:** In `relations` mode, it renders the `RelatedItemsManager` component to handle that specific functionality.
- **Data Fetching:** It uses the `useApi` hook to fetch a list of all storage items, which is needed by the `RelatedItemsManager`.
- **Submission Logic:** It contains multiple separate submission handlers (`handleSubmit`, `handleDefectSubmit`, `handleRepairSubmit`) for each of its primary functions, each calling a different backend API endpoint.'),

('frontend/src/components/admin/users/PermissionTab.jsx',
'## 1. File Overview & Purpose

This is a controlled, presentational React component that renders the **nested checklist of permissions** inside the `UserModal`.

## 2. Architectural Role

This is a **Form Component**.

## 3. Key Features & Logic

- **Controlled Component:** It receives the `groupedPermissions` object, a `Set` of `assignedIds`, and an `onPermissionChange` callback function as props.
- **Rendering Logic:**
    - It iterates over the keys of the `groupedPermissions` object to create a `<details>` section for each permission group (e.g., "USER", "EVENT").
    - Within each group, it maps over the permissions to render a checkbox and label for each.
    - The `checked` status of each checkbox is determined by checking if its ID exists in the `assignedIds` `Set`.
- **State Manipulation:** When a checkbox is clicked, it calls the `onPermissionChange` prop with the permission''s ID, allowing the parent `UserModal` to update its state.'),

('frontend/src/components/admin/users/UserModal.jsx',
'## 1. File Overview & Purpose

This is a large, stateful React component that provides the **modal dialog for creating and editing users**. It contains the entire form, including the tabbed interface for general details, permissions, and admin notes.

## 2. Architectural Role

This is a key **UI Container Component** used by the `AdminUsersPage`.

## 3. Key Features & Logic

- **Data Fetching:** When in edit mode, it makes an API call via `apiClient` to `/api/v1/users/:id` to fetch the full user object, including their currently assigned permissions.
- **State Management:**
    - `useState` is used to manage the `formData` object, which contains all user details and a `Set` of `permissionIds`.
    - It also manages the `activeTab` state.
- **Component Composition:** It renders the `PermissionsTab` component, passing down the `groupedPermissions` (received from `AdminUsersPage`), the `formData.permissionIds` set, and the `handlePermissionChange` callback.
- **Permission Handling:** The `handlePermissionChange` function is responsible for updating the `permissionIds` `Set` in the component''s state when a checkbox is toggled in the child component.
- **Submission:** On submit, it converts the `permissionIds` `Set` back into an array and sends the complete payload to the backend.');
COMMIT;