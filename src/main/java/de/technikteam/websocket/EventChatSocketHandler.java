package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EventChatSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LogManager.getLogger(EventChatSocketHandler.class);
	private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

	private final EventChatDAO chatDAO;
	private final EventDAO eventDAO;
	private final UserDAO userDAO;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final ChatSessionManager sessionManager;
	private final Gson gson;

	@Autowired
	public EventChatSocketHandler(EventChatDAO chatDAO, EventDAO eventDAO, UserDAO userDAO,
			AdminLogService adminLogService, NotificationService notificationService,
			ChatSessionManager sessionManager) {
		this.chatDAO = chatDAO;
		this.eventDAO = eventDAO;
		this.userDAO = userDAO;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.sessionManager = sessionManager;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		User user = getUserFromSession(session);
		String eventId = getEventId(session);

		if (user == null) {
			session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentication required."));
			return;
		}

		if (eventId == null || !eventDAO.isUserAssociatedWithEvent(Integer.parseInt(eventId), user.getId())) {
			session.close(CloseStatus.POLICY_VIOLATION.withReason("Permission denied for this event chat."));
			return;
		}

		session.getAttributes().put("user", user);
		session.getAttributes().put("eventId", eventId);
		sessionManager.addSession(eventId, session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		User user = (User) session.getAttributes().get("user");
		String eventId = (String) session.getAttributes().get("eventId");
		if (user == null || eventId == null)
			return;

		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> messageMap = gson.fromJson(message.getPayload(), Map.class);
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
		sessionManager.broadcast(eventId, gson.toJson(broadcastPayload));
		handleMentions(user, savedMessage);
	}

	private void handleUpdateMessage(User user, String eventId, Map<String, Object> payload) {
		int messageId = ((Double) payload.get("messageId")).intValue();
		String newText = (String) payload.get("newText");
		String sanitizedText = MarkdownUtil.sanitize(newText);
		if (chatDAO.updateMessage(messageId, user.getId(), sanitizedText)) {
			Map<String, Object> broadcastPayload = Map.of("type", "message_updated", "payload",
					Map.of("messageId", messageId, "newText", sanitizedText));
			sessionManager.broadcast(eventId, gson.toJson(broadcastPayload));
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
			sessionManager.broadcast(eventId, gson.toJson(broadcastPayload));
		}
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
						Map.of("message", notificationMessage, "url", "/veranstaltungen/details/" + event.getId()));
				notificationService.sendNotificationToUser(mentionedUser.getId(), notificationPayload);
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		String eventId = (String) session.getAttributes().get("eventId");
		if (eventId != null) {
			sessionManager.removeSession(eventId, session);
		}
	}

	private User getUserFromSession(WebSocketSession session) {
		if (session.getPrincipal() instanceof Authentication) {
			Authentication auth = (Authentication) session.getPrincipal();
			if (auth.getPrincipal() instanceof User) {
				return (User) auth.getPrincipal();
			}
		}
		return null;
	}

	private String getEventId(WebSocketSession session) {
		// URI pattern is /ws/chat/{eventId}
		String path = session.getUri().getPath();
		String[] parts = path.split("/");
		if (parts.length > 0) {
			return parts[parts.length - 1];
		}
		return null;
	}
}