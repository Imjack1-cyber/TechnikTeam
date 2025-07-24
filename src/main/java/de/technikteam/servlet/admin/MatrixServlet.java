package de.technikteam.servlet.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Meeting;
import de.technikteam.model.MeetingAttendance;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class MatrixServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MatrixServlet.class);
	private final UserDAO userDAO;
	private final CourseDAO courseDAO;
	private final MeetingDAO meetingDAO;
	private final MeetingAttendanceDAO meetingAttendanceDAO;

	@Inject
	public MatrixServlet(UserDAO userDAO, CourseDAO courseDAO, MeetingDAO meetingDAO,
			MeetingAttendanceDAO meetingAttendanceDAO) {
		this.userDAO = userDAO;
		this.courseDAO = courseDAO;
		this.meetingDAO = meetingDAO;
		this.meetingAttendanceDAO = meetingAttendanceDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Matrix data requested. Fetching all necessary data from DAOs.");

		List<User> allUsers = userDAO.getAllUsers();
		logger.debug("Fetched {} users.", allUsers.size());

		List<Course> allCourses = courseDAO.getAllCourses();
		logger.debug("Fetched {} parent courses.", allCourses.size());

		Map<Integer, List<Meeting>> meetingsByCourse = new HashMap<>();
		for (Course course : allCourses) {
			List<Meeting> meetings = meetingDAO.getMeetingsForCourse(course.getId());
			meetingsByCourse.put(course.getId(), meetings);
			logger.trace("Fetched {} meetings for course '{}' (ID: {}).", meetings.size(), course.getName(),
					course.getId());
		}

		Map<String, MeetingAttendance> attendanceMap = meetingAttendanceDAO.getAllAttendance().stream()
				.collect(Collectors.toMap(a -> a.getUserId() + "-" + a.getMeetingId(), Function.identity()));
		logger.debug("Fetched and mapped {} total attendance records.", attendanceMap.size());

		request.setAttribute("allUsers", allUsers);
		request.setAttribute("allCourses", allCourses);
		request.setAttribute("meetingsByCourse", meetingsByCourse);
		request.setAttribute("attendanceMap", attendanceMap);

		logger.info("Data generation for matrix complete. Forwarding to admin_matrix.jsp.");
		request.getRequestDispatcher("/views/admin/admin_matrix.jsp").forward(request, response);
	}
}