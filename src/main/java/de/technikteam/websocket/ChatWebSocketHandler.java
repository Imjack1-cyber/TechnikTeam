package de.technikteam.websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.ChatDAO;
import de.technikteam.model.ChatMessage;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
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

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private static final Logger logger = LogManager.getLogger(ChatWebSocketHandler.class);
	private final ChatDAO chatDAO;
	private final ChatWebSocketSessionManager sessionManager;
	private final Gson gson;

	@Autowired
	public ChatWebSocketHandler(ChatDAO chatDAO, ChatWebSocketSessionManager sessionManager) {
		this.chatDAO = chatDAO;
		this.sessionManager = sessionManager;
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
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		User user = (User) session.getAttributes().get("user");
		String conversationId = (String) session.getAttributes().get("conversationId");
		if (user == null || conversationId == null)
			return;

		try {
			Map<String, String> payload = gson.fromJson(message.getPayload(), Map.class);
			String text = payload.get("messageText");

			if (text != null && !text.isBlank()) {
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.setConversationId(Integer.parseInt(conversationId));
				chatMessage.setSenderId(user.getId());
				chatMessage.setMessageText(text); // Sanitization can be added here if needed

				ChatMessage savedMessage = chatDAO.createMessage(chatMessage);
				// Refetch full message to include username and timestamp
				ChatMessage fullMessage = chatDAO.getMessagesForConversation(savedMessage.getConversationId(), 1, 0)
						.get(0);

				sessionManager.broadcast(conversationId, gson.toJson(fullMessage));
			}
		} catch (JsonSyntaxException e) {
			logger.error("Invalid JSON received from user {}: {}", user.getUsername(), message.getPayload());
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