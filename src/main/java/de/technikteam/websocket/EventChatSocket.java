package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventChatMessage;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ServerEndpoint(value = "/ws/chat/{eventId}", configurator = GetHttpSessionConfigurator.class)
public class EventChatSocket {

	private static final Logger logger = LogManager.getLogger(EventChatSocket.class);
	private static final EventChatDAO chatDAO = new EventChatDAO();
	private static final EventDAO eventDAO = new EventDAO(); 
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

	@OnOpen
	public void onOpen(Session session, @PathParam("eventId") String eventId, EndpointConfig config) {
		User user = (User) config.getUserProperties().get("user");
		if (user == null) {
			try {
				session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication required."));
			} catch (IOException e) {
				logger.error("Error closing unauthenticated WebSocket session.", e);
			}
			return;
		}
		session.getUserProperties().put("user", user);
		ChatSessionManager.getInstance().addSession(eventId, session);
	}

	@OnMessage
	public void onMessage(Session session, String message, @PathParam("eventId") String eventId) {
		User user = (User) session.getUserProperties().get("user");
		if (user == null)
			return;

		try {
			Map<String, Object> messageMap = gson.fromJson(message, Map.class);
			String type = (String) messageMap.get("type");
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
			default:
				logger.warn("Unknown WebSocket message type received: {}", type);
			}
		} catch (JsonSyntaxException e) {
			logger.error("Error processing message from user '{}'. Invalid JSON format.", user.getUsername(), e);
		}
	}

	private void handleNewMessage(User user, String eventId, Map<String, Object> payload) {
		String messageText = (String) payload.get("messageText");
		EventChatMessage newMessage = new EventChatMessage();
		newMessage.setEventId(Integer.parseInt(eventId));
		newMessage.setUserId(user.getId());
		newMessage.setUsername(user.getUsername());
		newMessage.setMessageText(messageText);

		EventChatMessage savedMessage = chatDAO.postMessage(newMessage);

		if (savedMessage != null) {
			savedMessage.setChatColor(user.getChatColor()); 
			Map<String, Object> broadcastPayload = Map.of("type", "new_message", "payload", savedMessage);
			ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		}
	}

	private void handleDeleteMessage(User user, String eventId, Map<String, Object> payload) {
		int messageId = ((Double) payload.get("messageId")).intValue();
		boolean hasMasterPermission = user.getPermissions().contains("ACCESS_ADMIN_PANEL");

		Event event = eventDAO.getEventById(Integer.parseInt(eventId));
		boolean isEventLeader = event != null && event.getLeaderUserId() == user.getId();

		boolean canDeleteAsAdmin = hasMasterPermission || isEventLeader;

		if (chatDAO.deleteMessage(messageId, user.getId(), canDeleteAsAdmin)) {
			if (canDeleteAsAdmin && user.getId() != ((Double) payload.get("originalUserId")).intValue()) {
				String logDetails = String.format("User '%s' deleted a chat message (ID: %d) in event chat (ID: %s).",
						user.getUsername(), messageId, eventId);
				AdminLogService.log(user.getUsername(), "DELETE_CHAT_MESSAGE", logDetails);
			}

			Map<String, Object> broadcastPayload = Map.of("type", "message_soft_deleted", "payload",
					Map.of("messageId", messageId, "originalUsername", payload.get("originalUsername"),
							"deletedByUsername", user.getUsername()));
			ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		}
	}

	private void handleUpdateMessage(User user, String eventId, Map<String, Object> payload) {
		int messageId = ((Double) payload.get("messageId")).intValue();
		String newText = (String) payload.get("newText");

		if (chatDAO.updateMessage(messageId, user.getId(), newText)) {
			Map<String, Object> broadcastPayload = Map.of("type", "message_updated", "payload",
					Map.of("messageId", messageId, "newText", newText));
			ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason, @PathParam("eventId") String eventId) {
		User user = (User) session.getUserProperties().get("user");
		String username = (user != null) ? user.getUsername() : "[unauthenticated]";
		ChatSessionManager.getInstance().removeSession(eventId, session);
	}

	@OnError
	public void onError(Session session, Throwable throwable, @PathParam("eventId") String eventId) {
		logger.error("WebSocket ERROR in event [{}]:", eventId, throwable);
	}
}