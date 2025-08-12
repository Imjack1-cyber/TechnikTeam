package de.technikteam.service;

import de.technikteam.dao.AchievementDAO;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AchievementService {
	private static final Logger logger = LogManager.getLogger(AchievementService.class);

	private final AchievementDAO achievementDAO;
	private final EventDAO eventDAO;
	private final CourseDAO courseDAO;

	@Autowired
	public AchievementService(AchievementDAO achievementDAO, EventDAO eventDAO, CourseDAO courseDAO) {
		this.achievementDAO = achievementDAO;
		this.eventDAO = eventDAO;
		this.courseDAO = courseDAO;
	}

	@Transactional
	public void checkAndGrantAchievements(User user, String triggerType) {
		checkAndGrantAchievements(user, triggerType, null);
	}

	@Transactional
	public void checkAndGrantAchievements(User user, String triggerType, Integer entityId) {
		switch (triggerType) {
		case "EVENT_COMPLETED":
			checkEventParticipationAchievements(user);
			checkEventLeaderAchievements(user);
			break;
		case "QUALIFICATION_GAINED":
			if (entityId != null) {
				checkSpecificQualificationAchievement(user, entityId);
			}
			break;
		}
	}

	private void checkSpecificQualificationAchievement(User user, int courseId) {
		Course course = courseDAO.getCourseById(courseId);
		if (course != null && course.getAbbreviation() != null && !course.getAbbreviation().isBlank()) {
			String achievementKey = "QUALIFICATION_GAINED_" + course.getAbbreviation().toUpperCase();
			logger.debug("Checking for qualification achievement '{}' for user {}", achievementKey, user.getUsername());
			achievementDAO.grantAchievementToUser(user.getId(), achievementKey);
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