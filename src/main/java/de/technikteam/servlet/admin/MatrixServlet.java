package de.technikteam.servlet.admin;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Meeting;
import de.technikteam.model.MeetingAttendance;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
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

/**
 * Mapped to `/admin/matrix`, this servlet constructs the data for the
 * comprehensive qualification and attendance matrix. It fetches all users, all
 * course templates, all meetings for each course, and all attendance records.
 * It then organizes this data and forwards it to `admin_matrix.jsp` for
 * rendering a grid view that shows which users have attended which course
 * meetings.
 */
@WebServlet("/admin/matrix")
public class MatrixServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MatrixServlet.class);
	private UserDAO userDAO;
	private CourseDAO courseDAO;
	private MeetingDAO meetingDAO;
	private MeetingAttendanceDAO meetingAttendanceDAO;

	@Override
	public void init() {
		userDAO = new UserDAO();
		courseDAO = new CourseDAO();
		meetingDAO = new MeetingDAO();
		meetingAttendanceDAO = new MeetingAttendanceDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		logger.info("Matrix data requested. Fetching all necessary data from DAOs.");

		// 1. Fetch all users who will be rows in the matrix
		List<User> allUsers = userDAO.getAllUsers();
		logger.debug("Fetched {} users.", allUsers.size());

		// 2. Fetch all parent courses which define the top-level columns
		List<Course> allCourses = courseDAO.getAllCourses();
		logger.debug("Fetched {} parent courses.", allCourses.size());

		// 3. For each course, fetch its scheduled meetings to create sub-columns
		Map<Integer, List<Meeting>> meetingsByCourse = new HashMap<>();
		for (Course course : allCourses) {
			List<Meeting> meetings = meetingDAO.getMeetingsForCourse(course.getId());
			meetingsByCourse.put(course.getId(), meetings);
			logger.trace("Fetched {} meetings for course '{}' (ID: {}).", meetings.size(), course.getName(),
					course.getId());
		}

		// 4. Fetch all attendance records and put them in a map for quick lookup.
		// The key is a "userId-meetingId" string.
		Map<String, MeetingAttendance> attendanceMap = meetingAttendanceDAO.getAllAttendance().stream()
				.collect(Collectors.toMap(a -> a.getUserId() + "-" + a.getMeetingId(), Function.identity()));
		logger.debug("Fetched and mapped {} total attendance records.", attendanceMap.size());

		// 5. Set all data as request attributes for the JSP
		request.setAttribute("allUsers", allUsers);
		request.setAttribute("allCourses", allCourses);
		request.setAttribute("meetingsByCourse", meetingsByCourse);
		request.setAttribute("attendanceMap", attendanceMap);

		logger.info("Data generation for matrix complete. Forwarding to admin_matrix.jsp.");
		request.getRequestDispatcher("/admin/admin_matrix.jsp").forward(request, response);
	}
}