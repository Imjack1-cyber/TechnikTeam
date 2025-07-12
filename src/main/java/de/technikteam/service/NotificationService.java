package de.technikteam.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.model.User;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;

public class NotificationService {
	private static final Logger logger = LogManager.getLogger(NotificationService.class);
	private static final NotificationService INSTANCE = new NotificationService();
	private final Gson gson = new Gson();

	private final Map<Integer, List<AsyncContext>> contextsByUser = new ConcurrentHashMap<>();

	private NotificationService() {
	}

	public static NotificationService getInstance() {
		return INSTANCE;
	}

	public void register(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute("user");
		if (user == null) {
			logger.warn("Attempt to register for notifications from a non-authenticated session.");
			return;
		}

		AsyncContext asyncContext = request.startAsync();
		asyncContext.setTimeout(0);

		contextsByUser.computeIfAbsent(user.getId(), k -> new CopyOnWriteArrayList<>()).add(asyncContext);
		logger.info("New client registered for SSE notifications for user ID {}. Total clients for user: {}",
				user.getId(), contextsByUser.get(user.getId()).size());
	}

	public void sendNotification(String message) {
		logger.info("Broadcasting notification to all clients: {}", message);
		Map<String, Object> payload = Map.of("type", "generic", "payload", Map.of("message", message));
		String jsonMessage = gson.toJson(payload);
		contextsByUser.values().forEach(contextList -> {
			contextList.forEach(context -> sendMessageToContext(context, jsonMessage, contextList));
		});
	}

	public void sendNotificationToUser(int userId, Map<String, Object> payload) {
		List<AsyncContext> userContexts = contextsByUser.get(userId);
		if (userContexts != null && !userContexts.isEmpty()) {
			String jsonMessage = gson.toJson(payload);
			logger.info("Sending targeted notification to user ID {}: {}", userId, jsonMessage);
			userContexts.forEach(context -> sendMessageToContext(context, jsonMessage, userContexts));
		} else {
			logger.debug("No active SSE clients found for user ID {} to send notification.", userId);
		}
	}

	public void sendEventInvitation(int userId, String eventName, int eventId) {
		String message = String.format("Du wurdest zum Event '%s' eingeladen!", eventName);
		Map<String, Object> payload = Map.of("type", "event_invitation", "payload",
				Map.of("message", message, "url", "/veranstaltungen/details?id=" + eventId));
		sendNotificationToUser(userId, payload);
	}

	private void sendMessageToContext(AsyncContext context, String message, List<AsyncContext> contextList) {
		try {
			PrintWriter writer = context.getResponse().getWriter();
			writer.write("data: " + message + "\n\n");
			writer.flush();
		} catch (IOException e) {
			logger.warn("Failed to send notification to a client (likely disconnected), removing it.");
			contextList.remove(context);
		}
	}
}