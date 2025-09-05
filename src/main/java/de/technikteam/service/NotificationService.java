package de.technikteam.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.api.v1.dto.MaintenanceStatusDTO;
import de.technikteam.api.v1.dto.NotificationRequest;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserNotificationDAO;
import de.technikteam.model.User;
import de.technikteam.model.UserNotification;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class NotificationService {
	private static final Logger logger = LogManager.getLogger(NotificationService.class);

	private final Map<Integer, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();
	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final MeetingDAO meetingDAO;
	private final AdminLogService adminLogService;
	private final UserNotificationDAO userNotificationDAO;

	public NotificationService(UserDAO userDAO, EventDAO eventDAO, MeetingDAO meetingDAO,
			AdminLogService adminLogService, UserNotificationDAO userNotificationDAO) {
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.meetingDAO = meetingDAO;
		this.adminLogService = adminLogService;
		this.userNotificationDAO = userNotificationDAO;
		new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();

        try {
            // Check if credentials are provided either via environment variable or default gcloud login
            if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null || GoogleCredentials.getApplicationDefault() != null) {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build();
                    FirebaseApp.initializeApp(options);
                    logger.info("Firebase Admin SDK initialized successfully.");
                }
            } else {
                 logger.warn("GOOGLE_APPLICATION_CREDENTIALS environment variable not set and Application Default Credentials not found. Firebase Admin SDK will not be initialized. Push notifications will be disabled.");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase Admin SDK. Push notifications will be disabled.", e);
        }
	}

	public SseEmitter register(User user) {
		if (user == null) {
			logger.warn("Versuch, Benachrichtigungen für eine nicht authentifizierte Sitzung zu registrieren.");
			return null;
		}

		// Timeout set to a very long value. The connection will be kept alive by
		// heartbeats.
		SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
		int userId = user.getId();

		emitter.onCompletion(() -> {
			logger.info("SSE Emitter für Benutzer {} beendet.", userId);
			removeEmitter(userId, emitter);
		});
		emitter.onTimeout(() -> {
			logger.warn("SSE Emitter für Benutzer {} hat Zeitüberschreitung.", userId);
			emitter.complete();
		});
		emitter.onError(e -> {
			// This often logs benign client-side disconnects, so we log at debug level.
			logger.debug("SSE Emitter Fehler für Benutzer {}: {}", userId, e.getMessage());
			// The emitter is completed by the container/Spring, no need to call complete()
			// here.
		});

		emittersByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

		logger.info(
				"Neuer Client für SSE-Benachrichtigungen für Benutzer-ID {} registriert. Gesamtzahl der Clients für Benutzer: {}",
				userId, emittersByUser.get(userId).size());

		// When a user connects via SSE, we can consider their unseen notifications
		// "seen".
		userNotificationDAO.markAllAsSeen(userId);

		// Send a confirmation event
		try {
			emitter.send(SseEmitter.event().name("connected").data("Verbindung hergestellt"));
		} catch (Exception e) {
			logger.error("Fehler beim Senden der Verbindungsbestätigung an Benutzer {}", userId, e);
			emitter.complete();
		}

		return emitter;
	}

	@Scheduled(fixedRate = 30000) // Run every 30 seconds
	public void sendHeartbeat() {
		SseEmitter.SseEventBuilder heartbeatEvent = SseEmitter.event().comment("heartbeat");
		emittersByUser.values().forEach(emitterList -> emitterList.forEach(emitter -> {
			try {
				emitter.send(heartbeatEvent);
			} catch (IOException e) {
                if (e instanceof ClientAbortException) {
                    logger.debug("Heartbeat failed for a client (ClientAbortException), it has likely disconnected. Emitter will be removed by its onCompletion handler.");
                } else {
                    logger.warn("Heartbeat failed for a client, it has likely disconnected. Error: {}", e.getMessage());
                }
            } catch (Exception e) {
				logger.debug(
						"Heartbeat failed for a client, it has likely disconnected. Emitter will be removed by its onCompletion handler.");
			}
		}));
	}

	public void broadcastUIUpdate(String type, Object payload) {
		logger.info("Sende UI-Update vom Typ '{}' an alle Clients.", type);
		Map<String, Object> message = Map.of("updateType", type, "data", payload);
		SseEmitter.SseEventBuilder event = SseEmitter.event().name("ui_update").data(message);

		emittersByUser.values().forEach(emitterList -> emitterList.forEach(emitter -> {
			try {
				emitter.send(event);
			} catch (Exception e) {
				logger.warn("Fehler beim Senden an einen Client (wahrscheinlich getrennt), wird entfernt. Fehler: {}",
						e.getMessage());
				// Do not complete the emitter here, let its own lifecycle handlers manage it.
			}
		}));
	}

	public void sendNotificationToUser(int userId, Map<String, Object> payload) {
		// 1. Persist the notification
		UserNotification notification = new UserNotification();
		notification.setUserId(userId);
		notification.setTitle((String) payload.get("title"));
		notification.setDescription((String) payload.get("description"));
		notification.setLevel((String) payload.get("level"));
		notification.setUrl((String) payload.get("url"));
		userNotificationDAO.create(notification);

		// 2. Push via SSE if user is connected
		List<SseEmitter> userEmitters = emittersByUser.get(userId);
		if (userEmitters != null && !userEmitters.isEmpty()) {
			SseEmitter.SseEventBuilder event = SseEmitter.event().name("notification").data(payload);
			logger.info("Sende gezielte Benachrichtigung an Benutzer-ID {}: {}", userId, payload);

			userEmitters.forEach(emitter -> {
				try {
					emitter.send(event);
				} catch (Exception e) {
					logger.warn(
							"Fehler beim Senden der gezielten Benachrichtigung an Benutzer {} (Client wahrscheinlich getrennt), wird entfernt. Fehler: {}",
							userId, e.getMessage());
					// Do not complete the emitter here, let its own lifecycle handlers manage it.
				}
			});
		} else {
			logger.debug("Keine aktiven SSE-Clients für Benutzer-ID {} gefunden, um Benachrichtigung zu senden.",
					userId);
		}
        // 3. Send FCM Push Notification if token exists
        User user = userDAO.getUserById(userId);
        if (user != null) {
            sendFcmNotification(user, payload);
        }
	}

	private void sendFcmNotification(User user, Map<String, Object> payload) {
        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            return; // No token, nothing to do
        }
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase not initialized, skipping push notification for user {}", user.getUsername());
            return;
        }

        String title = (String) payload.get("title");
        String description = (String) payload.get("description");
        String url = (String) payload.get("url");

        Message message = Message.builder()
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(description)
                .build())
            .setToken(user.getFcmToken())
            .putData("url", url != null ? url : "")
            .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent FCM message to user {}: {}", user.getUsername(), response);
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send FCM message to user {}: {}", user.getUsername(), e.getMessage());
            // Handle potential invalid token error
            if (e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.UNREGISTERED ||
                e.getMessagingErrorCode() == com.google.firebase.messaging.MessagingErrorCode.INVALID_ARGUMENT) {
                logger.warn("FCM token for user {} seems invalid. Clearing it from database.", user.getUsername());
                userDAO.updateFcmToken(user.getId(), null);
            }
        }
    }


	public int sendBroadcastNotification(NotificationRequest request, User adminUser) {
		List<User> targetUsers;
		String targetDescription;

		switch (request.targetType()) {
		case "ALL":
			targetUsers = userDAO.getAllUsers();
			targetDescription = "alle Benutzer";
			break;
		case "EVENT":
			if (request.targetId() == null)
				throw new IllegalArgumentException("Event-ID ist erforderlich.");
			targetUsers = eventDAO.getAssignedUsersForEvent(request.targetId());
			targetDescription = "Teilnehmer des Events ID " + request.targetId();
			break;
		case "MEETING":
			if (request.targetId() == null)
				throw new IllegalArgumentException("Meeting-ID ist erforderlich.");
			targetUsers = meetingDAO.getEnrolledUsersForMeeting(request.targetId());
			targetDescription = "Teilnehmer des Meetings ID " + request.targetId();
			break;
		default:
			throw new IllegalArgumentException("Ungültiger Zieltyp: " + request.targetType());
		}

		if (targetUsers.isEmpty()) {
			logger.warn("Keine Empfänger für die Benachrichtigung gefunden (Ziel: {}).", targetDescription);
			return 0;
		}

		Map<String, Object> payload = Map.of("title", request.title(), "description", request.description(), "level",
				request.level());

		for (User targetUser : targetUsers) {
			sendNotificationToUser(targetUser.getId(), payload);
		}

		String logDetails = String.format("Benachrichtigung gesendet an '%s'. Titel: %s, Stufe: %s", targetDescription,
				request.title(), request.level());
		adminLogService.log(adminUser.getUsername(), "SEND_NOTIFICATION", logDetails);

		return targetUsers.size();
	}
	
	public void broadcastSystemStatusUpdate(MaintenanceStatusDTO status) {
		logger.info("Broadcasting system status update to all clients: mode={}, message='{}'", status.mode(), status.message());
		SseEmitter.SseEventBuilder event = SseEmitter.event().name("system_status_update").data(status);

		emittersByUser.values().forEach(emitterList -> emitterList.forEach(emitter -> {
			try {
				emitter.send(event);
			} catch (Exception e) {
				logger.warn("Error broadcasting system status update to a client: {}", e.getMessage());
			}
		}));
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