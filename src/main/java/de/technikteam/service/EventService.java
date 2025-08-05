package de.technikteam.service;

import de.technikteam.dao.AttachmentDAO;
import de.technikteam.dao.EventCustomFieldDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.ScheduledNotificationDAO;
import de.technikteam.model.Attachment;
import de.technikteam.model.Event;
import de.technikteam.model.EventCustomField;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {
	private static final Logger logger = LogManager.getLogger(EventService.class);

	private final EventDAO eventDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventCustomFieldDAO customFieldDAO;
	private final ConfigurationService configService;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final ScheduledNotificationDAO scheduledNotificationDAO;
	private final PolicyFactory richTextPolicy;

	@Autowired
	public EventService(EventDAO eventDAO, AttachmentDAO attachmentDAO, EventCustomFieldDAO customFieldDAO,
			ConfigurationService configService, AdminLogService adminLogService,
			NotificationService notificationService, ScheduledNotificationDAO scheduledNotificationDAO,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.eventDAO = eventDAO;
		this.attachmentDAO = attachmentDAO;
		this.customFieldDAO = customFieldDAO;
		this.configService = configService;
		this.adminLogService = adminLogService;
		this.notificationService = notificationService;
		this.scheduledNotificationDAO = scheduledNotificationDAO;
		this.richTextPolicy = richTextPolicy;
	}

	@Transactional
	public int createOrUpdateEvent(Event event, boolean isUpdate, User adminUser, String[] requiredCourseIds,
			String[] requiredPersons, String[] itemIds, String[] quantities, List<EventCustomField> customFields,
			MultipartFile file, String requiredRole, int reminderMinutes) throws SQLException, IOException {

		// Sanitize HTML content before saving
		if (event.getDescription() != null) {
			String sanitizedDescription = richTextPolicy.sanitize(event.getDescription());
			event.setDescription(sanitizedDescription);
		}

		int eventId;
		if (isUpdate) {
			eventDAO.updateEvent(event);
			eventId = event.getId();
			adminLogService.log(adminUser.getUsername(), "UPDATE_EVENT",
					"Event '" + event.getName() + "' (ID: " + eventId + ") aktualisiert.");
		} else {
			eventId = eventDAO.createEvent(event);
			event.setId(eventId);
			adminLogService.log(adminUser.getUsername(), "CREATE_EVENT",
					"Event '" + event.getName() + "' (ID: " + eventId + ") erstellt.");
		}

		eventDAO.saveSkillRequirements(eventId, requiredCourseIds, requiredPersons);
		eventDAO.saveReservations(eventId, itemIds, quantities);
		if (customFields != null) {
			customFieldDAO.saveCustomFieldsForEvent(eventId, customFields);
		}

		if (file != null && !file.isEmpty()) {
			handleAttachmentUpload(file, eventId, requiredRole, adminUser);
		}

		// Handle scheduled reminder
		List<Integer> participantIds = eventDAO.getAssignedUsersForEvent(eventId).stream().map(User::getId)
				.collect(Collectors.toList());
		LocalDateTime sendAt = reminderMinutes > 0 ? event.getEventDateTime().minusMinutes(reminderMinutes) : null;
		if (sendAt != null && sendAt.isAfter(LocalDateTime.now())) {
			scheduledNotificationDAO.createOrUpdateReminder("EVENT_REMINDER", eventId, participantIds, sendAt,
					"Erinnerung: " + event.getName(), "Diese Veranstaltung beginnt bald.",
					"/veranstaltungen/details/" + eventId);
		} else {
			// Delete existing reminder if it's no longer needed
			scheduledNotificationDAO.deleteReminders("EVENT_REMINDER", eventId);
		}

		logger.info("Transaction for event ID {} committed successfully.", eventId);
		return eventId;
	}

	public void assignUsersToEventAndNotify(int eventId, String[] userIds, User adminUser) {
		eventDAO.assignUsersToEvent(eventId, userIds);
		Event event = eventDAO.getEventById(eventId);
		String logDetails = String.format("Benutzer %s zu Event '%s' zugewiesen.", Arrays.toString(userIds),
				event.getName());
		adminLogService.log(adminUser.getUsername(), "ASSIGN_USERS_EVENT", logDetails);

		for (String userIdStr : userIds) {
			int userId = Integer.parseInt(userIdStr);
			String notificationMessage = String.format("Du wurdest zum Event '%s' zugewiesen.", event.getName());
			Map<String, Object> payload = Map.of("type", "assignment", "title", "Neue Zuweisung", "description",
					notificationMessage, "level", "Important", "url", "/veranstaltungen/details/" + eventId);
			notificationService.sendNotificationToUser(userId, payload);
		}
	}

	public void signOffUserFromRunningEvent(int userId, String username, int eventId, String reason) {
		eventDAO.signOffFromEvent(userId, eventId);
		Event event = eventDAO.getEventById(eventId);
		if (event != null && event.getLeaderUserId() > 0) {
			String notificationMessage = String.format("%s hat sich vom laufenden Event '%s' abgemeldet. Grund: %s",
					username, event.getName(), reason);

			Map<String, Object> payload = Map.of("type", "alert", "payload",
					Map.of("message", notificationMessage, "url", "/veranstaltungen/details/" + eventId));

			notificationService.sendNotificationToUser(event.getLeaderUserId(), payload);
			logger.info("Sent sign-off notification to event leader (ID: {}) for event '{}'", event.getLeaderUserId(),
					event.getName());
		}
	}

	private void handleAttachmentUpload(MultipartFile file, int eventId, String requiredRole, User adminUser)
			throws IOException {
		String uploadDir = configService.getProperty("upload.directory");
		String originalFileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
		String uniqueFileName = UUID.randomUUID() + "-" + originalFileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");

		Path targetPath = Paths.get(uploadDir, "events", uniqueFileName);
		Files.createDirectories(targetPath.getParent());
		Files.copy(file.getInputStream(), targetPath);

		Attachment attachment = new Attachment();
		attachment.setParentId(eventId);
		attachment.setParentType("EVENT");
		attachment.setFilename(originalFileName);
		attachment.setFilepath("events/" + uniqueFileName);
		attachment.setRequiredRole(requiredRole);

		if (attachmentDAO.addAttachment(attachment)) {
			adminLogService.log(adminUser.getUsername(), "ADD_EVENT_ATTACHMENT",
					"Anhang '" + originalFileName + "' zu Event ID " + eventId + " hinzugefügt.");
		} else {
			Files.deleteIfExists(targetPath);
			throw new RuntimeException("Fehler beim Speichern des Anhangs in der Datenbank.");
		}
	}
}