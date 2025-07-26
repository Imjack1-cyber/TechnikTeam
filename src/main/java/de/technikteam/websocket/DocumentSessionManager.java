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
 * Manages WebSocket sessions for document-specific editing rooms using a
 * thread-safe Singleton pattern. It maps file IDs to a set of active sessions,
 * allowing for targeted broadcasting of document updates.
 */
public final class DocumentSessionManager {
	private static final Logger logger = LogManager.getLogger(DocumentSessionManager.class);
	private static final DocumentSessionManager INSTANCE = new DocumentSessionManager();

	private final Map<String, Set<Session>> sessionsByFile = new ConcurrentHashMap<>();

	private DocumentSessionManager() {
	}

	public static DocumentSessionManager getInstance() {
		return INSTANCE;
	}

	public void addSession(String fileId, Session session) {
		sessionsByFile.computeIfAbsent(fileId, k -> new CopyOnWriteArraySet<>()).add(session);
		logger.info("Editor session {} registered for file [{}].", session.getId(), fileId);
	}

	public void removeSession(String fileId, Session session) {
		Set<Session> sessions = sessionsByFile.get(fileId);
		if (sessions != null) {
			sessions.remove(session);
			logger.info("Editor session {} removed from file [{}].", session.getId(), fileId);
			if (sessions.isEmpty()) {
				sessionsByFile.remove(fileId);
				logger.info("Editing room for file [{}] is now empty and has been removed.", fileId);
			}
		}
	}

	public int getSessionsCount(String fileId) {
		Set<Session> sessions = sessionsByFile.get(fileId);
		return sessions != null ? sessions.size() : 0;
	}

	public void broadcastExcept(String fileId, String message, Session excludeSession) {
		Set<Session> sessions = sessionsByFile.get(fileId);
		if (sessions != null) {
			for (Session session : sessions) {
				if (session.isOpen() && !session.getId().equals(excludeSession.getId())) {
					try {
						session.getBasicRemote().sendText(message);
					} catch (IOException e) {
						logger.error("Error broadcasting document update to session {}:", session.getId(), e);
					}
				}
			}
		}
	}
}