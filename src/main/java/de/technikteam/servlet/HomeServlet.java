package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Event;
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
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		eventDAO = new EventDAO();
		meetingDAO = new MeetingDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		logger.info("Home page requested by user '{}'. Fetching dashboard data.", user.getUsername());

		List<Event> upcomingEvents = eventDAO.getUpcomingEventsForUser(user, 3);
		logger.debug("Fetched {} upcoming events for home page.", upcomingEvents.size());

		List<Meeting> upcomingMeetings = meetingDAO.getUpcomingMeetingsForUser(user).stream().limit(3)
				.collect(Collectors.toList());
		logger.debug("Fetched {} upcoming meetings for home page.", upcomingMeetings.size());

		request.setAttribute("upcomingEvents", upcomingEvents);
		request.setAttribute("upcomingMeetings", upcomingMeetings);

		logger.debug("Forwarding to the correct home.jsp path.");
		// CORRECTION: The path must point to the new location inside /WEB-INF/views/
		request.getRequestDispatcher("/home").forward(request, response);
	}
}