package de.technikteam.servlet.http;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple, centralized manager for active HttpSession objects. This allows
 * other parts of the application to find and invalidate a specific user's
 * session.
 */
public class SessionManager {

	private static final Map<String, HttpSession> SESSIONS = new ConcurrentHashMap<>();

	public static void addSession(HttpSession session) {
		SESSIONS.put(session.getId(), session);
	}

	public static void removeSession(HttpSession session) {
		SESSIONS.remove(session.getId());
	}

	/**
	 * Finds and invalidates all active sessions for a given user ID. This is useful
	 * for forcing a user to re-authenticate after critical profile changes.
	 * 
	 * @param userId The ID of the user whose sessions should be invalidated.
	 */
	public static void invalidateSessionsForUser(int userId) {
		SESSIONS.values().stream().filter(session -> {
			try {
				de.technikteam.model.User user = (de.technikteam.model.User) session.getAttribute("user");
				return user != null && user.getId() == userId;
			} catch (IllegalStateException e) {
				// Session might already be invalid, safe to ignore and remove
				return false;
			}
		}).forEach(session -> {
			try {
				session.invalidate();
			} catch (IllegalStateException e) {
				// Session was already invalidated, no action needed
			}
		});
	}
}