package de.technikteam.servlet;

import de.technikteam.dao.MeetingAttendanceDAO;
import de.technikteam.model.User;
import de.technikteam.util.CSRFUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Mapped to `/meeting-action`, this servlet processes POST requests from the
 * main course/meeting listing page (`lehrgaenge.jsp`). It allows a logged-in
 * user to either sign up for (`signup`) or sign off from (`signoff`) a specific
 * meeting by updating the `meeting_attendance` table via the
 * `MeetingAttendanceDAO`.
 */
@WebServlet("/meeting-action")
public class MeetingActionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(MeetingActionServlet.class);
	private MeetingAttendanceDAO attendanceDAO;

	@Override
	public void init() {
		attendanceDAO = new MeetingAttendanceDAO();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User user = (User) request.getSession().getAttribute("user");

		if (!CSRFUtil.isTokenValid(request)) {
			logger.warn("CSRF token validation failed for meeting action by user '{}'",
					user != null ? user.getUsername() : "GUEST");
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF Token");
			return;
		}

		String action = request.getParameter("action");
		String meetingIdParam = request.getParameter("meetingId");

		if (user == null || action == null || meetingIdParam == null) {
			logger.warn("Invalid request to MeetingActionServlet. Missing user, action, or meetingId.");
			response.sendRedirect(request.getContextPath() + "/lehrgaenge");
			return;
		}

		try {
			int meetingId = Integer.parseInt(meetingIdParam);
			logger.info("User '{}' (ID: {}) performing action '{}' on meeting ID {}", user.getUsername(), user.getId(),
					action, meetingId);

			if ("signup".equals(action)) {
				attendanceDAO.setAttendance(user.getId(), meetingId, true, "");
				request.getSession().setAttribute("successMessage", "Erfolgreich zum Meeting angemeldet.");
			} else if ("signoff".equals(action)) {
				attendanceDAO.setAttendance(user.getId(), meetingId, false, "");
				request.getSession().setAttribute("successMessage", "Erfolgreich vom Meeting abgemeldet.");
			}
		} catch (NumberFormatException e) {
			logger.error("Invalid meeting ID format in MeetingActionServlet.", e);
			request.getSession().setAttribute("errorMessage", "Ungültige Meeting-ID.");
		}
		response.sendRedirect(request.getContextPath() + "/lehrgaenge");
	}
}