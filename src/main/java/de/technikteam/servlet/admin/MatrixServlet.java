package de.technikteam.servlet.admin;

// All necessary imports remain the same...
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@WebServlet("/admin/matrix")
public class MatrixServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
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

		List<User> allUsers = userDAO.getAllUsers();
		List<Course> allCourses = courseDAO.getAllCourses();
		Map<Integer, List<Meeting>> meetingsByCourse = new HashMap<>();
		for (Course course : allCourses) {
			meetingsByCourse.put(course.getId(), meetingDAO.getMeetingsForCourse(course.getId()));
		}
		Map<String, MeetingAttendance> attendanceMap = meetingAttendanceDAO.getAllAttendance().stream()
				.collect(Collectors.toMap(a -> a.getUserId() + "-" + a.getMeetingId(), Function.identity()));

		// =================================================================
		//  REMOVED: The entire modalDataStore map and JSON conversion.
		//  It is no longer necessary with the new data-* attribute approach.
		// =================================================================

		request.setAttribute("allUsers", allUsers);
		request.setAttribute("allCourses", allCourses);
		request.setAttribute("meetingsByCourse", meetingsByCourse);
		request.setAttribute("attendanceMap", attendanceMap);

		request.getRequestDispatcher("/admin/admin_matrix.jsp").forward(request, response);
	}
}