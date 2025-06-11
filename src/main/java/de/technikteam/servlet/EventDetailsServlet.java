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

	/**
	 * Handles GET requests to display the detailed view of a single event. It
	 * fetches the event data, its skill requirements, and for admins, a list of all
	 * signed-up users.
	 *
	 * @param request  The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @throws ServletException If a servlet-specific error occurs.
	 * @throws IOException      If an I/O error occurs.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			// Step 1: Get the event ID from the URL parameter.
			int eventId = Integer.parseInt(request.getParameter("id"));

			// Step 2: Fetch the main event object from the database.
			Event event = eventDAO.getEventById(eventId);

			// If no event is found for the given ID, show a 404 error.
			if (event == null) {
				logger.warn("Attempted to access non-existent event with ID: {}", eventId);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Event nicht gefunden.");
				return;
			}

			// Step 3: Fetch related data for the event.
			// The skill requirements are needed for all users.
			event.setSkillRequirements(eventDAO.getSkillRequirementsForEvent(eventId));
			// The list of finally assigned attendees is needed if the event is "KOMPLETT".
			if ("KOMPLETT".equalsIgnoreCase(event.getStatus())) {
				event.setAssignedAttendees(eventDAO.getAssignedUsersForEvent(eventId));
			}

			// Step 4: For Admins, fetch additional privileged information.
			User user = (User) request.getSession().getAttribute("user");
			if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
				// Fetch a list of all users who have actively signed up.
				List<User> signedUpUsers = eventDAO.getSignedUpUsersForEvent(eventId);
				request.setAttribute("signedUpUsers", signedUpUsers);
				logger.debug("Admin access: Fetched {} signed-up users for event {}.", signedUpUsers.size(), eventId);
			}

			// Step 5: Set the event object as a request attribute and forward to the JSP.
			request.setAttribute("event", event);
			request.getRequestDispatcher("/eventDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format received.", e);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Event-ID.");
		} catch (Exception e) {
			logger.error("An unexpected error occurred while fetching event details.", e);
			throw new ServletException(e); // Forward to the 500 error page
		}
	}
}