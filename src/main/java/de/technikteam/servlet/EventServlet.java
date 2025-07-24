package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class EventServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventServlet.class);
	private final EventDAO eventDAO;

	@Inject
	public EventServlet(EventDAO eventDAO) {
		this.eventDAO = eventDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		// CORRECTED: Added a defensive null check
		if (user == null) {
			logger.warn("EventServlet accessed without an authenticated user. Redirecting to login.");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		logger.info("Fetching upcoming events for user '{}' (ID: {})", user.getUsername(), user.getId());

		List<Event> allUpcomingEvents = eventDAO.getAllActiveAndUpcomingEvents();
		List<Event> qualifiedEvents = eventDAO.getUpcomingEventsForUser(user, 0);
		List<Integer> qualifiedEventIds = qualifiedEvents.stream().map(Event::getId).collect(Collectors.toList());

		for (Event event : allUpcomingEvents) {
			event.setUserQualified(qualifiedEventIds.contains(event.getId()));
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