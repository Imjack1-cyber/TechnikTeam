// Rename CourseServlet.java to MeetingServlet.java and replace content
package de.technikteam.servlet;

import de.technikteam.dao.MeetingDAO; // <-- Use MeetingDAO
import de.technikteam.model.Meeting; // <-- Use Meeting
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/lehrgaenge") // The URL stays the same for the user
public class MeetingServlet extends HttpServlet { // Renamed class
	private static final long serialVersionUID = 1L;
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    User user = (User) request.getSession().getAttribute("user");

	    // FIX: Call the new method to get user-specific data
	    List<Meeting> meetings = meetingDAO.getUpcomingMeetingsForUser(user);
	    
	    request.setAttribute("meetings", meetings);
	    request.getRequestDispatcher("/lehrgaenge.jsp").forward(request, response);
	}
}