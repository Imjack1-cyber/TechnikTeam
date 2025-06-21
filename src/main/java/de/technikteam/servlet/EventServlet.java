package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
 Responsible for the main event listing page at /events. It fetches a list of all upcoming events that the current user is qualified for, along with detailed information for each (like skill requirements), and passes the data to events.jsp for rendering.
 */

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