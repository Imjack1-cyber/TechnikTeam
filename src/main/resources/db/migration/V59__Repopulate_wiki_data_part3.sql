-- Flyway migration V59, Part 3: Overhaul Technical Wiki Documentation

INSERT INTO `wiki_documentation` (`file_path`, `content`) VALUES
('src/main/java/de/websocket/ChatSessionManager.java',
'## 1. File Overview & Purpose

This class is a thread-safe singleton manager for WebSocket sessions related to **event-specific** chats. It maintains a map of active chat rooms, where each room (keyed by an `eventId`) contains a set of connected user sessions. This allows for targeted message broadcasting to all participants in a specific event''s chat.

## 2. Architectural Role

This is an **Infrastructure** component for the real-time communication feature, operating within the **Web/Controller Tier**. It is used by the `EventChatSocketHandler` to manage session lifecycle and broadcast messages.

## 3. Key Dependencies & Libraries

- **Spring WebSockets (`org.springframework.web.socket.WebSocketSession`)**: The object representing a single client connection.
- `java.util.concurrent.ConcurrentHashMap` & `CopyOnWriteArraySet`: Thread-safe collection classes are used to safely manage sessions from multiple concurrent WebSocket threads.

## 4. In-Depth Breakdown

- **`sessionsByEvent` (Map)**: The central data structure. The key is the `eventId` as a string, and the value is a `CopyOnWriteArraySet` of `WebSocketSession` objects. `CopyOnWriteArraySet` is chosen for its thread-safety, being particularly efficient when reads and iterations are more common than writes (add/remove).
- **`addSession(String eventId, WebSocketSession session)`**: Adds a new user''s session to the set for the corresponding event room.
- **`removeSession(String eventId, WebSocketSession session)`**: Removes a user''s session when they disconnect. If a room becomes empty, it is removed from the main map to conserve memory.
- **`broadcast(String eventId, String message)`**: Sends a message to *every* active and open session in a specific event room.'),

('src/main/java/de/websocket/ChatWebSocketHandler.java',
'## 1. File Overview & Purpose

This is the WebSocket endpoint that powers the real-time **direct messaging and group chat** feature. It manages client connections for specific conversation rooms, processes incoming messages (new, edit, delete, read receipts), persists them to the database via `ChatDAO`, and broadcasts them to all participants in the room.

## 2. Architectural Role

This is a key component of the **Web/Controller Tier**. It provides the real-time communication layer for the `/chat` page. It interacts with the `ChatWebSocketSessionManager` for session handling and the `ChatDAO` for data persistence.

## 3. Key Dependencies & Libraries

- **Spring WebSockets (`TextWebSocketHandler`)**: The base class for handling text-based WebSocket communication.
- `ChatWebSocketSessionManager`: To manage sessions and broadcast messages.
- `ChatDAO`: To save, update, and delete messages, and to authorize users for conversations.
- `NotificationService`: To send out-of-app notifications to users about new messages.
- **Gson**: For parsing incoming JSON messages from the client.

## 4. In-Depth Breakdown

- **`afterConnectionEstablished(...)`**: Handles a new client connection. It extracts the `conversationId` from the URL and the `User` from the security principal. It performs an authorization check using `chatDAO.isUserInConversation` before adding the session to the `ChatWebSocketSessionManager`.
- **`handleTextMessage(...)`**: The main message router. It parses the incoming JSON message and delegates to a specific handler based on the message `type`.
- **`handleNewMessage(...)`**: Persists a new message via the DAO and then broadcasts a `new_message` event to all clients in the conversation. It also triggers an out-of-app notification via the `NotificationService`.
- **`handleMarkAsRead(...)`**: Updates the status of multiple messages in the database and broadcasts a `messages_status_updated` event so clients can update their UI to show the "read" status.
- **`handleUpdateMessage(...)`**: Handles a message edit request. It performs a DAO update (which includes a check to ensure only the original author can edit within a 24-hour window) and then broadcasts the `message_updated` event.
- **`handleDeleteMessage(...)`**: Performs a soft delete in the database and broadcasts a `message_deleted` event.'),

('src/main/java/de/websocket/ChatWebSocketSessionManager.java',
'## 1. File Overview & Purpose

This class is a thread-safe singleton manager for WebSocket sessions related to the **direct messaging and group chat** feature. It is functionally identical to `ChatSessionManager` but operates on `conversationId`s instead of `eventId`s, keeping the two real-time features isolated.

## 2. Architectural Role

This is an **Infrastructure** component within the **Web/Controller Tier**. It is used exclusively by the `ChatWebSocketHandler` to manage session lifecycle and broadcast updates for the `/chat` page.

## 3. Key Dependencies & Libraries

- **Spring WebSockets (`org.springframework.web.socket.WebSocketSession`)**: The object representing a single client connection.
- `java.util.concurrent.ConcurrentHashMap` & `CopyOnWriteArraySet`: Thread-safe collections for managing sessions.

## 4. In-Depth Breakdown

- **`sessionsByConversation` (Map)**: The central map where the key is the `conversationId` (as a string) and the value is a `CopyOnWriteArraySet` of `WebSocketSession` objects.
- **`addSession(...)`, `removeSession(...)`, `broadcast(...)`**: These methods provide the core functionality for adding, removing, and broadcasting messages to all sessions within a specific conversation room.'),

('src/main/java/de/websocket/EventChatSocketHandler.java',
'## 1. File Overview & Purpose

This is the WebSocket endpoint that powers the real-time **event-specific chat** feature (the chat tab on an event''s detail page). It manages client connections for event chat rooms, processes incoming messages (new, edit, delete), persists them to the database, and broadcasts them to all participants in the room.

## 2. Architectural Role

This is a key component of the **Web/Controller Tier**. It provides the real-time communication layer for events. It interacts with the `ChatSessionManager` for session management and various DAOs (`EventChatDAO`, `EventDAO`) for data persistence and validation.

## 3. Key Dependencies & Libraries

- **Spring WebSockets (`TextWebSocketHandler`)**: The base class for WebSocket handlers.
- `ChatSessionManager`: To manage sessions for event rooms.
- `EventChatDAO`: To save, update, and delete messages in the database.
- `EventDAO`: To verify that a connecting user is actually a participant of the event.
- `NotificationService`: To send out-of-app notifications for user mentions.

## 4. In-Depth Breakdown

- **`afterConnectionEstablished(...)`**: Handles new connections. It authorizes the user by checking if they are associated with the event (`eventDAO.isUserAssociatedWithEvent`). If so, it adds their session to the `ChatSessionManager`.
- **`handleTextMessage(...)`**: The main message router. It parses the incoming JSON message and delegates to a specific handler based on the message `type` (`new_message`, `update_message`, `delete_message`).
- **`handleNewMessage(...)`**: Sanitizes the message content, saves it to the database, broadcasts the saved message to all clients in the room, and calls `handleMentions()`.
- **`handleUpdateMessage(...)`**: Handles a message edit request, updating it in the database and broadcasting the change.
- **`handleDeleteMessage(...)`**: Handles a message delete request, performing a soft delete and broadcasting the change. It includes logic to check if the deleter is an admin or the event leader.
- **`handleMentions(...)`**: Parses the message text for `@username` patterns and sends targeted, out-of-app notifications via the `NotificationService`.'),

('src/main/java/de/websocket/WebSocketConfig.java',
'## 1. File Overview & Purpose

This class configures the WebSocket endpoints for the Spring application. It registers the different `WebSocketHandler` classes and maps them to specific URL paths.

## 2. Architectural Role

This is a **Configuration** file for the **Web/API Tier**. It is responsible for setting up the WebSocket infrastructure.

## 3. Key Dependencies & Libraries

- **Spring WebSockets (`@EnableWebSocket`, `WebSocketConfigurer`)**: The core components for enabling and configuring WebSockets in Spring.
- `EventChatSocketHandler`, `ChatWebSocketHandler`: The specific handler beans that are being registered.

## 4. In-Depth Breakdown

- **`@EnableWebSocket`**: This annotation enables WebSocket support in the Spring application.
- **`registerWebSocketHandlers(WebSocketHandlerRegistry registry)`**: This is the main configuration method.
    - `registry.addHandler(eventChatSocketHandler, "/ws/chat/{eventId}")`: This line maps the `EventChatSocketHandler` to the URL pattern `/ws/chat/{eventId}`. The `{eventId}` part is a path parameter that allows each event to have its own dedicated chat room.
    - `registry.addHandler(chatWebSocketHandler, "/ws/dm/{conversationId}")`: This maps the `ChatWebSocketHandler` to the URL pattern `/ws/dm/{conversationId}` for the direct messaging feature.
    - `.setAllowedOrigins("*")`: This configures CORS for the WebSocket connections, allowing connections from any origin. In a production environment, this should be restricted to the specific domain of the frontend application.');
COMMIT;