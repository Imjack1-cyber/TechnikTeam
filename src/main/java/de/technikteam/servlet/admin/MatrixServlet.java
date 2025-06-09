// Die Version aus meiner vorherigen Antwort war bereits korrekt und sammelt alle Daten.
// Wir stellen sicher, dass sie so aussieht:
package de.technikteam.servlet.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.UserDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;

@WebServlet("/admin/matrix")
public class MatrixServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;
	private CourseDAO courseDAO;
	private UserQualificationsDAO userQualificationsDAO;

	@Override
	public void init() {
		userDAO = new UserDAO();
		courseDAO = new CourseDAO();
		userQualificationsDAO = new UserQualificationsDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		List<User> allUsers = userDAO.getAllUsers();
		List<Course> allCourses = courseDAO.getAllCourses(); // Holt ALLE Kurse
		List<UserQualification> allQualifications = userQualificationsDAO.getAllQualifications();

		Map<String, UserQualification> qualificationMap = allQualifications.stream()
				.collect(Collectors.toMap(q -> q.getUserId() + "-" + q.getCourseId(), Function.identity()));

		request.setAttribute("allUsers", allUsers);
		request.setAttribute("allCourses", allCourses);
		request.setAttribute("qualificationMap", qualificationMap);

		request.getRequestDispatcher("/admin/admin_matrix.jsp").forward(request, response);
	}
}