package de.technikteam.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.model.User;

/**
 * Handles user actions related to courses, such as signing up or signing off.
 */
@WebServlet("/course-action")
public class CourseActionServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CourseActionServlet.class);
	private CourseDAO courseDAO;

	@Override
	public void init() {
		courseDAO = new CourseDAO();
	}

	/**
	 * Handles POST requests from the course list page to sign up or sign off.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Get the logged-in user and the form parameters
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");
		String courseIdParam = request.getParameter("courseId");

		// Basic validation
		if (user == null || action == null || courseIdParam == null) {
			logger.warn("Invalid request to CourseActionServlet. Missing parameters.");
			response.sendRedirect(request.getContextPath() + "/lehrgaenge");
			return;
		}

		try {
			int courseId = Integer.parseInt(courseIdParam);

			// -- THIS IS THE FIX --
			// Check the 'action' parameter and call the corresponding DAO method.
			if ("signup".equals(action)) {
				courseDAO.signUpForCourse(user.getId(), courseId);
				request.getSession().setAttribute("successMessage", "Erfolgreich zum Lehrgang angemeldet.");

			} else if ("signoff".equals(action)) {
				courseDAO.signOffFromCourse(user.getId(), courseId);
				request.getSession().setAttribute("successMessage", "Erfolgreich vom Lehrgang abgemeldet.");
			} else {
				logger.warn("Unknown action received in CourseActionServlet: {}", action);
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid course ID format in CourseActionServlet.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Lehrgangs-ID.");
		}

		// Redirect back to the course list page to show the result
		response.sendRedirect(request.getContextPath() + "/lehrgaenge");
	}
}