// src/main/java/de/technikteam/websocket/EventChatSocket.java
package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.config.Permissions;
import de.technikteam.dao.EventChatDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventChatMessage;
import de.technikteam.model.User;
import de.technikteam.service.AdminLogService;
import de.technikteam.service.NotificationService;
import de.technikteam.util.MarkdownUtil;
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

@ServerEndpoint(value = "/ws/chat/{eventId}", configurator = GuiceAwareServerEndpointConfigurator.class)
public class EventChatSocket {

	private static final Logger logger = LogManager.getLogger(EventChatSocket.class);
	private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

	// Injected dependencies
	private static EventChatDAO chatDAO;
	private static EventDAO eventDAO;
	private static UserDAO userDAO;
	private static AdminLogService adminLogService;
	private static NotificationService notificationService;
	private static Gson gson;

	@Inject
	public static void setDependencies(EventChatDAO injectedChatDAO, EventDAO injectedEventDAO, UserDAO injectedUserDAO,
			AdminLogService injectedAdminLogService, NotificationService injectedNotificationService) {
		chatDAO = injectedChatDAO;
		eventDAO = injectedEventDAO;
		userDAO = injectedUserDAO;
		adminLogService = injectedAdminLogService;
		notificationService = injectedNotificationService;
		gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@OnOpen
	public void onOpen(Session session, @PathParam("eventId") String eventIdStr, EndpointConfig config)
			throws IOException {
		User user = (User) config.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);

		if (user == null) {
			session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication required."));
			return;
		}

		try {
			int eventId = Integer.parseInt(eventIdStr);
			if (!eventDAO.isUserAssociatedWithEvent(eventId, user.getId())) {
				session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Permission denied."));
				return;
			}

			session.getUserProperties().put(GetHttpSessionConfigurator.USER_PROPERTY_KEY, user);
			ChatSessionManager.getInstance().addSession(eventIdStr, session);

		} catch (NumberFormatException e) {
			session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Invalid event ID format."));
		}
	}

	@OnMessage
	public void onMessage(Session session, String message, @PathParam("eventId") String eventId) {
		User user = (User) session.getUserProperties().get(GetHttpSessionConfigurator.USER_PROPERTY_KEY);
		if (user == null)
			return;

		try {
			@SuppressWarnings("unchecked")
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
			}
		} catch (JsonSyntaxException e) {
			logger.error("Error processing message from user '{}'. Invalid JSON format.", user.getUsername(), e);
		}
	}

	private void handleNewMessage(User user, String eventId, Map<String, Object> payload) {
		String messageText = (String) payload.get("messageText");
		String sanitizedMessage = MarkdownUtil.sanitize(messageText);
		EventChatMessage newMessage = new EventChatMessage();
		newMessage.setEventId(Integer.parseInt(eventId));
		newMessage.setUserId(user.getId());
		newMessage.setUsername(user.getUsername());
		newMessage.setMessageText(sanitizedMessage);
		EventChatMessage savedMessage = chatDAO.postMessage(newMessage);
		if (savedMessage == null)
			return;
		Map<String, Object> broadcastPayload = Map.of("type", "new_message", "payload", savedMessage);
		ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		handleMentions(user, savedMessage);
	}

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
				notificationService.sendNotificationToUser(mentionedUser.getId(), notificationPayload);
			}
		}
	}

	private void handleDeleteMessage(User user, String eventId, Map<String, Object> payload) {
		int messageId = ((Double) payload.get("messageId")).intValue();
		Event event = eventDAO.getEventById(Integer.parseInt(eventId));
		boolean isEventLeader = event != null && event.getLeaderUserId() == user.getId();
		boolean canDeleteAsAdmin = user.getPermissions().contains(Permissions.ACCESS_ADMIN_PANEL) || isEventLeader;
		if (chatDAO.deleteMessage(messageId, user.getId(), canDeleteAsAdmin)) {
			int originalUserId = ((Double) payload.get("originalUserId")).intValue();
			if (canDeleteAsAdmin && user.getId() != originalUserId && event != null) {
				String logDetails = String.format(
						"Admin '%s' deleted chat message (ID: %d) in event chat for event '%s' (ID: %s).",
						user.getUsername(), messageId, event.getName(), eventId);
				adminLogService.log(user.getUsername(), "DELETE_CHAT_MESSAGE", logDetails);
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
		String sanitizedText = MarkdownUtil.sanitize(newText);
		if (chatDAO.updateMessage(messageId, user.getId(), sanitizedText)) {
			Map<String, Object> broadcastPayload = Map.of("type", "message_updated", "payload",
					Map.of("messageId", messageId, "newText", sanitizedText));
			ChatSessionManager.getInstance().broadcast(eventId, gson.toJson(broadcastPayload));
		}
	}

	@OnClose
	public void onClose(Session session, @PathParam("eventId") String eventId) {
		ChatSessionManager.getInstance().removeSession(eventId, session);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		logger.error("WebSocket ERROR in session [{}]:", session.getId(), throwable);
	}
}