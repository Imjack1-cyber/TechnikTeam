package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapped to `/veranstaltungen`, this servlet is responsible for the main event
 * listing page for a logged-in user. It fetches a list of all upcoming events
 * and enriches each with user-specific data, such as attendance status and
 * qualification status.
 */
@WebServlet("/veranstaltungen")
public class EventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventServlet.class);
	private EventDAO eventDAO;

	public void init() {
		eventDAO = new EventDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		logger.info("Fetching upcoming events for user '{}' (ID: {})", user.getUsername(), user.getId());

		// Fetch all active/upcoming events first
		List<Event> allUpcomingEvents = eventDAO.getAllActiveAndUpcomingEvents();

		// Fetch events the user is qualified for to determine signup eligibility and
		// status
		List<Event> qualifiedEvents = eventDAO.getUpcomingEventsForUser(user, 0);
		List<Integer> qualifiedEventIds = qualifiedEvents.stream().map(Event::getId).collect(Collectors.toList());

		// Enrich all events with user-specific data
		for (Event event : allUpcomingEvents) {
			// Is the user qualified?
			event.setUserQualified(qualifiedEventIds.contains(event.getId()));

			// What is the user's status for this event?
			qualifiedEvents.stream().filter(qe -> qe.getId() == event.getId()).findFirst()
					.ifPresent(qe -> event.setUserAttendanceStatus(qe.getUserAttendanceStatus()));

			if (event.getUserAttendanceStatus() == null) {
				event.setUserAttendanceStatus("OFFEN");
			}
		}

		request.setAttribute("events", allUpcomingEvents);
		logger.debug("Found {} upcoming events for user '{}'. Forwarding to veranstaltungen.jsp.",
				allUpcomingEvents.size(), user.getUsername());
		request.getRequestDispatcher("/views/public/events.jsp").forward(request, response);
	}
}