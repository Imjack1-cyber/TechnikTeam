package de.technikteam.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

@Singleton
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(HomeServlet.class);
	private final EventDAO eventDAO;
	private final EventTaskDAO eventTaskDAO;

	@Inject
	public HomeServlet(EventDAO eventDAO, EventTaskDAO eventTaskDAO) {
		this.eventDAO = eventDAO;
		this.eventTaskDAO = eventTaskDAO;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		// CORRECTED: Added a defensive null check. This is a critical safeguard.
		// If for any reason a request reaches this servlet without an authenticated
		// user,
		// it will be safely redirected instead of causing a 500 error.
		if (user == null) {
			logger.warn("HomeServlet accessed without an authenticated user in the session. Redirecting to login.");
			response.sendRedirect(request.getContextPath() + "/login");
			return;
		}

		logger.info("Home page requested by user '{}'. Fetching dashboard data.", user.getUsername());

		List<Event> assignedEvents = eventDAO.getAssignedEventsForUser(user.getId(), 5);
		List<EventTask> openTasks = eventTaskDAO.getOpenTasksForUser(user.getId());
		List<Event> upcomingEvents = eventDAO.getUpcomingEventsForUser(user, 5);

		logger.debug("Fetched {} assigned events, {} open tasks, and {} general upcoming events.",
				assignedEvents.size(), openTasks.size(), upcomingEvents.size());

		request.setAttribute("assignedEvents", assignedEvents);
		request.setAttribute("openTasks", openTasks);
		request.setAttribute("upcomingEvents", upcomingEvents);

		logger.debug("Forwarding to the correct home.jsp path.");
		request.getRequestDispatcher("/views/public/home.jsp").forward(request, response);
	}
}