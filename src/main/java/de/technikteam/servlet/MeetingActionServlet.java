// Rename CourseActionServlet.java to MeetingActionServlet.java and replace content
package de.technikteam.servlet;

import de.technikteam.dao.MeetingAttendanceDAO; // <-- Use new DAO
import de.technikteam.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/meeting-action") // Change the URL
public class MeetingActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private MeetingAttendanceDAO attendanceDAO;

	@Override
	public void init() {
		attendanceDAO = new MeetingAttendanceDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");
		String action = request.getParameter("action");
		String meetingIdParam = request.getParameter("meetingId"); // <-- Use meetingId

		// Basic validation...

		try {
			int meetingId = Integer.parseInt(meetingIdParam);

			if ("signup".equals(action)) {
				// FIX: Call the new DAO method
				attendanceDAO.setAttendance(user.getId(), meetingId, true, "");
				request.getSession().setAttribute("successMessage", "Erfolgreich zum Meeting angemeldet.");
			} else if ("signoff".equals(action)) {
				// FIX: Call the new DAO method
				attendanceDAO.setAttendance(user.getId(), meetingId, false, "");
				request.getSession().setAttribute("successMessage", "Erfolgreich vom Meeting abgemeldet.");
			}
		} catch (NumberFormatException e) {
			// Error handling...
		}

		response.sendRedirect(request.getContextPath() + "/lehrgaenge");
	}
}