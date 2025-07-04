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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapped to `/veranstaltungen`, this servlet is responsible for the main event
 * listing page for a logged-in user. It fetches a list of all upcoming events
 * for which the user is qualified, along with their specific attendance status
 * for each event (e.g., ZUGEWIESEN, ANGEMELDET, OFFEN). It then passes this
 * data to `events.jsp`.
 */
// CORRECTED: The servlet mapping is changed to /veranstaltungen to match all links and user expectations.
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

		// The DAO method intelligently calculates the most relevant status for the
		// user.
		List<Event> events = eventDAO.getUpcomingEventsForUser(user, 0); // 0 means no limit

		request.setAttribute("events", events);
		logger.debug("Found {} upcoming events for user '{}'. Forwarding to veranstaltungen.jsp.", events.size(),
				user.getUsername());
		// CORRECTED: Forward to the actual JSP file path.
		request.getRequestDispatcher("/views/public/events.jsp").forward(request, response);
	}
}