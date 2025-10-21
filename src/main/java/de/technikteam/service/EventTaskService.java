package de.technikteam.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.technikteam.api.v1.dto.NotificationPayload;
import de.technikteam.config.LocalDateTimeAdapter;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import de.technikteam.websocket.ChatSessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
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
	private final AdminLogService adminLogService;
	private final PolicyFactory richTextPolicy;
	private final ChatSessionManager sessionManager;
	private final Gson gson;
	private static final Logger logger = LogManager.getLogger(EventTaskService.class);

	private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

	@Autowired
	public EventTaskService(EventTaskDAO taskDAO, UserDAO userDAO, EventDAO eventDAO,
			NotificationService notificationService, AdminLogService adminLogService, @Qualifier("richTextPolicy") PolicyFactory richTextPolicy, ChatSessionManager sessionManager) {
		this.taskDAO = taskDAO;
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.notificationService = notificationService;
		this.adminLogService = adminLogService;
		this.richTextPolicy = richTextPolicy;
		this.sessionManager = sessionManager;
		this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
	}

	@Transactional
	public int saveTaskAndHandleMentions(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities,
			String[] kitIds, int[] dependencyIds, User currentUser) {

		logger.debug("Service: Saving task '{}'", task.getName());
		if (task.getDescription() != null) {
			String sanitizedDescription = richTextPolicy.sanitize(task.getDescription());
			logger.trace("Sanitized task details from '{}' to '{}'", task.getDescription(), sanitizedDescription);
			task.setDescription(sanitizedDescription);
		}

		String originalDescription = "";
		if (task.getId() > 0) {
			// This is not perfectly efficient, but good enough for this purpose.
			// A better solution would be to get the task from a service layer cache.
			EventTask existingTask = taskDAO.getTasksForEvent(task.getEventId()).stream()
					.filter(t -> t.getId() == task.getId()).findFirst().orElse(null);
			if (existingTask != null) {
				originalDescription = existingTask.getDescription();
			}
		}

		logger.debug("Calling DAO to save task. Is update: {}", (task.getId() > 0));
		int taskId = taskDAO.saveTask(task, userIds != null ? userIds : new int[0], itemIds, itemQuantities, kitIds,
				dependencyIds);
		task.setId(taskId); // Ensure the task object has the ID for mention handling
		logger.debug("DAO returned task ID: {}", taskId);

		// Handle mentions only if the details have changed
		if (task.getDescription() != null && !task.getDescription().equals(originalDescription)) {
			handleMentions(currentUser, task);
		}

		// Notify newly assigned users
		if (userIds != null) {
			notifyAssignedUsers(task, userIds, currentUser);
		}
        
        // After saving, re-calculate all task statuses for the event
        calculateAndUpdateTaskStatuses(task.getEventId());
        
        // Check if the saved task needs help
        EventTask savedTask = taskDAO.getTasksForEvent(task.getEventId()).stream().filter(t -> t.getId() == taskId).findFirst().orElse(null);
        if (savedTask != null) {
            sendHelpNeededNotification(savedTask);
        }

        // Broadcast full event object for real-time updates
        Event updatedEvent = eventDAO.getEventById(task.getEventId());
        Map<String, Object> broadcastPayload = Map.of("type", "EVENT_FULL_UPDATE", "payload", updatedEvent);
        sessionManager.broadcast(String.valueOf(task.getEventId()), gson.toJson(broadcastPayload));

		return taskId;
	}

	@Transactional
	public void deleteTask(int eventId, int taskId, User currentUser) {
		Event event = eventDAO.getEventById(eventId);
		if (event == null) throw new IllegalArgumentException("Event not found.");

		boolean canManage = currentUser.hasAdminAccess() || event.getLeaderUserId() == currentUser.getId();
		if (!canManage) {
			throw new AccessDeniedException("You do not have permission to delete tasks for this event.");
		}

		if (taskDAO.deleteTask(taskId)) {
			adminLogService.log(currentUser.getUsername(), "EVENT_TASK_DELETE", "Deleted task ID " + taskId + " from event '" + event.getName() + "'.");
			calculateAndUpdateTaskStatuses(eventId);

			// Broadcast full event object for real-time updates
			Event updatedEvent = eventDAO.getEventById(eventId);
			Map<String, Object> broadcastPayload = Map.of("type", "EVENT_FULL_UPDATE", "payload", updatedEvent);
			sessionManager.broadcast(String.valueOf(eventId), gson.toJson(broadcastPayload));
		}
	}
    
    @Transactional
    public void reorderTasks(int eventId, Map<String, List<Integer>> payload, User adminUser) {
        logger.info("User {} reordered tasks for event {}.", adminUser.getUsername(), eventId);
        for (Map.Entry<String, List<Integer>> entry : payload.entrySet()) {
            int categoryId = Integer.parseInt(entry.getKey());
            taskDAO.updateTaskOrders(entry.getValue(), categoryId);
        }
        
        // After reordering, it's crucial to recalculate statuses
        calculateAndUpdateTaskStatuses(eventId);

        // Broadcast full event object for real-time updates
        Event updatedEvent = eventDAO.getEventById(eventId);
        Map<String, Object> broadcastPayload = Map.of("type", "EVENT_FULL_UPDATE", "payload", updatedEvent);
        sessionManager.broadcast(String.valueOf(eventId), gson.toJson(broadcastPayload));
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
                NotificationPayload payload = new NotificationPayload();
                payload.setTitle(String.format("Neue Aufgabe in '%s'", event.getName()));
                payload.setDescription(String.format("%s hat Ihnen die Aufgabe '%s' zugewiesen.", currentUser.getUsername(), task.getName()));
                payload.setLevel("Informational");
                payload.setUrl("/veranstaltungen/details/" + event.getId());
				notificationService.sendNotificationToUser(userId, payload);
			}
		}
	}

	private void handleMentions(User currentUser, EventTask task) {
		Event event = eventDAO.getEventById(task.getEventId());
		if (event == null || task.getDescription() == null) {
			if (event == null)
				logger.warn("Cannot handle mentions for task {} because parent event {} was not found.", task.getId(),
						task.getEventId());
			return;
		}

		Set<String> mentionedUsernames = new HashSet<>();
		Matcher matcher = MENTION_PATTERN.matcher(task.getDescription());
		while (matcher.find()) {
			mentionedUsernames.add(matcher.group(1));
		}

		if (!mentionedUsernames.isEmpty()) {
			logger.debug("Found mentions for users: {}", mentionedUsernames);
		}

		for (String username : mentionedUsernames) {
			User mentionedUser = userDAO.getUserByUsername(username);
			if (mentionedUser != null && mentionedUser.getId() != currentUser.getId()) {
                NotificationPayload payload = new NotificationPayload();
                payload.setTitle(String.format("Erwähnung in Aufgabe für '%s'", event.getName()));
                payload.setDescription(String.format("%s hat Sie in der Aufgabe '%s' erwähnt.", currentUser.getUsername(), task.getName()));
                payload.setLevel("Informational");
                payload.setUrl("/veranstaltungen/details/" + event.getId());
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
			if (newStatus == null || !List.of("OPEN", "IN_PROGRESS", "DONE", "LOCKED").contains(newStatus)) {
				throw new IllegalArgumentException("Invalid status provided.");
			}
			if ("DONE".equals(newStatus)) {
				if (!"IN_PROGRESS".equals(task.getStatus())) {
					throw new IllegalStateException("Task must be IN_PROGRESS before it can be marked as DONE.");
				}
				if (task.getAssignedUsers().size() < task.getRequiredPersons()) {
					throw new IllegalStateException("Not enough people are assigned to complete this task.");
				}
			}
			taskDAO.updateTaskStatus(taskId, newStatus);
			if("DONE".equals(newStatus)) {
				calculateAndUpdateTaskStatuses(eventId);
			}
			break;

		case "claim":
			if (!isParticipant) {
				throw new SecurityException("You must be a participant of the event to claim tasks.");
			}
			// Check if user is already on another active task
			boolean isAlreadyOnActiveTask = event.getEventTasks().stream()
					.filter(t -> "IN_PROGRESS".equals(t.getStatus()))
					.anyMatch(t -> t.getAssignedUsers().stream().anyMatch(u -> u.getId() == currentUser.getId()));
			if (isAlreadyOnActiveTask) {
				throw new IllegalStateException("You are already working on another task.");
			}

			taskDAO.assignUserToTask(taskId, currentUser.getId());
            taskDAO.updateTaskStatus(taskId, "IN_PROGRESS");
			EventTask claimedTask = taskDAO.getTasksForEvent(eventId).stream().filter(t -> t.getId() == taskId).findFirst().orElse(null);
			if (claimedTask != null) sendHelpNeededNotification(claimedTask);
			break;

		case "unclaim":
			if (!isAssigned) {
				throw new SecurityException("You can only un-claim tasks assigned to you.");
			}
			taskDAO.unassignUserFromTask(taskId, currentUser.getId());
            EventTask unclaimedTask = taskDAO.getTasksForEvent(eventId).stream().filter(t -> t.getId() == taskId).findFirst().orElse(null);
			if (unclaimedTask != null) {
				if(unclaimedTask.getAssignedUsers().isEmpty()){
					taskDAO.updateTaskStatus(taskId, "OPEN");
				}
				sendHelpNeededNotification(unclaimedTask);
			}
            calculateAndUpdateTaskStatuses(eventId);
			break;

		default:
			throw new IllegalArgumentException("Invalid action: " + action);
		}


        // Broadcast full event object for real-time updates
        Event updatedEvent = eventDAO.getEventById(eventId);
        Map<String, Object> broadcastPayload = Map.of("type", "EVENT_FULL_UPDATE", "payload", updatedEvent);
        sessionManager.broadcast(String.valueOf(eventId), gson.toJson(broadcastPayload));
	}

    @Transactional
    public void calculateAndUpdateTaskStatuses(int eventId) {
        List<EventTask> allTasks = taskDAO.getTasksForEvent(eventId);
        Map<Integer, EventTask> taskMap = allTasks.stream().collect(Collectors.toMap(EventTask::getId, t -> t));

        for (EventTask task : allTasks) {
            // Skip tasks that are already completed.
            if ("DONE".equals(task.getStatus())) {
                continue;
            }

            boolean allDependenciesMet = task.getDependsOn().stream()
                .allMatch(dep -> "DONE".equals(taskMap.get(dep.getId()).getStatus()));

            String currentStatus = task.getStatus();
            String newStatus = null;

            if (allDependenciesMet) {
                // Dependencies are met. If it was locked, it should become open.
                if ("LOCKED".equals(currentStatus)) {
                    newStatus = "OPEN";
                }
            } else {
                // Dependencies are NOT met. Any non-done task should be locked.
                if (!"LOCKED".equals(currentStatus)) {
                    newStatus = "LOCKED";
                }
            }
            
            if (newStatus != null) {
                taskDAO.updateTaskStatus(task.getId(), newStatus);
            }
        }
    }

	private void sendHelpNeededNotification(EventTask task) {
		int assignedCount = task.getAssignedUsers().size();
		int requiredCount = task.getRequiredPersons();
		int neededCount = requiredCount - assignedCount;

		if (neededCount <= 0) {
			return; // No help needed
		}

		Event event = eventDAO.getEventById(task.getEventId());
		if (event == null) return;

		// Find users who are part of the event but not currently on any active task
		List<User> allEventParticipants = event.getAssignedAttendees();
		Set<Integer> usersOnActiveTasks = taskDAO.getTasksForEvent(event.getId()).stream()
				.filter(t -> "IN_PROGRESS".equals(t.getStatus()))
				.flatMap(t -> t.getAssignedUsers().stream())
				.map(User::getId)
				.collect(Collectors.toSet());

		List<User> availableUsers = allEventParticipants.stream()
				.filter(u -> !usersOnActiveTasks.contains(u.getId()))
				.collect(Collectors.toList());

		NotificationPayload payload = new NotificationPayload();
		payload.setTitle("Hilfe benötigt bei: " + event.getName());
		payload.setDescription(String.format("%d weitere Person(en) für Aufgabe '%s' benötigt.", neededCount, task.getName()));
		payload.setLevel("Important");
		payload.setUrl(String.format("/veranstaltungen/details/%d?action=claimTask&taskId=%d", event.getId(), task.getId()));

		availableUsers.forEach(user -> notificationService.sendNotificationToUser(user.getId(), payload));
	}
}