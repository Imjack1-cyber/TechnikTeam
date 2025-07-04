package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.EventTaskDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Event;
import de.technikteam.model.EventTask;
import de.technikteam.model.Meeting;
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(HomeServlet.class);
	private EventDAO eventDAO;
	private EventTaskDAO eventTaskDAO;
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		eventTaskDAO = new EventTaskDAO();
		meetingDAO = new MeetingDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
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