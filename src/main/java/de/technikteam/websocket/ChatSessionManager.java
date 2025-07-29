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

/**
 * Manages WebSocket sessions for event-specific chat rooms as a thread-safe
 * Spring Component. It maps event IDs to a set of active sessions, allowing for
 * targeted broadcasting of messages.
 */
@Component
public class ChatSessionManager {
	private static final Logger logger = LogManager.getLogger(ChatSessionManager.class);

	private final Map<String, Set<WebSocketSession>> sessionsByEvent = new ConcurrentHashMap<>();

	/**
	 * Adds a new WebSocket session to a specific event chat room.
	 *
	 * @param eventId The ID of the event chat room.
	 * @param session The WebSocket session to add.
	 */
	public void addSession(String eventId, WebSocketSession session) {
		sessionsByEvent.computeIfAbsent(eventId, k -> new CopyOnWriteArraySet<>()).add(session);
		logger.info("Session {} registered for event chat [{}].", session.getId(), eventId);
	}

	/**
	 * Removes a WebSocket session from an event chat room. If the room becomes
	 * empty, it is removed from the map to conserve memory.
	 *
	 * @param eventId The ID of the event chat room.
	 * @param session The WebSocket session to remove.
	 */
	public void removeSession(String eventId, WebSocketSession session) {
		Set<WebSocketSession> sessions = sessionsByEvent.get(eventId);
		if (sessions != null) {
			sessions.remove(session);
			logger.info("Session {} removed from event chat [{}].", session.getId(), eventId);
			if (sessions.isEmpty()) {
				sessionsByEvent.remove(eventId);
				logger.info("Event chat room [{}] is now empty and has been removed.", eventId);
			}
		}
	}

	/**
	 * Broadcasts a message to all open sessions in a specific event chat room.
	 *
	 * @param eventId The ID of the event chat room.
	 * @param message The message to broadcast.
	 */
	public void broadcast(String eventId, String message) {
		Set<WebSocketSession> sessions = sessionsByEvent.get(eventId);
		if (sessions != null) {
			logger.debug("Broadcasting to {} sessions in event chat [{}].", sessions.size(), eventId);
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