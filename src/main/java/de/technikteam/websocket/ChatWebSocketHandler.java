package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.technikteam.api.v1.dto.NotificationPayload;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.ChatDAO;
import de.technikteam.model.ChatConversation;
import de.technikteam.model.ChatMessage;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import de.technikteam.service.NotificationService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LogManager.getLogger(ChatWebSocketHandler.class);
	private final ChatDAO chatDAO;
	private final ChatWebSocketSessionManager sessionManager;
	private final NotificationService notificationService;
	private final Gson gson;

	@Autowired
	public ChatWebSocketHandler(ChatDAO chatDAO, ChatWebSocketSessionManager sessionManager,
			NotificationService notificationService) {
		this.chatDAO = chatDAO;
		this.sessionManager = sessionManager;
		this.notificationService = notificationService;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		User user = getUserFromSession(session);
		String conversationId = getConversationId(session);

		if (user == null || conversationId == null) {
			session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing user or conversation ID."));
			return;
		}

		if (!chatDAO.isUserInConversation(Integer.parseInt(conversationId), user.getId())) {
			session.close(CloseStatus.POLICY_VIOLATION.withReason("Unauthorized for this conversation."));
			return;
		}

		session.getAttributes().put("user", user);
		session.getAttributes().put("conversationId", conversationId);
		sessionManager.addSession(conversationId, session);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		User user = (User) session.getAttributes().get("user");
		String conversationIdStr = (String) session.getAttributes().get("conversationId");
		if (user == null || conversationIdStr == null)
			return;
		int conversationId = Integer.parseInt(conversationIdStr);

		try {
			Map<String, Object> payload = gson.fromJson(message.getPayload(), new TypeToken<Map<String, Object>>() {
			}.getType());
			String type = (String) payload.get("type");
			Map<String, Object> data = (Map<String, Object>) payload.get("payload");
			if (data == null) {
				data = payload;
			}

			switch (type) {
			case "new_message":
				handleNewMessage(user, conversationId, (String) data.get("messageText"));
				break;
			case "mark_as_read":
				if (data.get("messageIds") instanceof List) {
					handleMarkAsRead(user, conversationId, (List<Double>) data.get("messageIds"));
				}
				break;
			case "update_message":
				handleUpdateMessage(user, conversationId, data);
				break;
			case "delete_message":
				handleDeleteMessage(user, conversationId, data);
				break;
			}
		} catch (JsonSyntaxException | ClassCastException | NullPointerException e) {
			logger.warn("Bad payload from {}: {}", user.getUsername(), e.getMessage());
		}
	}

	private void handleNewMessage(User user, int conversationId, String text) {
		if (text != null && !text.isBlank()) {
			ChatMessage chatMessage = new ChatMessage();
			chatMessage.setConversationId(conversationId);
			chatMessage.setSenderId(user.getId());
			chatMessage.setMessageText(text);

			ChatMessage savedMessage = chatDAO.createMessage(chatMessage);
			ChatMessage fullMessage = chatDAO.getMessageById(savedMessage.getId());

			sessionManager.broadcast(String.valueOf(conversationId),
					gson.toJson(Map.of("type", "new_message", "payload", fullMessage)));
			notifyParticipants(fullMessage, user);
		}
	}

	private void handleMarkAsRead(User user, int conversationId, List<Double> messageIdsDouble) {
		if (messageIdsDouble == null || messageIdsDouble.isEmpty())
			return;
		List<Long> messageIds = messageIdsDouble.stream().map(Double::longValue).collect(Collectors.toList());

		boolean updated = chatDAO.updateMessagesStatusToRead(messageIds, conversationId, user.getId());
		if (updated) {
			Map<String, Object> updatePayload = Map.of("type", "messages_status_updated", "payload",
					Map.of("messageIds", messageIds, "newStatus", "READ"));
			sessionManager.broadcast(String.valueOf(conversationId), gson.toJson(updatePayload));
		}
	}

	private void handleUpdateMessage(User user, int conversationId, Map<String, Object> data) {
		if (data == null || !(data.get("messageId") instanceof Number))
			return;
		long messageId = ((Number) data.get("messageId")).longValue();
		String newText = (String) data.get("newText");

		if (chatDAO.updateMessage(messageId, user.getId(), newText)) {
			ChatMessage updatedMessage = chatDAO.getMessageById(messageId);
			if (updatedMessage == null)
				return;

			Map<String, Object> broadcastPayload = Map.of("type", "message_updated", "payload", updatedMessage);
			sessionManager.broadcast(String.valueOf(conversationId), gson.toJson(broadcastPayload));
		}
	}

	private void handleDeleteMessage(User user, int conversationId, Map<String, Object> data) {
		if (data == null || !(data.get("messageId") instanceof Number))
			return;
		long messageId = ((Number) data.get("messageId")).longValue();
		ChatConversation conversation = chatDAO.getConversationById(conversationId);
		boolean isAdmin = conversation != null && conversation.isGroupChat() && conversation.getCreatorId() != null
				&& conversation.getCreatorId() == user.getId();

		if (chatDAO.deleteMessage(messageId, user.getId(), isAdmin)) {
			ChatMessage deletedMessage = chatDAO.getMessageById(messageId);
			if (deletedMessage == null)
				return;

			Map<String, Object> broadcastPayload = Map.of("type", "message_deleted", "payload", deletedMessage);
			sessionManager.broadcast(String.valueOf(conversationId), gson.toJson(broadcastPayload));
		}
	}

	private void notifyParticipants(ChatMessage message, User sender) {
		ChatConversation conversation = chatDAO.getConversationById(message.getConversationId());
		if (conversation == null)
			return;

		List<User> participants = conversation.getParticipants();
		for (User participant : participants) {
			if (participant.getId() != sender.getId()) {
				String messageSnippet = message.getMessageText();
				if (messageSnippet.length() > 50) {
					messageSnippet = messageSnippet.substring(0, 47) + "...";
				}

				String title = conversation.isGroupChat() ? "Neue Nachricht in \"" + conversation.getName() + "\""
						: "Neue Nachricht von " + sender.getUsername();
                
                NotificationPayload payload = new NotificationPayload();
                payload.setTitle(title);
                payload.setDescription(messageSnippet);
                payload.setLevel("Informational");
                payload.setUrl("/chat/" + message.getConversationId());
				notificationService.sendNotificationToUser(participant.getId(), payload);
			}
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String conversationId = (String) session.getAttributes().get("conversationId");
		if (conversationId != null) {
			sessionManager.removeSession(conversationId, session);
		}
	}

	private User getUserFromSession(WebSocketSession session) {
		if (session.getPrincipal() instanceof Authentication auth) {
			if (auth.getPrincipal() instanceof SecurityUser securityUser) {
				return securityUser.getUser();
			}
		}
		return null;
	}

	private String getConversationId(WebSocketSession session) {
		String path = session.getUri().getPath();
		String[] parts = path.split("/");
		if (parts.length > 0) {
			return parts[parts.length - 1];
		}
		return null;
	}
}