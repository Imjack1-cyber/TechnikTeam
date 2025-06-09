package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;

// Servlet for displaying the events page.
@WebServlet("/events")
public class EventServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;

	public void init() {
		eventDAO = new EventDAO();
	}

	// Modify doGet in src/main/java/de/technikteam/servlet/EventServlet.java
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		// Dieser Aufruf ist bereits korrekt, da die DAO-Methode den Status holt.
		List<Event> events = eventDAO.getUpcomingEventsForUser(user, 0);

		// Die Logik, um Details für die ausklappbare Ansicht zu laden, bleibt.
		for (Event event : events) {
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(event.getId()));
			if ("KOMPLETT".equals(event.getStatus())) {
				event.setAssignedAttendees(eventDAO.getAssignedUsersForEvent(event.getId()));
			}
		}

		request.setAttribute("events", events);
		request.getRequestDispatcher("/events.jsp").forward(request, response);
	}
}