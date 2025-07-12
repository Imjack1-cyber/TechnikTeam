package de.technikteam.service;

import de.technikteam.dao.AchievementDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class AchievementService {
	private static final Logger logger = LogManager.getLogger(AchievementService.class);
	private static final AchievementService INSTANCE = new AchievementService();

	private final AchievementDAO achievementDAO = new AchievementDAO();
	private final EventDAO eventDAO = new EventDAO();

	private AchievementService() {
	}

	public static AchievementService getInstance() {
		return INSTANCE;
	}

	public void checkAndGrantAchievements(User user, String triggerType) {
		switch (triggerType) {
		case "EVENT_COMPLETED":
			checkEventParticipationAchievements(user);
			checkEventLeaderAchievements(user);
			break;
		case "QUALIFICATION_GAINED":
			// This would be called from UserQualificationsDAO after an update
			break;
		}
	}

	private void checkEventParticipationAchievements(User user) {
		int completedEvents = eventDAO.getCompletedEventsForUser(user.getId()).size();
		logger.debug("Checking event participation achievements for user {}. Completed events: {}", user.getUsername(),
				completedEvents);
		if (completedEvents >= 1) {
			achievementDAO.grantAchievementToUser(user.getId(), "EVENT_PARTICIPANT_1");
		}
		if (completedEvents >= 5) {
			achievementDAO.grantAchievementToUser(user.getId(), "EVENT_PARTICIPANT_5");
		}
		if (completedEvents >= 10) {
			achievementDAO.grantAchievementToUser(user.getId(), "EVENT_PARTICIPANT_10");
		}
	}

	private void checkEventLeaderAchievements(User user) {
		List<Event> allEvents = eventDAO.getAllEvents();
		long ledEventsCount = allEvents.stream()
				.filter(event -> event.getLeaderUserId() == user.getId() && "ABGESCHLOSSEN".equals(event.getStatus()))
				.count();

		if (ledEventsCount >= 1) {
			achievementDAO.grantAchievementToUser(user.getId(), "EVENT_LEADER_1");
		}
	}
}