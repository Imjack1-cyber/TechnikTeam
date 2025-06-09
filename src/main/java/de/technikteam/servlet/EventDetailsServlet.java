package de.technikteam.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventAttendance; // Import hinzufügen
import de.technikteam.model.User;

/**
 * Servlet for displaying the detailed view of a single event.
 */
@WebServlet("/eventDetails")
public class EventDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventDetailsServlet.class);
	private EventDAO eventDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			int eventId = Integer.parseInt(request.getParameter("id"));
			User user = (User) request.getSession().getAttribute("user");

			// Fetch the main event object
			Event event = eventDAO.getEventById(eventId);
			if (event == null) {
				logger.warn("Event with ID {} not found.", eventId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event nicht gefunden.");
				return;
			}

			// Always fetch skill requirements
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));

			// For admins, fetch detailed attendance information
			if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
				logger.debug("Admin access: fetching detailed attendance for event ID {}", eventId);
				// KORREKTUR: Lade die vollständigen EventAttendance-Objekte
				List<EventAttendance> attendances = eventDAO.getAttendanceDetailsForEvent(eventId);
				request.setAttribute("attendances", attendances);
			}

			request.setAttribute("event", event);
			request.getRequestDispatcher("/eventDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID provided in URL.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		}
	}
}