package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Servlet for the home page.
@WebServlet("/home")
public class HomeServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private CourseDAO courseDAO;

	public void init() {
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO(); // <-- FIX: Diese Zeile hinzufügen, um das Objekt zu erstellen
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");

		// Fetch max 3 upcoming events for this user
		List<Event> upcomingEvents = eventDAO.getUpcomingEventsForUser(user, 3);

		// Fetch max 3 upcoming courses (this call will now work correctly)
		List<Course> upcomingCourses = courseDAO.getUpcomingCourses(3);

		request.setAttribute("upcomingEvents", upcomingEvents);
		request.setAttribute("upcomingCourses", upcomingCourses);

		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
}