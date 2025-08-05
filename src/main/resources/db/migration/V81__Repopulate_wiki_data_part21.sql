-- Flyway migration V81, Part 21: Overhaul Technical Wiki Documentation (Frontend Components)

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('frontend/src/components/calendar/CalendarDesktopView.jsx',
'## 1. File Overview & Purpose

This component renders a traditional, grid-based **monthly calendar view** for desktop screens.

## 2. Architectural Role

This is a **View** sub-component, used by `CalendarPage`.

## 3. Key Features & Logic

- **Date Calculation (`date-fns`):** It heavily uses the `date-fns` library to perform all necessary date calculations:
    - `startOfMonth`, `endOfMonth`: To find the boundaries of the current month.
    - `startOfWeek`, `endOfWeek`: To find the start and end of the visible grid, ensuring full weeks are always displayed.
    - `eachDayOfInterval`: To generate an array of all `Date` objects to be rendered in the grid.
- **Data Grouping:** It uses `useMemo` to transform the flat list of `entries` (from props) into a `Map` where keys are date strings (`"yyyy-MM-dd"`) and values are arrays of events for that day. This is an efficient way to look up events for each day cell.
- **Rendering:** It maps over the `daysInGrid` array. For each day, it looks up the events in the memoized map and renders them. Days not in the current month are visually greyed out.'),

('frontend/src/components/calendar/CalendarMobileView.jsx',
'## 1. File Overview & Purpose

This component renders a simple, **chronological list of upcoming events and meetings**, optimized for mobile screens.

## 2. Architectural Role

This is a **View** sub-component, used by `CalendarPage`.

## 3. Key Features & Logic

- **Sorting:** It first sorts the incoming `entries` array by date to ensure they are displayed in chronological order.
- **Rendering:** It maps over the sorted array and renders each entry as a list item (`termin-item`). Each item displays the day, month, title, and type of the entry, and links to the respective details page.'),

('frontend/src/components/chat/ConversationList.jsx',
'## 1. File Overview & Purpose

This component renders the **list of conversations** on the left side of the `ChatPage`. It displays all of the user''s 1-on-1 and group chats and provides controls for starting new ones.

## 2. Architectural Role

This is a **View** sub-component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch the user''s conversation list from `/api/v1/public/chat/conversations`.
- **Starting New Chats:**
    - The "New Direct Message" button opens a `UserSearchModal`. When a user is selected, it calls `POST /api/v1/public/chat/conversations` to find or create a conversation and then navigates to the new chat URL.
    - The "New Group" button opens the `GroupChatModal`, which handles the creation of a new group conversation.
- **Active State:** It compares the `selectedConversationId` prop (from the URL) with each conversation''s ID to apply an "active" CSS class to the currently viewed chat.'),

('frontend/src/components/chat/GroupChatModal.jsx',
'## 1. File Overview & Purpose

This component provides the **modal dialog for creating a new group chat**.

## 2. Architectural Role

This is a **UI Container Component**.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch a list of all users from `/api/v1/users` to populate the member selection checklist.
- **State Management:** Uses `useState` to manage the `groupName` input and a `Set` of `selectedUsers`.
- **Submission:** The "Create Group" button calls the `onCreateGroup` prop (passed from `ConversationList`), providing the group name and the array of selected user IDs.'),

('frontend/src/components/chat/ManageParticipantsModal.jsx',
'## 1. File Overview & Purpose

This component provides the **modal dialog for adding new members to an existing group chat**.

## 2. Architectural Role

This is a **UI Container Component**.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to get a list of all users.
- **Filtering:** It filters the list of all users to show only those who are *not* already in the current conversation.
- **Submission:** The "Add" button calls the `onAddUsers` prop (passed from `MessageView`), providing the array of newly selected user IDs.'),

('frontend/src/components/chat/MessageStatus.jsx',
'## 1. File Overview & Purpose

This is a small, presentational component that displays the **read receipt status** (Sent, Delivered, Read) for a chat message.

## 2. Architectural Role

This is a **View** sub-component, used within `MessageView`.

## 3. Key Features & Logic

- **Conditional Rendering:** It only renders if the message was sent by the current user (`isSentByMe` prop is true).
- **Icon Logic:** It uses a `switch` statement on the `status` prop to determine which FontAwesome icon to display (`fa-check`, `fa-check-double`) and whether to apply a different color for the "Read" status.'),

('frontend/src/components/chat/MessageView.jsx',
'## 1. File Overview & Purpose

This is a large, complex component that renders the **main message view** for a single conversation on the `ChatPage`. It handles displaying message history, real-time message updates, and the message input form.

## 2. Architectural Role

This is a key **View** component.

## 3. Key Features & Logic

- **Data Fetching:** Uses `useApi` to fetch the initial message history from `/api/v1/public/chat/conversations/:id/messages`.
- **Real-Time Updates:** Uses the `useWebSocket` hook to connect to `/ws/dm/:conversationId`. The `handleWebSocketMessage` callback handles incoming messages (`new_message`, `message_updated`, `message_deleted`, `messages_status_updated`) and updates the local `messages` state accordingly.
- **Read Receipts:** An `useEffect` hook monitors the `messages` state. If it detects unread messages received from another user, it sends a `mark_as_read` event over the WebSocket to inform the server and other clients.
- **Message Rendering:** It maps over the `messages` array, rendering each one as a "bubble". It handles different styles for sent vs. received messages, displays sender info for group chats, and renders message content (including file links) via the `renderMessageContent` helper.
- **Message Actions:** It provides options for editing and deleting messages, which send `update_message` and `delete_message` events over the WebSocket.
- **Input Form:** Provides a controlled form for typing new messages and uploading files.

## 4. State Management

- **Message History:** Managed by `useState`, initialized by `useApi` and updated by `useWebSocket`.
- **Conversation Details:** Managed by a separate `useApi` hook.
- **UI State:** Uses `useState` for the message input, editing state, and modal visibility.');
COMMIT;