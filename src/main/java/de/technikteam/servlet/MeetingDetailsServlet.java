// CREATE THIS NEW FILE: src/main/java/de/technikteam/servlet/MeetingDetailsServlet.java
package de.technikteam.servlet;

import de.technikteam.dao.MeetingDAO;
import de.technikteam.model.Meeting;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles GET requests to display the detailed view of a single Meeting.
 */
@WebServlet("/meetingDetails")
public class MeetingDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MeetingDAO meetingDAO;

	@Override
	public void init() {
		meetingDAO = new MeetingDAO();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String meetingIdParam = request.getParameter("id");
		if (meetingIdParam == null || meetingIdParam.isEmpty()) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Meeting-ID fehlt.");
			return;
		}

		try {
			int meetingId = Integer.parseInt(meetingIdParam);
			Meeting meeting = meetingDAO.getMeetingById(meetingId);

			// If no meeting is found for the given ID, show a 404 error.
			if (meeting == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "Meeting nicht gefunden.");
				return;
			}

			// In the future, you could fetch participant lists here if needed.
			// List<User> participants =
			// meetingAttendanceDAO.getAttendeesForMeeting(meetingId);
			// request.setAttribute("participants", participants);

			request.setAttribute("meeting", meeting);
			request.getRequestDispatcher("/meetingDetails.jsp").forward(request, response);

		} catch (NumberFormatException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Meeting-ID.");
		}
	}
}