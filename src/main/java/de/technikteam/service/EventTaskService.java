package de.technikteam.service;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EventTaskService {

	private final EventTaskDAO taskDAO;
	private final UserDAO userDAO;
	private final EventDAO eventDAO;
	private final NotificationService notificationService;

	private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

	@Autowired
	public EventTaskService(EventTaskDAO taskDAO, UserDAO userDAO, EventDAO eventDAO,
			NotificationService notificationService) {
		this.taskDAO = taskDAO;
		this.userDAO = userDAO;
		this.eventDAO = eventDAO;
		this.notificationService = notificationService;
	}

	@Transactional
	public int saveTaskAndHandleMentions(EventTask task, int[] userIds, String[] itemIds, String[] itemQuantities,
			String[] kitIds, int[] dependencyIds, User currentUser) {
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

		int taskId = taskDAO.saveTask(task, userIds, itemIds, itemQuantities, kitIds, dependencyIds);

		// Handle mentions only if the details have changed
		if (task.getDetails() != null && !task.getDetails().equals(originalDetails)) {
			handleMentions(currentUser, task);
		}

		return taskId;
	}

	private void handleMentions(User currentUser, EventTask task) {
		Event event = eventDAO.getEventById(task.getEventId());
		if (event == null || task.getDetails() == null)
			return;

		Set<String> mentionedUsernames = new HashSet<>();
		Matcher matcher = MENTION_PATTERN.matcher(task.getDetails());
		while (matcher.find()) {
			mentionedUsernames.add(matcher.group(1));
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
}