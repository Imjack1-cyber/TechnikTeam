package de.technikteam.websocket;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.User;
import de.technikteam.security.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChecklistWebSocketHandler extends TextWebSocketHandler {

	private final EventDAO eventDAO;
	private final ChatSessionManager sessionManager; // Reusing this manager

	@Autowired
	public ChecklistWebSocketHandler(EventDAO eventDAO, ChatSessionManager sessionManager) {
		this.eventDAO = eventDAO;
		this.sessionManager = sessionManager;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		User user = getUserFromSession(session);
		String eventId = getEventId(session);

		if (user == null) {
			session.close(CloseStatus.POLICY_VIOLATION.withReason("Authentifizierung erforderlich."));
			return;
		}

		if (eventId == null || !eventDAO.isUserAssociatedWithEvent(Integer.parseInt(eventId), user.getId())) {
			session.close(CloseStatus.POLICY_VIOLATION.withReason("Keine Berechtigung für diese Event-Checkliste."));
			return;
		}

		session.getAttributes().put("eventId", eventId);
		sessionManager.addSession(eventId, session);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String eventId = (String) session.getAttributes().get("eventId");
		if (eventId != null) {
			sessionManager.removeSession(eventId, session);
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

	private String getEventId(WebSocketSession session) {
		String path = session.getUri().getPath();
		String[] parts = path.split("/");
		if (parts.length > 0) {
			return parts[parts.length - 1];
		}
		return null;
	}
}