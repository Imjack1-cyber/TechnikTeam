package de.technikteam.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketSessionManager {
	private static final Logger logger = LogManager.getLogger(ChatWebSocketSessionManager.class);

	private final Map<String, Set<WebSocketSession>> sessionsByConversation = new ConcurrentHashMap<>();

	public void addSession(String conversationId, WebSocketSession session) {
		sessionsByConversation.computeIfAbsent(conversationId, k -> new CopyOnWriteArraySet<>()).add(session);
		logger.info("Session {} registered for direct message chat [{}].", session.getId(), conversationId);
	}

	public void removeSession(String conversationId, WebSocketSession session) {
		Set<WebSocketSession> sessions = sessionsByConversation.get(conversationId);
		if (sessions != null) {
			sessions.remove(session);
			logger.info("Session {} removed from direct message chat [{}].", session.getId(), conversationId);
			if (sessions.isEmpty()) {
				sessionsByConversation.remove(conversationId);
				logger.info("Direct message chat room [{}] is now empty and has been removed.", conversationId);
			}
		}
	}

	public void broadcast(String conversationId, String message) {
		Set<WebSocketSession> sessions = sessionsByConversation.get(conversationId);
		if (sessions != null) {
			logger.debug("Broadcasting to {} sessions in direct message chat [{}].", sessions.size(), conversationId);
			TextMessage textMessage = new TextMessage(message);
			for (WebSocketSession session : sessions) {
				if (session.isOpen()) {
					try {
						session.sendMessage(textMessage);
					} catch (IOException e) {
						logger.error("Error broadcasting to session {}:", session.getId(), e);
					}
				}
			}
		}
	}
}