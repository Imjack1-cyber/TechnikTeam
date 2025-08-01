package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class NotificationService {
	private static final Logger logger = LogManager.getLogger(NotificationService.class);

	private final Map<Integer, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

	public NotificationService() {
		new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	public SseEmitter register(User user) {
		if (user == null) {
			logger.warn("Attempt to register for notifications from a non-authenticated session.");
			return null;
		}

		// Timeout set to a very long value. The connection will be kept alive by
		// heartbeats.
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		int userId = user.getId();

		emitter.onCompletion(() -> {
			logger.info("SSE Emitter completed for user {}", userId);
			removeEmitter(userId, emitter);
		});
		emitter.onTimeout(() -> {
			logger.warn("SSE Emitter timed out for user {}", userId);
			emitter.complete();
		});
		emitter.onError(e -> {
			logger.error("SSE Emitter error for user {}: {}", userId, e.getMessage());
			emitter.complete();
		});

		emittersByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		logger.info("New client registered for SSE notifications for user ID {}. Total clients for user: {}", userId,
				emittersByUser.get(userId).size());

		// Send a confirmation event
		try {
			emitter.send(SseEmitter.event().name("connected").data("Connection established"));
		} catch (IOException e) {
			logger.error("Error sending connection confirmation to user {}", userId, e);
			emitter.complete();
		}

		return emitter;
	}

	public void broadcastUIUpdate(String type, Object payload) {
		logger.info("Broadcasting UI update of type '{}' to all clients.", type);
		Map<String, Object> message = Map.of("updateType", type, "data", payload);
		SseEmitter.SseEventBuilder event = SseEmitter.event().name("ui_update").data(message);

		emittersByUser.values().forEach(emitterList -> emitterList.forEach(emitter -> {
			try {
				emitter.send(event);
			} catch (IOException e) {
				logger.warn("Failed to send broadcast to a client (likely disconnected), removing it. Error: {}",
						e.getMessage());
				emitter.complete();
			}
		}));
	}

	public void sendNotificationToUser(int userId, Map<String, Object> payload) {
		List<SseEmitter> userEmitters = emittersByUser.get(userId);
		if (userEmitters != null && !userEmitters.isEmpty()) {
			SseEmitter.SseEventBuilder event = SseEmitter.event().name("notification").data(payload);
			logger.info("Sending targeted notification to user ID {}: {}", userId, payload);

			userEmitters.forEach(emitter -> {
				try {
					emitter.send(event);
				} catch (IOException e) {
					logger.warn(
							"Failed to send targeted notification to user {} (client likely disconnected), removing it. Error: {}",
							userId, e.getMessage());
					emitter.complete();
				}
			});
		} else {
			logger.debug("No active SSE clients found for user ID {} to send notification.", userId);
		}
	}

	private void removeEmitter(int userId, SseEmitter emitter) {
		List<SseEmitter> userEmitters = emittersByUser.get(userId);
		if (userEmitters != null) {
			userEmitters.remove(emitter);
			if (userEmitters.isEmpty()) {
				emittersByUser.remove(userId);
			}
		}
	}
}