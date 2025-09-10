package de.technikteam.service;

import com.google.gson.Gson;
import de.technikteam.api.v1.dto.EventAssignmentDTO;
import de.technikteam.api.v1.dto.NotificationPayload;
import de.technikteam.dao.*;
import de.technikteam.model.*;
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
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventService {
	private static final Logger logger = LogManager.getLogger(EventService.class);

	private final EventDAO eventDAO;
	private final EventTaskDAO taskDAO;
	private final MeetingDAO meetingDAO;
	private final AttachmentDAO attachmentDAO;
	private final EventCustomFieldDAO customFieldDAO;
	private final ChecklistDAO checklistDAO;
	private final ConfigurationService configService;
	private final AdminLogService adminLogService;
	private final NotificationService notificationService;
	private final ScheduledNotificationDAO scheduledNotificationDAO;
	private final PolicyFactory richTextPolicy;

	@Autowired
	public EventService(EventDAO eventDAO, EventTaskDAO taskDAO, MeetingDAO meetingDAO, AttachmentDAO attachmentDAO,
			EventCustomFieldDAO customFieldDAO, ChecklistDAO checklistDAO, ConfigurationService configService,
			AdminLogService adminLogService, NotificationService notificationService,
			ScheduledNotificationDAO scheduledNotificationDAO,
			@Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.eventDAO = eventDAO;
		this.taskDAO = taskDAO;
		this.meetingDAO = meetingDAO;
		this.attachmentDAO = attachmentDAO;
		this.customFieldDAO = customFieldDAO;
		this.checklistDAO = checklistDAO;
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
		checklistDAO.generateChecklistFromReservations(eventId); // Generate/update checklist
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

	@Transactional
	public void updateTeamAssignmentsAndNotify(int eventId, List<EventAssignmentDTO> newAssignments, User adminUser) {
		Event event = eventDAO.getEventById(eventId);
		if (event == null) {
			throw new IllegalArgumentException("Event not found.");
		}

		List<User> oldAssignees = event.getAssignedAttendees();
		List<Integer> oldAssigneeIds = oldAssignees.stream().map(User::getId).collect(Collectors.toList());
		List<Integer> newAssigneeIds = newAssignments.stream().map(EventAssignmentDTO::userId)
				.collect(Collectors.toList());

		eventDAO.updateTeamAssignments(eventId, newAssignments);

		String logDetails = String.format("Team for event '%s' (ID: %d) updated.", event.getName(), eventId);
		adminLogService.log(adminUser.getUsername(), "UPDATE_TEAM_ASSIGNMENTS", logDetails);

		// Notify newly added users
		for (Integer newUserId : newAssigneeIds) {
			if (!oldAssigneeIds.contains(newUserId)) {
                NotificationPayload payload = new NotificationPayload();
                payload.setTitle("Neue Zuweisung");
                payload.setDescription(String.format("Du wurdest zum Event '%s' zugewiesen.", event.getName()));
                payload.setLevel("Informational");
                payload.setUrl("/veranstaltungen/details/" + eventId);
				notificationService.sendNotificationToUser(newUserId, payload);
			}
		}
	}

	public void signOffUserFromRunningEvent(int userId, String username, int eventId, String reason) {
		eventDAO.signOffFromEvent(userId, eventId);
		Event event = eventDAO.getEventById(eventId);
		if (event != null && event.getLeaderUserId() > 0) {
			String notificationMessage = String.format("%s hat sich vom laufenden Event '%s' abgemeldet. Grund: %s",
					username, event.getName(), reason);
            NotificationPayload payload = new NotificationPayload();
            payload.setTitle("Team-Änderung");
            payload.setDescription(notificationMessage);
            payload.setLevel("Important");
            payload.setUrl("/veranstaltungen/details/" + eventId);

			notificationService.sendNotificationToUser(event.getLeaderUserId(), payload);
			logger.info("Sent sign-off notification to event leader (ID: {}) for event '{}'", event.getLeaderUserId(),
					event.getName());
		}
	}

	@Transactional
	public Event cloneEvent(int originalEventId, User adminUser) {
		Event originalEvent = eventDAO.getEventById(originalEventId);
		if (originalEvent == null) {
			throw new IllegalArgumentException("Original event not found.");
		}

		// Create a copy with a new name and future date
		Event clonedEvent = new Event();
		clonedEvent.setName(originalEvent.getName() + " (Kopie)");
		clonedEvent.setDescription(originalEvent.getDescription());
		clonedEvent.setLocation(originalEvent.getLocation());
		clonedEvent.setLeaderUserId(originalEvent.getLeaderUserId());
		clonedEvent.setEventDateTime(LocalDateTime.now().plus(7, ChronoUnit.DAYS).withHour(18).withMinute(0));

		int newEventId = eventDAO.createEvent(clonedEvent);
		clonedEvent.setId(newEventId);

		// Copy skill requirements
		List<SkillRequirement> skills = eventDAO.getSkillRequirementsForEvent(originalEventId);
		String[] courseIds = skills.stream().map(s -> String.valueOf(s.getRequiredCourseId())).toArray(String[]::new);
		String[] persons = skills.stream().map(s -> String.valueOf(s.getRequiredPersons())).toArray(String[]::new);
		eventDAO.saveSkillRequirements(newEventId, courseIds, persons);

		// Copy item reservations
		List<StorageItem> items = eventDAO.getReservedItemsForEvent(originalEventId);
		String[] itemIds = items.stream().map(i -> String.valueOf(i.getId())).toArray(String[]::new);
		String[] quantities = items.stream().map(i -> String.valueOf(i.getQuantity())).toArray(String[]::new);
		eventDAO.saveReservations(newEventId, itemIds, quantities);
		checklistDAO.generateChecklistFromReservations(newEventId);

		// Copy tasks (without assignments)
		List<EventTask> tasks = taskDAO.getTasksForEvent(originalEventId);
		for (EventTask task : tasks) {
			task.setEventId(newEventId);
			task.setStatus("OFFEN");
			taskDAO.saveTask(task, new int[0], new String[0], new String[0], new String[0], new int[0]);
		}

		adminLogService.log(adminUser.getUsername(), "CLONE_EVENT", "Event '" + originalEvent.getName() + "' (ID: "
				+ originalEventId + ") zu '" + clonedEvent.getName() + "' (ID: " + newEventId + ") geklont.");

		return clonedEvent;
	}

	@Transactional
	public Meeting cloneMeeting(int originalMeetingId, User adminUser) {
		Meeting originalMeeting = meetingDAO.getMeetingById(originalMeetingId);
		if (originalMeeting == null) {
			throw new IllegalArgumentException("Original meeting not found.");
		}

		Meeting clonedMeeting = new Meeting();
		clonedMeeting.setCourseId(originalMeeting.getCourseId());
		clonedMeeting.setName(originalMeeting.getName() + " (Kopie)");
		clonedMeeting.setDescription(originalMeeting.getDescription());
		clonedMeeting.setLocation(originalMeeting.getLocation());
		clonedMeeting.setLeaderUserId(originalMeeting.getLeaderUserId());
		clonedMeeting.setMeetingDateTime(LocalDateTime.now().plus(7, ChronoUnit.DAYS).withHour(19).withMinute(0));

		int newMeetingId = meetingDAO.createMeeting(clonedMeeting);
		clonedMeeting.setId(newMeetingId);

		adminLogService.log(adminUser.getUsername(), "CLONE_MEETING",
				"Meeting '" + originalMeeting.getName() + "' (ID: " + originalMeetingId + ") zu '"
						+ clonedMeeting.getName() + "' (ID: " + newMeetingId + ") geklont.");

		return clonedMeeting;
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