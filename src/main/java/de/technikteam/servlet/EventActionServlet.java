package de.technikteam.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.technikteam.dao.EventDAO;
import de.technikteam.model.User;

/**
 * Handles user actions related to events, such as signing up or signing off.
 */
@WebServlet("/event-action")
public class EventActionServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(EventActionServlet.class);
	private EventDAO eventDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
	}

	/**
	 * Handles POST requests from the event list page to sign up or sign off.
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");
		String eventIdParam = request.getParameter("eventId");

		if (user == null || action == null || eventIdParam == null) {
			logger.warn("Invalid request to EventActionServlet. Missing parameters.");
			response.sendRedirect(request.getContextPath() + "/events");
			return;
		}

		try {
			int eventId = Integer.parseInt(eventIdParam);

			if ("signup".equals(action)) {
				eventDAO.signUpForEvent(user.getId(), eventId);
				request.getSession().setAttribute("successMessage", "Erfolgreich zum Event angemeldet.");

			} else if ("signoff".equals(action)) {
				eventDAO.signOffFromEvent(user.getId(), eventId);
				request.getSession().setAttribute("successMessage", "Erfolgreich vom Event abgemeldet.");

			} else {
				logger.warn("Unknown action received in EventActionServlet: {}", action);
			}

		} catch (NumberFormatException e) {
			logger.error("Invalid event ID format in EventActionServlet.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Event-ID.");
		}

		// Redirect back to the event list page to show the updated status
		response.sendRedirect(request.getContextPath() + "/events");
	}
}