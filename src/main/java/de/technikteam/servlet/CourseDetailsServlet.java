package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/courseDetails")
public class CourseDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CourseDetailsServlet.class);
	private CourseDAO courseDAO;

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			int courseId = Integer.parseInt(req.getParameter("id"));
			Course course = courseDAO.getCourseById(courseId);

			if (course == null) {
				logger.warn("Course with ID {} not found.", courseId);
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Lehrgang nicht gefunden.");
				return;
			}

			User user = (User) req.getSession().getAttribute("user");
			if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
				List<User> participants = courseDAO.getSignedUpUsersForCourse(courseId);
				req.setAttribute("participants", participants);
			}

			req.setAttribute("course", course);
			req.getRequestDispatcher("/courseDetails.jsp").forward(req, resp);

		} catch (NumberFormatException e) {
			logger.error("Invalid course ID provided in URL.", e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Lehrgangs-ID.");
		}
	}
}