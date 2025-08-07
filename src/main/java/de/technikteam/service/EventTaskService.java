package de.technikteam.service;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EventTaskService {

	private final EventTaskDAO taskDAO;
	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final NotificationService notificationService;
	private final PolicyFactory richTextPolicy;
	private static final Logger logger = LogManager.getLogger(EventTaskService.class);

	private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

	@Autowired
	public EventTaskService(EventTaskDAO taskDAO, UserDAO userDAO, EventDAO eventDAO,
			NotificationService notificationService, @Qualifier("richTextPolicy") PolicyFactory richTextPolicy) {
		this.taskDAO = taskDAO;
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.notificationService = notificationService;
		this.richTextPolicy = richTextPolicy;
	}

	@Transactional
	public int saveTaskAndHandleMentions(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities,
			String[] kitIds, int[] dependencyIds, User currentUser) {

		logger.debug("Service: Saving task '{}'", task.getDescription());
		if (task.getDetails() != null) {
			String sanitizedDetails = richTextPolicy.sanitize(task.getDetails());
			logger.trace("Sanitized task details from '{}' to '{}'", task.getDetails(), sanitizedDetails);
			task.setDetails(sanitizedDetails);
		}

		String originalDetails = "";
		if (task.getId() > 0) {
			// This is not perfectly efficient, but good enough for this purpose.
			// A better solution would be to get the task from a service layer cache.
			EventTask existingTask = taskDAO.getTasksForEvent(task.getEventId()).stream()
					.filter(t -> t.getId() == task.getId()).findFirst().orElse(null);
			if (existingTask != null) {
				originalDetails = existingTask.getDetails();
			}
		}

		logger.debug("Calling DAO to save task. Is update: {}", (task.getId() > 0));
		int taskId = taskDAO.saveTask(task, userIds != null ? userIds : new int[0], itemIds, itemQuantities, kitIds,
				dependencyIds);
		task.setId(taskId); // Ensure the task object has the ID for mention handling
		logger.debug("DAO returned task ID: {}", taskId);

		// Handle mentions only if the details have changed
		if (task.getDetails() != null && !task.getDetails().equals(originalDetails)) {
			handleMentions(currentUser, task);
		}

		// Notify newly assigned users
		if (userIds != null) {
			notifyAssignedUsers(task, userIds, currentUser);
		}

		// Broadcast a general UI update to all clients to indicate that the event data
		// has changed.
		notificationService.broadcastUIUpdate("EVENT_UPDATED", Map.of("eventId", task.getEventId()));
		logger.debug("Broadcasted EVENT_UPDATED notification for eventId: {}", task.getEventId());

		return taskId;
	}

	private void notifyAssignedUsers(EventTask task, int[] assignedUserIds, User currentUser) {
		Event event = eventDAO.getEventById(task.getEventId());
		if (event == null) {
			logger.warn("Cannot notify assigned users for task {} because parent event {} was not found.", task.getId(),
					task.getEventId());
			return;
		}

		for (int userId : assignedUserIds) {
			if (userId != currentUser.getId()) {
				String title = String.format("Neue Aufgabe in '%s'", event.getName());
				String description = String.format("%s hat Ihnen die Aufgabe '%s' zugewiesen.",
						currentUser.getUsername(), task.getDescription());
				Map<String, Object> payload = Map.of("title", title, "description", description, "level",
						"Informational", "url", "/veranstaltungen/details/" + event.getId());
				notificationService.sendNotificationToUser(userId, payload);
			}
		}
	}

	private void handleMentions(User currentUser, EventTask task) {
		Event event = eventDAO.getEventById(task.getEventId());
		if (event == null || task.getDetails() == null) {
			if (event == null)
				logger.warn("Cannot handle mentions for task {} because parent event {} was not found.", task.getId(),
						task.getEventId());
			return;
		}

		Set<String> mentionedUsernames = new HashSet<>();
		Matcher matcher = MENTION_PATTERN.matcher(task.getDetails());
		while (matcher.find()) {
			mentionedUsernames.add(matcher.group(1));
		}

		if (!mentionedUsernames.isEmpty()) {
			logger.debug("Found mentions for users: {}", mentionedUsernames);
		}

		for (String username : mentionedUsernames) {
			User mentionedUser = userDAO.getUserByUsername(username);
			if (mentionedUser != null && mentionedUser.getId() != currentUser.getId()) {
				String title = String.format("Erwähnung in Aufgabe für '%s'", event.getName());
				String description = String.format("%s hat Sie in der Aufgabe '%s' erwähnt.", currentUser.getUsername(),
						task.getDescription());

				Map<String, Object> payload = Map.of("title", title, "description", description, "level",
						"Informational", "url", "/veranstaltungen/details/" + event.getId());
				notificationService.sendNotificationToUser(mentionedUser.getId(), payload);
			}
		}
	}

	@Transactional
	public void performUserTaskAction(int eventId, int taskId, String action, String newStatus, User currentUser) {
		Event event = eventDAO.getEventById(eventId);
		if (event == null)
			throw new IllegalArgumentException("Event not found.");

		EventTask task = event.getEventTasks().stream().filter(t -> t.getId() == taskId).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Task not found."));

		boolean isAssigned = task.getAssignedUsers().stream().anyMatch(u -> u.getId() == currentUser.getId());
		boolean isParticipant = eventDAO.isUserAssociatedWithEvent(eventId, currentUser.getId());
		boolean canManage = currentUser.hasAdminAccess() || event.getLeaderUserId() == currentUser.getId();

		switch (action) {
		case "updateStatus":
			if (!canManage && !isAssigned) {
				throw new SecurityException("You are not authorized to update the status of this task.");
			}
			if (newStatus == null || !List.of("OFFEN", "IN_ARBEIT", "ERLEDIGT").contains(newStatus)) {
				throw new IllegalArgumentException("Invalid status provided.");
			}
			taskDAO.updateTaskStatus(taskId, newStatus);
			break;

		case "claim":
			if (!task.getAssignedUsers().isEmpty()) {
				throw new IllegalStateException("Task is already assigned.");
			}
			if (!isParticipant) {
				throw new SecurityException("You must be a participant of the event to claim tasks.");
			}
			taskDAO.assignUserToTask(taskId, currentUser.getId());
			break;

		case "unclaim":
			if (!isAssigned) {
				throw new SecurityException("You can only un-claim tasks assigned to you.");
			}
			taskDAO.unassignUserFromTask(taskId, currentUser.getId());
			break;

		default:
			throw new IllegalArgumentException("Invalid action: " + action);
		}

		notificationService.broadcastUIUpdate("EVENT_UPDATED", Map.of("eventId", eventId));
	}
}