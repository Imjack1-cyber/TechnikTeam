package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.CourseDAO;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Course;
import de.technikteam.model.Event;
import de.technikteam.model.User;

// Servlet for the home page.
@WebServlet("/home")
public class HomeServlet extends HttpServlet {
	private EventDAO eventDAO;
	private CourseDAO courseDAO;

	public void init() {
		eventDAO = new EventDAO();
		courseDAO = new CourseDAO(); // <-- FIX: Diese Zeile hinzufügen, um das Objekt zu erstellen
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		List<Event> upcomingEvents = eventDAO.getUpcomingEventsForUser(user, 3);
		List<Course> upcomingCourses = courseDAO.getUpcomingCourses(3); // Updated

		request.setAttribute("upcomingEvents", upcomingEvents);
		request.setAttribute("upcomingCourses", upcomingCourses); // Updated

		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
}