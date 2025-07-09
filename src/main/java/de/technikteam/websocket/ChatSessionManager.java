package de.technikteam.websocket;

import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatSessionManager {
	private static final Logger logger = LogManager.getLogger(ChatSessionManager.class);
	private static final ChatSessionManager INSTANCE = new ChatSessionManager();

	private final Map<String, Set<Session>> sessionsByEvent = new ConcurrentHashMap<>();

	private ChatSessionManager() {
	}

	public static ChatSessionManager getInstance() {
		return INSTANCE;
	}

	public void addSession(String eventId, Session session) {
		sessionsByEvent.computeIfAbsent(eventId, k -> new CopyOnWriteArraySet<>()).add(session);
	}

	public void removeSession(String eventId, Session session) {
		Set<Session> sessions = sessionsByEvent.get(eventId);
		if (sessions != null) {
			sessions.remove(session);
			if (sessions.isEmpty()) {
				sessionsByEvent.remove(eventId);
			}
		}
	}

	public void broadcast(String eventId, String message) {
		Set<Session> sessions = sessionsByEvent.get(eventId);
		if (sessions != null) {
			logger.debug("Broadcasting to {} sessions in event chat [{}].", sessions.size(), eventId);
			for (Session session : sessions) {
				if (session.isOpen()) {
					try {
						session.getBasicRemote().sendText(message);
					} catch (IOException e) {
						logger.error("Error broadcasting to session {}:", session.getId(), e);
					}
				}
			}
		}
	}
}