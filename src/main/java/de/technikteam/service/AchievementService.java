package de.technikteam.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.AchievementDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Singleton
public class AchievementService {
	private static final Logger logger = LogManager.getLogger(AchievementService.class);

	private final AchievementDAO achievementDAO;
	private final EventDAO eventDAO;

	@Inject
	public AchievementService(AchievementDAO achievementDAO, EventDAO eventDAO) {
		this.achievementDAO = achievementDAO;
		this.eventDAO = eventDAO;
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