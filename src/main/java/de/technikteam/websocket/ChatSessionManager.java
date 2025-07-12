package de.technikteam.websocket;

import jakarta.websocket.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Manages WebSocket sessions for event-specific chat rooms using a thread-safe
 * Singleton pattern. It maps event IDs to a set of active sessions, allowing
 * for targeted broadcasting of messages.
 */
public final class ChatSessionManager {
	private static final Logger logger = LogManager.getLogger(ChatSessionManager.class);
	private static final ChatSessionManager INSTANCE = new ChatSessionManager();

	// A map where the key is the event ID and the value is a thread-safe set of
	// sessions for that event.
	private final Map<String, Set<Session>> sessionsByEvent = new ConcurrentHashMap<>();

	private ChatSessionManager() {
	}

	/**
	 * Gets the singleton instance of the ChatSessionManager.
	 *
	 * @return The single instance of this class.
	 */
	public static ChatSessionManager getInstance() {
		return INSTANCE;
	}

	/**
	 * Adds a new WebSocket session to a specific event chat room.
	 *
	 * @param eventId The ID of the event chat room.
	 * @param session The WebSocket session to add.
	 */
	public void addSession(String eventId, Session session) {
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
	public void removeSession(String eventId, Session session) {
		Set<Session> sessions = sessionsByEvent.get(eventId);
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
		Set<Session> sessions = sessionsByEvent.get(eventId);
		if (sessions != null) {
			logger.debug("Broadcasting to {} sessions in event chat [{}].", sessions.size(), eventId);
			for (Session session : sessions) {
				if (session.isOpen()) {
					try {
						session.getBasicRemote().sendText(message);
					} catch (IOException e) {
						logger.error("Error broadcasting to session {}:", session.getId(), e);
						// Consider removing the session here if an error occurs
					}
				}
			}
		}
	}

	/**
	 * Broadcasts a message to all open sessions in an event chat room, except for
	 * the originating session.
	 *
	 * @param eventId        The ID of the event chat room.
	 * @param message        The message to broadcast.
	 * @param excludeSession The session to exclude from the broadcast.
	 */
	public void broadcastExcept(String eventId, String message, Session excludeSession) {
		Set<Session> sessions = sessionsByEvent.get(eventId);
		if (sessions != null) {
			for (Session session : sessions) {
				if (session.isOpen() && !session.getId().equals(excludeSession.getId())) {
					try {
						session.getBasicRemote().sendText(message);
					} catch (IOException e) {
						logger.error("Error broadcasting (except self) to session {}:", session.getId(), e);
					}
				}
			}
		}
	}
}