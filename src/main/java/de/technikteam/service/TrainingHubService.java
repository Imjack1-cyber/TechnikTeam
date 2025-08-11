package de.technikteam.service;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrainingHubService {

	private final CourseDAO courseDAO;
	private final MeetingDAO meetingDAO;
	private final UserQualificationsDAO qualificationsDAO;

	@Autowired
	public TrainingHubService(CourseDAO courseDAO, MeetingDAO meetingDAO, UserQualificationsDAO qualificationsDAO) {
		this.courseDAO = courseDAO;
		this.meetingDAO = meetingDAO;
		this.qualificationsDAO = qualificationsDAO;
	}

	public List<Course> getCoursesWithMeetingsForUser(User user) {
		// 1. Get all courses
		List<Course> allCourses = courseDAO.getAllCourses();

		// 2. Get all upcoming meetings for the user (already enriched with signup
		// status)
		List<Meeting> allUpcomingMeetings = meetingDAO.getUpcomingMeetingsForUser(user);

		// 3. Group meetings by course ID
		Map<Integer, List<Meeting>> meetingsByCourseId = allUpcomingMeetings.stream()
				.collect(Collectors.groupingBy(Meeting::getCourseId));

		// 4. Combine data
		for (Course course : allCourses) {
			List<Meeting> meetingsForCourse = meetingsByCourseId.getOrDefault(course.getId(), List.of());
			course.setUpcomingMeetings(meetingsForCourse);
			course.setUserCourseStatus(determineUserCourseStatus(user.getId(), course.getId(), meetingsForCourse));
		}

		return allCourses;
	}

	private String determineUserCourseStatus(int userId, int courseId, List<Meeting> meetingsForCourse) {
		if (qualificationsDAO.hasUserCompletedCourse(userId, courseId)) {
			return "QUALIFIZIERT";
		}
		boolean isSignedUp = meetingsForCourse.stream().anyMatch(m -> "ANGEMELDET".equals(m.getUserAttendanceStatus()));
		if (isSignedUp) {
			return "IN_BEARBEITUNG";
		}
		return "VERFÜGBAR";
	}
}