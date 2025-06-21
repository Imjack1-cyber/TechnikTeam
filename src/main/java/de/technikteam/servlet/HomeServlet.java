// In: src/main/java/de/technikteam/servlet/HomeServlet.java
package de.technikteam.servlet;

import de.technikteam.dao.EventDAO;
import de.technikteam.dao.MeetingDAO; // <-- Use MeetingDAO
import de.technikteam.model.Event;
import de.technikteam.model.Meeting; // <-- Use Meeting
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private EventDAO eventDAO;
	private MeetingDAO meetingDAO; // <-- Changed from CourseDAO

	public void init() {
		eventDAO = new EventDAO();
		meetingDAO = new MeetingDAO(); // <-- Initialize new DAO
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		List<Event> upcomingEvents = eventDAO.getUpcomingEventsForUser(user, 3);

		// FIX: Fetch upcoming MEETINGS
		// You might need a getUpcomingMeetings(limit) method in MeetingDAO
		List<Meeting> upcomingMeetings = meetingDAO.getAllMeetings(); // Assuming this gets upcoming

		request.setAttribute("upcomingEvents", upcomingEvents);
		request.setAttribute("upcomingMeetings", upcomingMeetings); // <-- Changed attribute name
		request.getRequestDispatcher("/home.jsp").forward(request, response);
	}
}