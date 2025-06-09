package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.UserQualificationsDAO;
import de.technikteam.model.Course;
import de.technikteam.model.User;
import de.technikteam.model.UserQualification;

/**
 * Servlet for the public-facing course list page (/lehrgaenge). It fetches all
 * upcoming courses and displays them with user-specific information, such as
 * attendance status and which courses have already been attended, to enable
 * filtering.
 */
@WebServlet("/lehrgaenge")
public class CourseServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(CourseServlet.class);

	// Data Access Objects for database communication
	private CourseDAO courseDAO;
	private UserQualificationsDAO userQualificationsDAO;

	/**
	 * Initializes the servlet and its required DAO instances. This method is called
	 * by the container when the servlet is first loaded.
	 */
	@Override
	public void init() {
		courseDAO = new CourseDAO();
		userQualificationsDAO = new UserQualificationsDAO();
		logger.info("CourseServlet initialized.");
	}

	/**
	 * Handles GET requests to display the list of upcoming courses for the
	 * logged-in user. It gathers all necessary data for the JSP to render the
	 * complete view, including data needed for the client-side filter.
	 *
	 * @param request  The HttpServletRequest object containing the client's
	 *                 request.
	 * @param response The HttpServletResponse object for sending the response.
	 * @throws ServletException If a servlet-specific error occurs.
	 * @throws IOException      If an I/O error occurs.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Step 1: Get the currently logged-in user from the session.
		User user = (User) request.getSession().getAttribute("user");

		// Safety check: If no user is logged in, redirect to the login page.
		// This should not happen if the AuthenticationFilter is working correctly, but
		// serves as a fallback.
		if (user == null) {
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		logger.debug("Fetching course list and qualifications for user: {}", user.getUsername());

		// Step 2: Fetch the list of all upcoming courses.
		// This DAO method is specifically designed to also fetch the user's
		// signup_status via a LEFT JOIN.
		List<Course> courses = courseDAO.getUpcomingCoursesForUser(user);

		// Step 3: Fetch all qualifications (attended/completed courses) for this
		// specific user.
		// This is needed for the client-side filter functionality.
		List<UserQualification> attendedQualifications = userQualificationsDAO.getQualificationsForUser(user.getId());

		// Step 4: For efficient checking in the JSP, extract only the course IDs into a
		// Set.
		// A Set provides a very fast .contains() check.
		Set<Integer> attendedCourseIds = attendedQualifications.stream().map(UserQualification::getCourseId)
				.collect(Collectors.toSet());

		// Step 5: Place the gathered data into the request scope so the JSP can access
		// it.
		request.setAttribute("courses", courses);
		request.setAttribute("attendedCourseIds", attendedCourseIds);

		// Step 6: Forward the request and response objects to the JSP for rendering.
		request.getRequestDispatcher("/lehrgaenge.jsp").forward(request, response);
	}
}