package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.FileDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventChatMessage;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebSocket endpoint for real-time event chats. Handles new messages, edits,
 * deletions, and document collaboration updates.
 */
@ServerEndpoint(value = "/ws/chat/{eventId}", configurator = GetHttpSessionConfigurator.class)
public class EventChatSocket {

	private static final Logger logger = LogManager.getLogger(EventChatSocket.class);
	private static final EventChatDAO chatDAO = new EventChatDAO();
	private static final EventDAO eventDAO = new EventDAO();
	private static final UserDAO userDAO = new UserDAO();
	private static final FileDAO fileDAO = new FileDAO();
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

	@OnOpen
	public void onOpen(Session session, @PathParam("eventId") String eventId, EndpointConfig config)
			throws IOException {
		User user = (User) config.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);

		// Security: Immediately close the connection if no authenticated user is found.
		if (user == null) {
			logger.warn("Unauthenticated attempt to open WebSocket. Closing session.");
			session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication required."));
			return;
		}

		session.getUserProperties().put(GetHttpSessionConfigurator.USER_PROPERTY_KEY, user);
		ChatSessionManager.getInstance().addSession(eventId, session);
	}

	@OnMessage
	public void onMessage(Session session, String message, @PathParam("eventId") String eventId) {
		User user = (User) session.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);
		if (user == null)
			return; // Should not happen due to onOpen check, but as a safeguard.

		try {
			Map<String, Object> messageMap = gson.fromJson(message, Map.class);
			String type = (String) messageMap.get("type");
			@SuppressWarnings("unchecked")
			Map<String, Object> payload = (Map<String, Object>) messageMap.get("payload");

			switch (type) {
			case "new_message":
				handleNewMessage(user, eventId, payload);
				break;
			case "update_message":
				handleUpdateMessage(user, eventId, payload);
				break;
			case "delete_message":
				handleDeleteMessage(user, eventId, payload);
				break;
			case "doc_update":
				handleDocUpdate(session, eventId, payload);
				break;
			default:
				logger.warn("Unknown WebSocket message type received: {}", type);
			}
		} catch (JsonSyntaxException e) {
			logger.error("Error processing message from user '{}'. Invalid JSON format.", user.getUsername(), e);
		}
	}

	/**
	 * Handles real-time updates for a shared document.
	 */
	private void handleDocUpdate(Session originSession, String eventId, Map<String, Object> payload) {
		// A special "event ID" of "0" is used for the global document to avoid
		// conflicts.
		if ("0".equals(eventId)) {
			String content = (String) payload.get("content");
			fileDAO.updateDocumentContent("realtime_notes", content);

			// Broadcast to other clients in the same "document room", excluding the sender.
			Map<String, Object> broadcastPayload = Map.of("type", "doc_update", "payload", payload);
			ChatSessionManager.getInstance().broadcastExcept(eventId, gson.toJson(broadcastPayload), originSession);
		}
	}

	/**
	 * Handles a new chat message, saves it, and broadcasts it to the room.
	 */
	private void handleNewMessage(User user, String eventId, Map<String, Object> payload) {
		String messageText = (String) payload.get("messageText");
		EventChatMessage newMessage = new EventChatMessage();
		newMessage.setEventId(Integer.parseInt(eventId));
		newMessage.setUserId(user.getId());
		newMessage.setUsername(user.getUsername());
		newMessage.setMessageText(messageText);

		EventChatMessage savedMessage = chatDAO.postMessage(newMessage);
		if (savedMessage == null) {
			logger.error("Failed to save new chat message for user {} in event {}", user.getId(), eventId);
			return;
		}

		Map<String, Object> broadcastPayload = Map.of("type", "new_message", "payload", savedMessage);
		ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));

		handleMentions(user, savedMessage);
	}

	/**
	 * Parses a message for @mentions and sends notifications.
	 */
	private void handleMentions(User sender, EventChatMessage message) {
		Event event = eventDAO.getEventById(message.getEventId());
		if (event == null)
			return;

		Matcher matcher = MENTION_PATTERN.matcher(message.getMessageText());
		while (matcher.find()) {
			String mentionedUsername = matcher.group(1);
			User mentionedUser = userDAO.getUserByUsername(mentionedUsername);
			if (mentionedUser != null && mentionedUser.getId() != sender.getId()) {
				String notificationMessage = String.format("%s hat Sie im Chat für '%s' erwähnt.", sender.getUsername(),
						event.getName());
				Map<String, Object> notificationPayload = Map.of("type", "mention", "payload",
						Map.of("message", notificationMessage, "url", "/veranstaltungen/details?id=" + event.getId()));
				NotificationService.getInstance().sendNotificationToUser(mentionedUser.getId(), notificationPayload);
				logger.info("Sent @mention notification from {} to {} for event chat {}.", sender.getUsername(),
						mentionedUsername, message.getEventId());
			}
		}
	}

	/**
	 * Handles a request to delete a message, checking permissions before doing so.
	 */
	private void handleDeleteMessage(User user, String eventId, Map<String, Object> payload) {
		int messageId = ((Double) payload.get("messageId")).intValue();

		Event event = eventDAO.getEventById(Integer.parseInt(eventId));
		boolean isEventLeader = event != null && event.getLeaderUserId() == user.getId();
		boolean canDeleteAsAdmin = user.getPermissions().contains("ACCESS_ADMIN_PANEL") || isEventLeader;

		if (chatDAO.deleteMessage(messageId, user.getId(), canDeleteAsAdmin)) {
			int originalUserId = ((Double) payload.get("originalUserId")).intValue();
			if (canDeleteAsAdmin && user.getId() != originalUserId) {
				String logDetails = String.format(
						"User '%s' deleted a chat message (ID: %d) in event chat for event '%s' (ID: %s).",
						user.getUsername(), messageId, event.getName(), eventId);
				AdminLogService.log(user.getUsername(), "DELETE_CHAT_MESSAGE", logDetails);
			}

			Map<String, Object> broadcastPayload = Map.of("type", "message_soft_deleted", "payload",
					Map.of("messageId", messageId, "originalUsername", payload.get("originalUsername"),
							"deletedByUsername", user.getUsername()));
			ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		} else {
			logger.warn("User {} failed to delete message {} in event {}. Permission was likely denied.",
					user.getUsername(), messageId, eventId);
		}
	}

	/**
	 * Handles a request to update a message's text, checking ownership.
	 */
	private void handleUpdateMessage(User user, String eventId, Map<String, Object> payload) {
		int messageId = ((Double) payload.get("messageId")).intValue();
		String newText = (String) payload.get("newText");

		if (chatDAO.updateMessage(messageId, user.getId(), newText)) {
			Map<String, Object> broadcastPayload = Map.of("type", "message_updated", "payload",
					Map.of("messageId", messageId, "newText", newText));
			ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		} else {
			logger.warn("User {} failed to update message {}. Not the owner or message deleted.", user.getUsername(),
					messageId);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason, @PathParam("eventId") String eventId) {
		User user = (User) session.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);
		String username = (user != null) ? user.getUsername() : "[unauthenticated]";
		logger.info("WebSocket session for user '{}' closed. Reason: {}", username, reason.getReasonPhrase());
		ChatSessionManager.getInstance().removeSession(eventId, session);
	}

	@OnError
	public void onError(Session session, Throwable throwable, @PathParam("eventId") String eventId) {
		logger.error("WebSocket ERROR in event [{}], session [{}]:", eventId, session.getId(), throwable);
	}
}